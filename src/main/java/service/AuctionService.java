package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dao.AuctionSessionDAO;
import dao.AuctionSessionDAOImpl;
import dao.BidDAO;
import dao.BidDAOImpl;
import dao.UserDAO;
import dao.UserDAOImpl;
import model.AuctionSession;
import model.Bid;
import server.AuctionFeedServer;
import utils.DBConnection;

public class AuctionService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuctionService.class);
    
    final private UserDAO userDAO;
    final private BidDAO bidDAO;
    final private AuctionSessionDAO sessionDAO;
    private AuctionFeedServer feedServer;
    private DataSource dataSource;
    public AuctionService() {
        this.userDAO = new UserDAOImpl();
        this.bidDAO = new BidDAOImpl();
        this.sessionDAO = new AuctionSessionDAOImpl();
        this.dataSource = DBConnection.getDataSource();
    }
    public AuctionService(UserDAO userDAO, BidDAO bidDAO, AuctionSessionDAO sessionDAO, DataSource dataSource) {
        this.userDAO = userDAO;
        this.bidDAO = bidDAO;
        this.sessionDAO = sessionDAO;
        this.dataSource = dataSource;
    }
    public AuctionService(UserDAO userDAO, BidDAO bidDAO, AuctionSessionDAO sessionDAO) {
        this.userDAO = userDAO;
        this.bidDAO = bidDAO;
        this.sessionDAO = sessionDAO;
    }
    // Cấu hình Anti-sniping
    private static final int SNIPING_THRESHOLD_MS = 3 * 60 * 1000;
    private static final int EXTENSION_TIME_MINUTES = 3;

    public void setFeedServer(AuctionFeedServer feedServer) {
        this.feedServer = feedServer;
    }

    public List<AuctionSession> getAllSessions() {
        return sessionDAO.getAllSessions();
    }

    public AuctionSession getSessionById(String sessionId) {
        return sessionDAO.getSessionById(sessionId);
    }

    public boolean placeBid(int currentUserId, String sessionId, double bidAmount) {
    Connection conn = null;
    try {
        conn = DBConnection.getConnection();
        conn.setAutoCommit(false);

        AuctionSession session = sessionDAO.getSessionById(conn, sessionId);
        if (session == null) {
            logger.warn("Session {} not found", sessionId);
            return false;
        }

        // 1. Freeze tiền NGAY LẬP TỨC (atomic trong DB)
        boolean isDeducted = userDAO.freezeMoneyAtomic(conn, currentUserId, bidAmount);
        if (!isDeducted) {
            logger.warn("Không thể đóng băng {} từ user {}", bidAmount, currentUserId);
            conn.rollback();
            return false;
        }

        // 2. Kiểm tra trạng thái session và các điều kiện khác
        if (!session.getState().canJoin()) {
            logger.warn("Session {} không ở trạng thái có thể đặt giá", sessionId);
            // Hoàn tiền vì đã freeze rồi
            userDAO.refundMoneyAtomic(conn, currentUserId, bidAmount);
            conn.rollback();
            return false;
        }

        Bid highestBid = bidDAO.getHighestBid(conn, sessionId);
        double minValidBid = (highestBid == null) ? session.getStartingPrice() 
                            : highestBid.getAmount() + session.getIncrementStep();
        if (bidAmount < minValidBid) {
            userDAO.refundMoneyAtomic(conn, currentUserId, bidAmount);
            conn.rollback();
            return false;
        }

        // 3. Hoàn tiền cho người bị vượt (nếu có) và thêm bid
        if (highestBid != null) {
            int previousUserId = highestBid.getBidder().getID();
            double previousAmount = highestBid.getAmount();
            userDAO.refundMoneyAtomic(conn, previousUserId, previousAmount);
        }

        Bid newBid = new Bid(userDAO.getUserById(conn, currentUserId), bidAmount);
        session.addBid(newBid);  // dùng State Pattern
        bidDAO.addBid(conn, sessionId, newBid);
        sessionDAO.updateCurrentPrice(conn, sessionId, bidAmount); // nếu cần

        // 4. Anti-sniping: nếu còn dưới threshold, gia hạn phiên
        long timeToEnd = ChronoUnit.MILLIS.between(LocalDateTime.now(), session.getEndTime());
        if (timeToEnd < SNIPING_THRESHOLD_MS) {
            LocalDateTime newEndTime = session.getEndTime().plusMinutes(EXTENSION_TIME_MINUTES);
            sessionDAO.updateEndTime(conn, sessionId, Timestamp.valueOf(newEndTime));
            feedServer.notifyObservers(sessionId, "Session extended by 3 minutes."); // thông báo cho client về việc gia hạn
        }
        conn.commit();
        return true;
    } catch (SQLException e) {
        logger.error("Lỗi khi đặt giá: user {}, session {}, amount {}: {}", currentUserId, sessionId, bidAmount, e.getMessage());
    } finally {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            logger.error("Lỗi khi đóng kết nối: {}", e.getMessage(), e);
        }
    }
    return false;
    }
}