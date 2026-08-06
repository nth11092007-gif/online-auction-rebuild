package com.auction.server.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.auction.common.model.AuctionSession;
import com.auction.common.model.Bid;

import com.auction.common.model.Item;
import com.auction.common.model.User;
import com.auction.common.model.Electronics;
import com.auction.server.dao.AuctionSessionDAO;
import com.auction.server.dao.BidDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.exception.AuctionClosedException;
import com.auction.server.exception.InvalidBidException;

@ExtendWith(MockitoExtension.class)
public class AuctionServiceTest {



    @Mock
    private UserDAO userDao;

    @Mock
    private BidDAO bidDao;

    @Mock
    private AuctionSessionDAO sessionDao;

    @Mock
    private AuctionEventPublisher eventPublisher;

    @InjectMocks
    private AuctionService auctionService;

    private AuctionSession mockSession;
    private User mockUser;

    @BeforeEach
    void setUp() throws Exception {
        
        mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername("testuser");

        Item mockItem = new Electronics(1, "Laptop", "admin", 1000.0, "Desc", 12, "Dell");
        mockSession = new AuctionSession(mockUser, mockItem, 1000.0, 100.0, LocalDateTime.now().minusDays(1), "session-123");
        mockSession.setEndTime(LocalDateTime.now().plusDays(1));
        mockSession.setStatus(AuctionSession.Status.OPEN);
        
        auctionService.setEventPublisher(eventPublisher);
    }

    @Test
    void testPlaceBid_Success() throws Exception {
        // Arrange
        when(sessionDao.getSessionForPlaceBid("session-123")).thenReturn(mockSession);
        when(userDao.freezeMoneyAtomic(2, 1200.0)).thenReturn(true);
        when(userDao.getUserById(2)).thenReturn(new User());

        // Act
        boolean result = auctionService.placeBid(2, "session-123", 1200.0);

        // Assert
        assertTrue(result);
        verify(bidDao, times(1)).addBid(eq("session-123"), any(Bid.class));
        verify(sessionDao, times(1)).updateCurrentPrice("session-123", 1200.0);
        verify(eventPublisher, times(1)).notifyObservers(eq("session-123"), anyString());
    }

    @Test
    @Disabled("Chờ refactor Giai đoạn 3: Ném Exception thay vì trả về false")
    void testPlaceBid_Fail_InvalidAmount() throws Exception {
        // Arrange
        when(sessionDao.getSessionForPlaceBid("session-123")).thenReturn(mockSession);
        when(userDao.freezeMoneyAtomic(2, 1050.0)).thenReturn(true);
        // Current price is 1000, step is 100 => min valid is 1100. Bid is 1050 (invalid)

        // Act & Assert
        assertThrows(InvalidBidException.class, () -> {
            auctionService.placeBid(2, "session-123", 1050.0);
        });
    }

    @Test
    @Disabled("Chờ refactor Giai đoạn 3: Ném Exception thay vì trả về false")
    void testPlaceBid_Fail_AuctionClosed() throws Exception {
        // Arrange
        mockSession.setStatus(AuctionSession.Status.CLOSED);
        when(sessionDao.getSessionForPlaceBid("session-123")).thenReturn(mockSession);
        when(userDao.freezeMoneyAtomic(2, 1200.0)).thenReturn(true);

        // Act & Assert
        assertThrows(AuctionClosedException.class, () -> {
            auctionService.placeBid(2, "session-123", 1200.0);
        });
    }

    @Test
    void testPlaceBid_AntiSniping() throws Exception {
        // Arrange
        LocalDateTime nearEnd = LocalDateTime.now().plusMinutes(2); // Dưới ngưỡng 3 phút
        mockSession.setEndTime(nearEnd);
        
        when(sessionDao.getSessionForPlaceBid("session-123")).thenReturn(mockSession);
        when(userDao.freezeMoneyAtomic(2, 1200.0)).thenReturn(true);
        when(userDao.getUserById(2)).thenReturn(new User());

        // Act
        boolean result = auctionService.placeBid(2, "session-123", 1200.0);

        // Assert
        assertTrue(result);
        // Verify updateEndTime was called with extended time
        verify(sessionDao, times(1)).updateEndTime(eq("session-123"), any(Timestamp.class));
        // Check local model is updated
        assertTrue(mockSession.getEndTime().isAfter(nearEnd)); 
    }

    @Test
    void testPlaceBid_RefundPreviousBidder() throws Exception {
        // Arrange
        User prevUser = new User();
        prevUser.setId(3);
        prevUser.setUsername("prev");
        Bid prevBid = new Bid(prevUser, 1100.0);
        mockSession.addBid(prevBid); // Adds to highest bidder (since it's the only one)
        mockSession.setCurrentPrice(1100.0);
        
        when(sessionDao.getSessionForPlaceBid("session-123")).thenReturn(mockSession);
        when(userDao.freezeMoneyAtomic(2, 1300.0)).thenReturn(true);
        when(userDao.getUserById(2)).thenReturn(new User());

        // Act
        boolean result = auctionService.placeBid(2, "session-123", 1300.0);

        // Assert
        assertTrue(result);
        // Verify refund was called for user 3
        verify(userDao, times(1)).refundMoneyAtomic(3, 1100.0);
    }
}
