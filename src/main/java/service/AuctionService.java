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
import model.Bidder;
import server.AuctionFeedServer;
import utils.DBConnection;

public class AuctionService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuctionService.class);
    
    final private UserDAO userDAO;
    final private BidDAO bidDAO;
    final private AuctionSessionDAO sessionDAO;
    private AuctionFeedServer feedServer;
    private final DataSource dataSource;

    public AuctionService() {
        this(DBConnection.getDataSource(), new UserDAOImpl(), new BidDAOImpl(), new AuctionSessionDAOImpl());
    }
    public AuctionService(DataSource dataSource, UserDAO userDAO, BidDAO bidDAO, AuctionSessionDAO sessionDAO) {
        this.dataSource = dataSource;
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

    public synchronized boolean placeBid(int currentUserId, String sessionId, double bidAmount) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            
            AuctionSession session = sessionDAO.getSessionById(conn, sessionId);
            if (session == null) {
                logger.warn("Phiên đấu giá {} không tồn tại", sessionId);
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

            // Kiểm tra số dư người dùng có đủ để đặt giá (số tiền cần freeze)
            if (bidder == null || bidder.getBalance() < bidAmount) {
                logger.warn("User {} không đủ số dư để đặt giá {}", currentUserId, bidAmount);
                return false;
            }

            // Logic Anti-sniping
            long timeDiff = ChronoUnit.MILLIS.between(LocalDateTime.now(), session.getEndTime());

            if (timeDiff > 0 && timeDiff < SNIPING_THRESHOLD_MS) {
                LocalDateTime newEndTime = session.getEndTime().plusMinutes(EXTENSION_TIME_MINUTES);
                session.setEndTime(newEndTime);

                sessionDAO.updateEndTime(conn, sessionId, Timestamp.valueOf(newEndTime));

                logger.info("Anti-sniping: Gia hạn phiên {} đến {}", sessionId, newEndTime);
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
        // Lưu bid vào DB
        bidDAO.addBid(conn, sessionId, newBid);

        // Cập nhật session (currentPrice và có thể bid count) vào DB
        sessionDAO.updateCurrentPrice(conn, sessionId, bidAmount); // Bạn cần thêm method này
        conn.commit();

            if (feedServer != null) {
                String msg = String.format("{\"type\":\"NEW_BID\",\"sessionId\":\"%s\",\"newPrice\":%f,\"endTime\":\"%s\"}",
                        sessionId, bidAmount, session.getEndTime().toString());
                feedServer.notifyObservers(sessionId, msg);
            }

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