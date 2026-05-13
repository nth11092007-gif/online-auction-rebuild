package service;

import dao.*;
import model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import server.AuctionFeedServer;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock private DataSource dataSource;
    @Mock private Connection connection;
    @Mock private UserDAO userDAO;
    @Mock private BidDAO bidDAO;
    @Mock private AuctionSessionDAO sessionDAO;
    @Mock private AuctionFeedServer feedServer;

    private AuctionService auctionService;
    private AuctionSession openSession, closedSession, pendingSession;
    private User bidder, seller;

    @BeforeEach
    void setUp() throws Exception {
        auctionService = new AuctionService(dataSource, userDAO, bidDAO, sessionDAO);
        auctionService.setFeedServer(feedServer);
        when(dataSource.getConnection()).thenReturn(connection);

        seller = new User(1, "seller", "p", "S", "s@t", "0", User.Role.USER, 1000, 0);
        bidder = new User(2, "bidder", "p", "B", "b@t", "0", User.Role.USER, 500, 0);

        openSession = new AuctionSession(seller, null, 100.0, 10.0, LocalDateTime.now());
        openSession.status = AuctionSession.Status.OPEN;
        openSession.setCurrentPrice(100.0);
        openSession.setEndTime(LocalDateTime.now().plusDays(1));

        closedSession = new AuctionSession(seller, null, 100.0, 10.0, LocalDateTime.now());
        closedSession.status = AuctionSession.Status.CLOSED;

        pendingSession = new AuctionSession(seller, null, 100.0, 10.0, LocalDateTime.now());
        pendingSession.status = AuctionSession.Status.PENDING;
    }

    // TC1: Thành công
    @Test
    void placeBid_Successful() throws Exception {
        String sessionId = "SS001";
        double bidAmount = 150.0;

        when(sessionDAO.getSessionById(connection, sessionId)).thenReturn(openSession);
        when(bidDAO.getHighestBid(connection, sessionId)).thenReturn(null);
        when(userDAO.getUserById(connection, 2)).thenReturn(bidder);
        when(bidDAO.addBid(eq(connection), eq(sessionId), any(Bid.class))).thenReturn(true);

        boolean result = auctionService.placeBid(2, sessionId, bidAmount);
        assertTrue(result);
        verify(bidDAO).addBid(eq(connection), eq(sessionId), any(Bid.class));
        verify(connection).commit();
        verify(feedServer).notifyObservers(eq(sessionId), contains("NEW_BID"));
    }

    // TC2: Sai giá (thấp hơn giá hiện tại + bước giá)
    @Test
    void placeBid_InvalidPrice() throws Exception {
        String sessionId = "SS001";
        // currentPrice = 100, step = 10 => cần >= 110
        Bid currentHighest = new Bid(bidder, 100.0);
        when(sessionDAO.getSessionById(connection, sessionId)).thenReturn(openSession);
        when(bidDAO.getHighestBid(connection, sessionId)).thenReturn(currentHighest);

        boolean result = auctionService.placeBid(2, sessionId, 105.0);
        assertFalse(result);
        verify(bidDAO, never()).addBid(any(), any(), any());
        verify(connection, never()).commit();
    }

    // TC3: Tự đấu giá (seller bid)
    @Test
    void placeBid_SellerCannotBid() throws Exception {
        when(sessionDAO.getSessionById(connection, "SS001")).thenReturn(openSession);
        boolean result = auctionService.placeBid(1, "SS001", 150.0);
        assertFalse(result);
    }

    // TC4: Hết tiền
    @Test
    void placeBid_InsufficientBalance() throws Exception {
        User poorBidder = new User(3, "poor", "p", "P", "p@t", "0", User.Role.USER, 50, 0);
        when(sessionDAO.getSessionById(connection, "SS001")).thenReturn(openSession);
        when(bidDAO.getHighestBid(connection, "SS001")).thenReturn(null);
        when(userDAO.getUserById(connection, 3)).thenReturn(poorBidder);

        boolean result = auctionService.placeBid(3, "SS001", 200.0);
        assertFalse(result);
    }

    // TC5: Sai trạng thái (CLOSED và PENDING)
    @Test
    void placeBid_ClosedSession() throws Exception {
        when(sessionDAO.getSessionById(connection, "SS002")).thenReturn(closedSession);
        boolean result = auctionService.placeBid(2, "SS002", 150.0);
        assertFalse(result);
        verify(bidDAO, never()).addBid(any(), any(), any());
    }

    @Test
    void placeBid_PendingSession() throws Exception {
        when(sessionDAO.getSessionById(connection, "SS003")).thenReturn(pendingSession);
        boolean result = auctionService.placeBid(2, "SS003", 150.0);
        assertFalse(result);
    }

    // TC6: Anti-sniping (2 trường hợp)
    @Test
    void placeBid_AntiSniping_NotTriggered() throws Exception {
        // Còn 181 giây -> không gia hạn
        openSession.setEndTime(LocalDateTime.now().plusSeconds(181));
        when(sessionDAO.getSessionById(connection, "SS004")).thenReturn(openSession);
        when(bidDAO.getHighestBid(connection, "SS004")).thenReturn(null);
        when(userDAO.getUserById(connection, 2)).thenReturn(bidder);
        when(bidDAO.addBid(any(), any(), any())).thenReturn(true);

        LocalDateTime before = openSession.getEndTime();
        auctionService.placeBid(2, "SS004", 150.0);
        assertEquals(before, openSession.getEndTime()); // Không thay đổi
        verify(sessionDAO, never()).updateEndTime(any(), any(), any());
    }

    @Test
    void placeBid_AntiSniping_Extends() throws Exception {
        // Còn 90 giây -> được cộng 3 phút
        openSession.setEndTime(LocalDateTime.now().plusSeconds(90));
        when(sessionDAO.getSessionById(connection, "SS005")).thenReturn(openSession);
        when(bidDAO.getHighestBid(connection, "SS005")).thenReturn(null);
        when(userDAO.getUserById(connection, 2)).thenReturn(bidder);
        when(bidDAO.addBid(any(), any(), any())).thenReturn(true);

        LocalDateTime before = openSession.getEndTime();
        auctionService.placeBid(2, "SS005", 150.0);
        assertTrue(openSession.getEndTime().isAfter(before.plusMinutes(2)));
        verify(sessionDAO).updateEndTime(eq(connection), eq("SS005"), any());
    }

    // TC7: Rollback khi có lỗi SQL
    @Test
    void placeBid_RollbackOnError() throws Exception {
        String sessionId = "SS006";
        when(sessionDAO.getSessionById(connection, sessionId)).thenReturn(openSession);
        when(bidDAO.getHighestBid(connection, sessionId)).thenReturn(null);
        when(userDAO.getUserById(connection, 2)).thenReturn(bidder);

        // Giả lập lỗi SQL ở bước addBid (hoặc bất kỳ bước nào)
        doThrow(new SQLException("DB error")).when(bidDAO).addBid(any(), any(), any());

        boolean result = auctionService.placeBid(2, sessionId, 150.0);

        // Kỳ vọng: trả về false do lỗi
        assertFalse(result);

        // Phải rollback
        verify(connection).rollback();
        // Không được commit
        verify(connection, never()).commit();
    }
}