package com.auction.server.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.auction.common.model.AuctionSession;
import com.auction.common.model.Electronics;
import com.auction.common.model.Item;
import com.auction.common.model.User;
import com.auction.server.dao.AuctionSessionDAO;
import com.auction.server.dao.BidDAO;
import com.auction.server.dao.UserDAO;

@ExtendWith(MockitoExtension.class)
public class AuctionRealtimeUpdateTest {

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

    @BeforeEach
    void setUp() throws Exception {
        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername("testuser");

        Item mockItem = new Electronics(1, "Laptop", "admin", 1000.0, "Desc", 12, "Dell");
        mockSession = new AuctionSession(mockUser, mockItem, 1000.0, 100.0, LocalDateTime.now().minusDays(1), "session-123");
        mockSession.setEndTime(LocalDateTime.now().plusDays(1));
        mockSession.setStatus(AuctionSession.Status.OPEN);
        
        auctionService.setEventPublisher(eventPublisher);
    }

    @Test
    void testPlaceBid_ShouldBroadcastToWebSocket() throws Exception {
        // Arrange
        when(sessionDao.getSessionForPlaceBid("session-123")).thenReturn(mockSession);
        when(userDao.freezeMoneyAtomic(2, 1200.0)).thenReturn(true);
        when(userDao.getUserById(2)).thenReturn(new User());

        // Act
        boolean result = auctionService.placeBid(2, "session-123", 1200.0);

        // Assert
        assertTrue(result, "Place bid should be successful");
        
        // Verify that the event publisher (WebSocket broadcaster) was called exactly once 
        // with the correct session ID and a payload containing "NEW_BID"
        verify(eventPublisher, times(1)).notifyObservers(
            eq("session-123"), 
            argThat(msg -> msg.contains("NEW_BID") && msg.contains("1200"))
        );
    }
}
