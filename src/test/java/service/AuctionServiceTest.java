// package service;

// import dao.*;
// import model.*;
// import org.junit.jupiter.api.*;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.*;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.mockito.junit.jupiter.MockitoSettings;
// import org.mockito.quality.Strictness;
// import service.AuctionEventPublisher;
// import javax.sql.DataSource;
// import java.sql.Connection;
// import java.sql.SQLException;
// import java.time.LocalDateTime;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// @MockitoSettings(strictness = Strictness.LENIENT)
// class AuctionServiceTest {

//     @Mock private DataSource dataSource;
//     @Mock private Connection connection;
//     @Mock private UserDAO userDAO;
//     @Mock private BidDAO bidDAO;
//     @Mock private AuctionSessionDAO sessionDAO;
//     @Mock private ProxyBidDAOImpl proxyBidDAO;
//     @Mock private AuctionFeedServer feedServer;

//     private AuctionService auctionService;
//     private AuctionSession openSession, closedSession, pendingSession;
//     private User bidder;
//     private Seller sellerMock;
//     private Item itemMock;

//     @BeforeEach
//     void setUp() throws Exception {
//         auctionService = new AuctionService(dataSource, userDAO, bidDAO, sessionDAO, proxyBidDAO);
//         auctionService.setFeedServer(feedServer);
//         when(dataSource.getConnection()).thenReturn(connection);

//         sellerMock = mock(Seller.class);
//         when(sellerMock.getID()).thenReturn(1);
//         itemMock = mock(Item.class);

//         bidder = new User(2, "bidder", "p", "B", "b@t", "0", User.Role.USER, 500, 0);

//         // OPEN session (đã gọi startSession, nhưng state vẫn là Pending do chưa cập nhật)
//         openSession = new AuctionSession(sellerMock, itemMock, 100.0, 10.0, LocalDateTime.now());
//         openSession.startSession(1);

//         // CLOSED session (sau khi endSession, status CLOSED nhưng state vẫn Pending)
//         closedSession = new AuctionSession(sellerMock, itemMock, 100.0, 10.0, LocalDateTime.now());
//         closedSession.startSession(1);
//         closedSession.endSession();

//         // PENDING session (chưa start, set endTime để tránh NPE)
//         pendingSession = new AuctionSession(sellerMock, itemMock, 100.0, 10.0, LocalDateTime.now());
//         pendingSession.setEndTime(LocalDateTime.now().plusDays(1));
//     }

//     // TC1: Thành công
//     @Test
//     void placeBid_Successful() throws Exception {
//         String sessionId = "SS001";
//         double bidAmount = 150.0;

//         when(sessionDAO.getSessionById(connection, sessionId)).thenReturn(openSession);
//         when(bidDAO.getHighestBid(connection, sessionId)).thenReturn(null);
//         when(userDAO.freezeMoneyAtomic(connection, 2, bidAmount)).thenReturn(true);
//         when(userDAO.getUserById(connection, 2)).thenReturn(bidder);
//         when(bidDAO.addBid(eq(connection), eq(sessionId), any(Bid.class))).thenReturn(true);
//         when(sessionDAO.updateCurrentPrice(eq(connection), eq(sessionId), eq(bidAmount))).thenReturn(true);

//         boolean result = auctionService.placeBid(2, sessionId, bidAmount);
//         assertTrue(result);
//         verify(bidDAO).addBid(eq(connection), eq(sessionId), any(Bid.class));
//         verify(connection).commit();
//         verify(eventPublisher).notifyObservers(eq(sessionId), contains("NEW_BID"));
//     }

//     // TC2: Sai giá (thấp hơn giá hiện tại + bước giá)
//     @Test
//     void placeBid_InvalidPrice() throws Exception {
//         String sessionId = "SS001";
//         Bid currentHighest = new Bid(bidder, 100.0);
//         when(sessionDAO.getSessionById(connection, sessionId)).thenReturn(openSession);
//         when(bidDAO.getHighestBid(connection, sessionId)).thenReturn(currentHighest);
//         when(userDAO.freezeMoneyAtomic(connection, 2, 105.0)).thenReturn(true);
//         when(userDAO.refundMoneyAtomic(connection, 2, 105.0)).thenReturn(true);

//         boolean result = auctionService.placeBid(2, sessionId, 105.0);
//         assertFalse(result);
//         verify(bidDAO, never()).addBid(any(), any(), any());
//         verify(connection, never()).commit();
//         verify(connection).rollback();
//     }

//     // TC3: Seller tự đấu giá
//     @Test
//     void placeBid_SellerCurrentlyAllowed() throws Exception {
//         when(sessionDAO.getSessionById(connection, "SS001")).thenReturn(openSession);
//         when(userDAO.freezeMoneyAtomic(connection, 1, 150.0)).thenReturn(true);
//         when(userDAO.getUserById(connection, 1)).thenReturn(mock(User.class)); // seller cũng là user
//         when(bidDAO.getHighestBid(connection, "SS001")).thenReturn(null);
//         when(bidDAO.addBid(any(), any(), any())).thenReturn(true);
//         when(sessionDAO.updateCurrentPrice(any(), anyString(), anyDouble())).thenReturn(true);

//         // Hiện tại seller vẫn đặt được giá
//         boolean result = auctionService.placeBid(1, "SS001", 150.0);
//         assertTrue(result, "Seller hiện chưa bị chặn, cần sửa logic placeBid");
//     }

//     // TC4: Hết tiền (freeze thất bại)
//     @Test
//     void placeBid_InsufficientBalance() throws Exception {
//         when(sessionDAO.getSessionById(connection, "SS001")).thenReturn(openSession);
//         when(userDAO.freezeMoneyAtomic(connection, 3, 200.0)).thenReturn(false);

//         boolean result = auctionService.placeBid(3, "SS001", 200.0);
//         assertFalse(result);
//         verify(bidDAO, never()).addBid(any(), any(), any());
//         verify(connection).rollback();
//     }

//     // TC5: Phiên đã đóng (ClosedState) không cho đặt giá
//     @Test
//     void placeBid_ClosedSessionRejected() throws Exception {
//         when(sessionDAO.getSessionById(connection, "SS002")).thenReturn(closedSession);
//         when(userDAO.freezeMoneyAtomic(connection, 2, 150.0)).thenReturn(true);
//         when(userDAO.refundMoneyAtomic(connection, 2, 150.0)).thenReturn(true);

//         boolean result = auctionService.placeBid(2, "SS002", 150.0);
//         assertFalse(result);
//         verify(bidDAO, never()).addBid(any(), any(), any());
//         verify(connection).rollback();
//     }

//     @Test
//     void placeBid_PendingSessionRejected() throws Exception {
//         when(sessionDAO.getSessionById(connection, "SS003")).thenReturn(pendingSession);
//         when(userDAO.freezeMoneyAtomic(connection, 2, 150.0)).thenReturn(true);
//         when(userDAO.refundMoneyAtomic(connection, 2, 150.0)).thenReturn(true);

//         boolean result = auctionService.placeBid(2, "SS003", 150.0);
//         assertFalse(result);
//         verify(bidDAO, never()).addBid(any(), any(), any());
//         verify(connection).rollback();
//     }

//     // TC6: Anti-sniping
//     @Test
//     void placeBid_AntiSniping_NotTriggered() throws Exception {
//         openSession.setEndTime(LocalDateTime.now().plusSeconds(181));
//         when(sessionDAO.getSessionById(connection, "SS004")).thenReturn(openSession);
//         when(bidDAO.getHighestBid(connection, "SS004")).thenReturn(null);
//         when(userDAO.freezeMoneyAtomic(connection, 2, 150.0)).thenReturn(true);
//         when(userDAO.getUserById(connection, 2)).thenReturn(bidder);
//         when(bidDAO.addBid(any(), any(), any())).thenReturn(true);
//         when(sessionDAO.updateCurrentPrice(any(), anyString(), anyDouble())).thenReturn(true);

//         LocalDateTime before = openSession.getEndTime();
//         auctionService.placeBid(2, "SS004", 150.0);
//         assertEquals(before, openSession.getEndTime());
//         verify(sessionDAO, never()).updateEndTime(any(), any(), any());
//     }

//     @Test
//     void placeBid_AntiSniping_Extends() throws Exception {
//         openSession.setEndTime(LocalDateTime.now().plusSeconds(90));
//         when(sessionDAO.getSessionById(connection, "SS005")).thenReturn(openSession);
//         when(bidDAO.getHighestBid(connection, "SS005")).thenReturn(null);
//         when(userDAO.freezeMoneyAtomic(connection, 2, 150.0)).thenReturn(true);
//         when(userDAO.getUserById(connection, 2)).thenReturn(bidder);
//         when(bidDAO.addBid(any(), any(), any())).thenReturn(true);
//         when(sessionDAO.updateCurrentPrice(any(), anyString(), anyDouble())).thenReturn(true);

//         LocalDateTime before = openSession.getEndTime();
//         auctionService.placeBid(2, "SS005", 150.0);
//         assertTrue(openSession.getEndTime().isAfter(before.plusMinutes(2)));
//         verify(sessionDAO).updateEndTime(eq(connection), eq("SS005"), any());
//     }

//     // TC7: Rollback khi lỗi SQL
//     @Test
//     void placeBid_RollbackOnError() throws Exception {
//         String sessionId = "SS006";
//         when(sessionDAO.getSessionById(connection, sessionId)).thenReturn(openSession);
//         when(bidDAO.getHighestBid(connection, sessionId)).thenReturn(null);
//         when(userDAO.freezeMoneyAtomic(connection, 2, 150.0)).thenReturn(true);
//         when(userDAO.getUserById(connection, 2)).thenReturn(bidder);
//         doThrow(new SQLException("DB error")).when(bidDAO).addBid(any(), any(), any());

//         boolean result = auctionService.placeBid(2, sessionId, 150.0);
//         assertFalse(result);
//         verify(connection, never()).commit();
//     }
// }