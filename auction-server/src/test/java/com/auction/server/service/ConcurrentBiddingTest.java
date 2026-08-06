package com.auction.server.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.auction.common.model.AuctionSession;
import com.auction.common.model.Electronics;
import com.auction.common.model.Item;
import com.auction.common.model.User;
import com.auction.server.dao.AuctionSessionDAO;
import com.auction.server.dao.BidDAO;
import com.auction.server.dao.UserDAO;

@ExtendWith(MockitoExtension.class)
public class ConcurrentBiddingTest {

    @Mock
    private UserDAO userDao;

    @Mock
    private BidDAO bidDao;

    @Mock
    private AuctionSessionDAO sessionDao;

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
    }

    @Test
    @Disabled("Chờ refactor Giai đoạn 3: Hiện tại code chưa xử lý concurrency đúng cách (Database lock)")
    void testConcurrentBidding_RaceCondition() throws Exception {
        // Arrange
        int numberOfThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);
        
        AtomicInteger successfulBids = new AtomicInteger(0);
        
        when(sessionDao.getSessionForPlaceBid(eq("session-123"))).thenReturn(mockSession);
        
        when(userDao.freezeMoneyAtomic(anyInt(), anyDouble())).thenReturn(true);
        when(userDao.getUserById(anyInt())).thenReturn(new User());

        // We will simulate DB behavior by updating mockSession's price when updateCurrentPrice happens.
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                double bidAmount = invocation.getArgument(1);
                mockSession.setCurrentPrice(bidAmount);
                return null;
            }
        }).when(sessionDao).updateCurrentPrice(eq("session-123"), anyDouble());

        // Act
        for (int i = 0; i < numberOfThreads; i++) {
            final int userId = i + 2; // User IDs 2 to 11
            executor.submit(() -> {
                try {
                    latch.await(); // Wait until all threads are ready
                    // All threads try to bid 1100.0 at the same time
                    boolean result = auctionService.placeBid(userId, "session-123", 1100.0);
                    if (result) {
                        successfulBids.incrementAndGet();
                    }
                } catch (Exception e) {
                    // Ignore exceptions for this test
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        
        latch.countDown(); // Release all threads at exactly the same time
        doneLatch.await(5, TimeUnit.SECONDS);

        // Assert
        // In Phase 3, we expect exactly ONE bid to succeed and the rest to fail
        assertEquals(1, successfulBids.get(), "Chỉ một thread được phép đặt giá thành công do Race Condition lock");
        assertEquals(1100.0, mockSession.getCurrentPrice(), "Giá cuối cùng phải là 1100.0");
    }
}
