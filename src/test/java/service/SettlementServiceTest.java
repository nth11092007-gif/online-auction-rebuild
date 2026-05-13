package service;

import dao.*;
import model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock private DataSource dataSource;
    @Mock private Connection connection;
    @Mock private AuctionSessionDAO sessionDAO;
    @Mock private BidDAO bidDAO;
    @Mock private UserDAO userDAO;
    @Mock private ItemDAO itemDAO;

    private SettlementService settlementService;
    private AuctionSession openSession, closedSession;

    @BeforeEach
    void setUp() {
        settlementService = new SettlementService(dataSource, sessionDAO, bidDAO, userDAO, itemDAO);

        User seller = new User(1, "sell", "p", "S", "s@t", "0", User.Role.USER, 0, 0);
        // item mock
        openSession = new AuctionSession(seller, null, 100, 10, LocalDateTime.now());
        openSession.status = AuctionSession.Status.OPEN;

        closedSession = new AuctionSession(seller, null, 100, 10, LocalDateTime.now());
        closedSession.status = AuctionSession.Status.CLOSED;
    }

    @Test
    void settleAuction_Success() throws Exception {
        String sessionId = "SS001";
        User buyer = new User(2, "buyer", "p", "B", "b@t", "0", User.Role.USER, 500, 100);
        Bid winningBid = new Bid(buyer, 250.0);

        // Stub DataSource
        when(dataSource.getConnection()).thenReturn(connection);

        // Tạo item mock riêng cho success
        Items item = mock(Items.class);
        when(item.getItemID()).thenReturn(10);
        openSession = new AuctionSession(openSession.getSeller(), item, 100, 10, LocalDateTime.now());
        openSession.status = AuctionSession.Status.OPEN;

        when(sessionDAO.getSessionById(connection, sessionId)).thenReturn(openSession);
        when(bidDAO.getHighestBid(connection, sessionId)).thenReturn(winningBid);
        when(userDAO.deductFrozenMoneyAtomic(connection, 2, 250.0)).thenReturn(true);
        when(userDAO.addMoneyAtomic(connection, 1, 250.0)).thenReturn(true);
        when(itemDAO.updateItemOwner(connection, 10, 2)).thenReturn(true);
        when(sessionDAO.updateSessionStatusAtomic(connection, sessionId, AuctionSession.Status.CLOSED)).thenReturn(true);

        assertTrue(settlementService.settleAuction(sessionId));
        verify(connection).commit();
    }

    @Test
    void settleAuction_AlreadyClosed() throws Exception {
        String sessionId = "SS002";
        when(dataSource.getConnection()).thenReturn(connection);
        when(sessionDAO.getSessionById(connection, sessionId)).thenReturn(closedSession);

        assertFalse(settlementService.settleAuction(sessionId));
        verify(connection, never()).commit();
    }

    @Test
    void settleAuction_NotFound() throws Exception {
        String sessionId = "SS999";
        when(dataSource.getConnection()).thenReturn(connection);
        when(sessionDAO.getSessionById(connection, sessionId)).thenReturn(null);

        assertFalse(settlementService.settleAuction(sessionId));
        verify(connection, never()).commit();
    }
}