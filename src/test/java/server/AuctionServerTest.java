package server;

import com.google.gson.Gson;
import org.java_websocket.WebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.LoggerFactory;

import dao.UserDAO;
import model.User;
import server.command.*;
import service.AuctionService;
import service.SettlementService;
import service.UserService;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuctionServerTest {

    @Mock private AuctionService auctionService;
    @Mock private SettlementService settlementService;
    @Mock private UserService userService;
    @Mock private AuctionFeedServer feedServer;
    @Mock private WebSocket webSocket;
    @Mock private UserDAO userDAO;

    private AuctionServer server;
    private Map<String, Set<WebSocket>> sessionSubscribers;
    private Map<WebSocket, Map<String, WebSocketObserver>> connectionObservers;

    @BeforeEach
    void setUp() throws Exception {
        server = mock(AuctionServer.class,
                withSettings().defaultAnswer(CALLS_REAL_METHODS));

        // --- Gán logger ---
        Field loggerField = AuctionServer.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        loggerField.set(server, LoggerFactory.getLogger(AuctionServer.class));

        // --- Gán Gson ---
        Field gsonField = AuctionServer.class.getDeclaredField("gson");
        gsonField.setAccessible(true);
        gsonField.set(server, new Gson());

        // --- Tạo ExecutorService đồng bộ (chạy ngay) ---
        ExecutorService directExecutor = mock(ExecutorService.class);
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(directExecutor).submit(any(Runnable.class));
        Field execField = AuctionServer.class.getDeclaredField("commandExecutor");
        execField.setAccessible(true);
        execField.set(server, directExecutor);

        // Tiêm các dependency
        setField(server, "auctionService", auctionService);
        setField(server, "settlementService", settlementService);
        setField(server, "userService", userService);
        setField(server, "feedServer", feedServer);

        // Khởi tạo các map subscriber
        sessionSubscribers = new ConcurrentHashMap<>();
        connectionObservers = new ConcurrentHashMap<>();
        setField(server, "sessionSubscribers", sessionSubscribers);
        setField(server, "connectionObservers", connectionObservers);

        // Tạo command map với mock
        Map<String, Command> commandMap = new HashMap<>();
        commandMap.put("LOGIN", new LoginCommand(userDAO));
        commandMap.put("GET_SESSIONS", new GetSessionsCommand(auctionService));
        commandMap.put("GET_USER", new GetUserCommand(userService));
        commandMap.put("JOIN", new JoinSessionCommand(sessionSubscribers,
                connectionObservers, feedServer, auctionService, userService));
        commandMap.put("BID", new PlaceBidCommand(auctionService, userService));
        commandMap.put("SETTLE", new SettleSessionCommand(settlementService, feedServer));

        setField(server, "commandMap", commandMap);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = AuctionServer.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void onMessage_JOIN_Subscribes() {
        String msg = "{\"type\":\"JOIN\", \"sessionId\":\"SS001\"}";
        server.onMessage(webSocket, msg);
        verify(feedServer).subscribe(eq("SS001"), any(WebSocketObserver.class));
    }

    @Test
    void onMessage_BID_PlaceBidCalled() {
        String msg = "{\"type\":\"BID\", \"auctionId\":\"SS001\", \"amount\":150.0}";
        when(webSocket.getAttachment()).thenReturn("bidder1");
        User bidder = mock(User.class);
        when(bidder.getId()).thenReturn(2);
        when(userService.getUserByUsername("bidder1")).thenReturn(bidder);
        when(auctionService.placeBid(2, "SS001", 150.0)).thenReturn(true);

        server.onMessage(webSocket, msg);
        verify(auctionService).placeBid(2, "SS001", 150.0);
    }

    @Test
    void onMessage_InvalidJson_NoCrash() {
        server.onMessage(webSocket, "not a json");
    }

    @Test
    void onMessage_MissingType_NoCrash() {
        server.onMessage(webSocket, "{\"userId\":1}");
    }

    @Test
    void onMessage_SETTLE_TriggersSettlement() {
        String msg = "{\"type\":\"SETTLE\", \"sessionId\":\"SS001\"}";
        when(settlementService.settleAuction("SS001")).thenReturn(true);
        server.onMessage(webSocket, msg);
        verify(settlementService).settleAuction("SS001");
    }
}