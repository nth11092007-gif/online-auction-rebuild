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
import server.AuctionFeedServer;
import utils.DBConnection;

public class AuctionService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuctionService.class);
    
    final private UserDAO userDAO = new UserDAOImpl();
    final private BidDAO bidDAO = new BidDAOImpl();
    final private AuctionSessionDAO sessionDAO = new AuctionSessionDAOImpl();
    private AuctionFeedServer feedServer;

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

            // Kiểm tra người bán không được tự đấu giá
            if (session.getSeller().getID() == currentUserId) {
                logger.warn("Người bán không được phép đấu giá sản phẩm của chính mình.");
                return false;
            }

            Bid highestBid = bidDAO.getHighestBid(conn, sessionId);
            
            double minValidBid;
            if (highestBid == null) {
                minValidBid = session.getStartingPrice();
            } else {
                minValidBid = highestBid.getAmount() + session.getIncrementStep();
            }

            if (bidAmount < minValidBid) {
                logger.warn("Giá đặt {} không hợp lệ cho session {}. Phải >= {}", bidAmount, sessionId, minValidBid);
                conn.rollback();
                return false;
            }

            boolean isDeducted = userDAO.freezeMoneyAtomic(conn, currentUserId, bidAmount);
            if (!isDeducted) {
                logger.warn("Số dư không đủ để đặt giá {} cho user {} trong session {}", bidAmount, currentUserId, sessionId);
                conn.rollback();
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

            Bid newBid = new Bid(userDAO.getUserById(conn, currentUserId), bidAmount);
            bidDAO.addBid(conn, sessionId, newBid);

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
    return false;
    }
}