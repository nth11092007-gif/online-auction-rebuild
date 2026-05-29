package server;

import org.java_websocket.WebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import model.User;
import service.AuctionService;
import service.SettlementService;
import service.UserService;

@ExtendWith(MockitoExtension.class)
class AuctionServerTest {

    @Mock private AuctionService auctionService;
    @Mock private SettlementService settlementService;
    @Mock private UserService userService;
    @Mock private AuctionFeedServer feedServer;
    @Mock private WebSocket webSocket;

    private AuctionServer server;

    @BeforeEach
    void setUp() {
        server = new AuctionServer(8080, auctionService, settlementService, userService, feedServer);
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
        when(bidder.getID()).thenReturn(2);
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
