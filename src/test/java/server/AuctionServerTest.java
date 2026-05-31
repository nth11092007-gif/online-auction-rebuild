package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.java_websocket.WebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.LoggerFactory;

import model.User;
import server.command.*;
import service.AuctionService;
import service.SettlementService;
import service.UserService;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
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

    // Mock commands
    @Mock private JoinSessionCommand joinCommand;
    @Mock private PlaceBidCommand bidCommand;
    @Mock private SettleSessionCommand settleCommand;
    @Mock private LoginCommand loginCommand;
    @Mock private GetSessionsCommand getSessionsCommand;
    @Mock private GetUserCommand getUserCommand;

    private AuctionServer server;

    @BeforeEach
    void setUp() throws Exception {
        server = mock(AuctionServer.class,
                withSettings().defaultAnswer(CALLS_REAL_METHODS));

        // Gán logger
        Field loggerField = AuctionServer.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        loggerField.set(server, LoggerFactory.getLogger(AuctionServer.class));

        // Gán Gson
        Field gsonField = AuctionServer.class.getDeclaredField("gson");
        gsonField.setAccessible(true);
        gsonField.set(server, new Gson());

        // Executor đồng bộ (chạy ngay)
        ExecutorService directExecutor = mock(ExecutorService.class);
        doAnswer(inv -> {
            ((Runnable) inv.getArgument(0)).run();
            return null;
        }).when(directExecutor).submit(any(Runnable.class));
        Field execField = AuctionServer.class.getDeclaredField("commandExecutor");
        execField.setAccessible(true);
        execField.set(server, directExecutor);

        // Tiêm các dependency service
        setField(server, "auctionService", auctionService);
        setField(server, "settlementService", settlementService);
        setField(server, "userService", userService);
        setField(server, "feedServer", feedServer);

        // Tạo command map với MOCK command
        Map<String, Command> commandMap = new HashMap<>();
        commandMap.put("LOGIN", loginCommand);
        commandMap.put("GET_SESSIONS", getSessionsCommand);
        commandMap.put("GET_USER", getUserCommand);
        commandMap.put("JOIN", joinCommand);
        commandMap.put("BID", bidCommand);
        commandMap.put("SETTLE", settleCommand);

        setField(server, "commandMap", commandMap);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = AuctionServer.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void onMessage_JOIN_DispatchesToJoinCommand() {
        String msg = "{\"type\":\"JOIN\", \"sessionId\":\"SS001\"}";
        server.onMessage(webSocket, msg);

        // Verify rằng joinCommand được gọi với WebSocket và JsonObject chứa đúng thông tin
        verify(joinCommand).execute(eq(webSocket), any(JsonObject.class));
    }

    @Test
    void onMessage_BID_DispatchesToBidCommand() {
        String msg = "{\"type\":\"BID\", \"auctionId\":\"SS001\", \"amount\":150.0}";
        server.onMessage(webSocket, msg);
        verify(bidCommand).execute(eq(webSocket), any(JsonObject.class));
    }

    @Test
    void onMessage_SETTLE_DispatchesToSettleCommand() {
        String msg = "{\"type\":\"SETTLE\", \"sessionId\":\"SS001\"}";
        server.onMessage(webSocket, msg);
        verify(settleCommand).execute(eq(webSocket), any(JsonObject.class));
    }

    @Test
    void onMessage_InvalidJson_NoCrash() {
        server.onMessage(webSocket, "not a json");
        // Không throw exception là pass
    }

    @Test
    void onMessage_MissingType_NoCrash() {
        server.onMessage(webSocket, "{\"userId\":1}");
        // Không throw exception là pass
    }
}