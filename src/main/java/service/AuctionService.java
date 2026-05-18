package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

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
import model.Bidder;
import model.User;
import utils.DBConnection;

public class AuctionService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuctionService.class);
    
    final private UserDAO userDAO;
    final private BidDAO bidDAO;
    final private AuctionSessionDAO sessionDAO = new AuctionSessionDAOImpl();

    public AuctionService() {
        this.userDAO = new UserDAOImpl();
        this.bidDAO = new BidDAOImpl();
    }

    public List<AuctionSession> getAllSessions() {
        return sessionDAO.getAllSessions();
    }

    public AuctionSession getSessionById(String sessionId) {
        return sessionDAO.getSessionById(sessionId);
    }

    public User getUserById(int userId) {
        return userDAO.getUserById(userId);
    }

    public boolean placeBid(int currentUserId, String sessionId, double bidAmount) {
    Connection conn = null;
    try {
        conn = DBConnection.getConnection();
        conn.setAutoCommit(false);

        AuctionSession session = sessionDAO.getSessionForUpdate(conn, sessionId);
        if (session == null) {
            logger.warn("Phiên {} không tồn tại", sessionId);
            return false;
        }

        // Lấy user (để đóng băng tiền)
        Bidder bidder = userDAO.getUserForUpdate(conn, currentUserId);
        if (bidder == null) {
            logger.warn("Người dùng {} không tồn tại", currentUserId);
            return false;
        }

        // Đóng băng tiền trước (vì cần kiểm tra số dư)
        if (bidder.getBalance() < bidAmount) {
            logger.warn("Số dư không đủ");
            return false;
        }
        boolean deducted = userDAO.freezeMoneyAtomic(conn, currentUserId, bidAmount);
        if (!deducted) {
            logger.warn("Không thể đóng băng tiền");
            conn.rollback();
            return false;
        }

        // Tạo Bid mới
        Bid newBid = new Bid(bidder, bidAmount); // Constructor phù hợp

        // Gọi addBid trên session (session tự kiểm tra trạng thái & giá)
        if (!session.addBid(newBid)) {
            // Nếu không hợp lệ, hoàn tiền ngay và rollback
            userDAO.refundMoneyAtomic(conn, currentUserId, bidAmount);
            conn.rollback();
            logger.warn("Bid không hợp lệ: giá={} session={}", bidAmount, sessionId);
            return false;
        }

        // Nếu có người bị vượt, hoàn tiền cho họ (lấy highest bid trước khi add)
        // Cần lấy highest trước khi addBid vì addBid đã thay đổi lịch sử
        // Hoặc bạn có thể lấy highest từ DB trước khi gọi addBid:
        Bid previousHighest = bidDAO.getHighestBid(conn, sessionId);
        if (previousHighest != null) {
            int prevUserId = previousHighest.getBidder().getID();
            double prevAmount = previousHighest.getAmount();
            userDAO.refundMoneyAtomic(conn, prevUserId, prevAmount);
        }

        // Lưu bid vào DB
        bidDAO.addBid(conn, sessionId, newBid);

        // Cập nhật session (currentPrice và có thể bid count) vào DB
        sessionDAO.updateCurrentPrice(conn, sessionId, bidAmount); // Bạn cần thêm method này

        conn.commit();
        logger.info("Đặt giá thành công: user {}, session {}, amount {}", currentUserId, sessionId, bidAmount);
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
    logger.error("Lỗi chưa xác định.");
    return false;
    }
}