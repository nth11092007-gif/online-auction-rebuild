package service;

import dao.*;
import model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SettlementServiceTest {

    @Mock private DataSource dataSource;
    @Mock private Connection connection;
    @Mock private AuctionSessionDAO sessionDAO;
    @Mock private BidDAO bidDAO;
    @Mock private UserDAO userDAO;
    @Mock private ItemDAO itemDAO;

    private SettlementService settlementService;
    private AuctionSession openSession, closedSession;
    private Seller sellerMock;
    private Item genericItemMock;

    @BeforeEach
    void setUp() {
        settlementService = new SettlementService(dataSource, sessionDAO, bidDAO, userDAO, itemDAO);

        sellerMock = mock(Seller.class);
        when(sellerMock.getId()).thenReturn(1);
        genericItemMock = mock(Item.class);

        openSession = new AuctionSession(sellerMock, genericItemMock, 100.0, 10.0, LocalDateTime.now());
        openSession.startSession(1); // OPEN

        closedSession = new AuctionSession(sellerMock, genericItemMock, 100.0, 10.0, LocalDateTime.now());
        closedSession.startSession(1);
        closedSession.endSession(); // CLOSED
    }

    @Test
    void settleAuction_Success() throws Exception {
        String sessionId = "SS001";
        User buyer = new User(2, "buyer", "p", "B", "b@t", "0", User.Role.USER, 500, 100);
        Bid winningBid = new Bid(buyer, 250.0);

        when(dataSource.getConnection()).thenReturn(connection);

        Item successItem = mock(Item.class);
        when(successItem.getItemId()).thenReturn(10);
        openSession = new AuctionSession(sellerMock, successItem, 100.0, 10.0, LocalDateTime.now());
        openSession.startSession(1);

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