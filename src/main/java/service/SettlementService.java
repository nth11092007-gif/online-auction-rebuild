package service;

import java.sql.Connection;
import java.sql.SQLException;

import dao.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.AuctionSession;
import model.Bid;
import utils.DBConnection;

import javax.sql.DataSource;

public class SettlementService {
    private final DataSource dataSource;
    private static final Logger logger = LoggerFactory.getLogger(SettlementService.class);
    private final AuctionSessionDAO sessionDAO;
    private final BidDAO bidDAO;
    private final UserDAO userDAO;
    private final ItemDAO itemDAO;

    public SettlementService() {
        this(DBConnection.getDataSource(), new AuctionSessionDAOImpl(),
                new BidDAOImpl(), new UserDAOImpl(), new ItemDAOImpl());
    }
    public SettlementService(DataSource dataSource, AuctionSessionDAO sessionDAO,
                             BidDAO bidDAO, UserDAO userDAO, ItemDAO itemDAO) {
        this.dataSource = dataSource;
        this.sessionDAO = sessionDAO;
        this.bidDAO = bidDAO;
        this.userDAO = userDAO;
        this.itemDAO = itemDAO;
    }

    /**
     * Hàm xử lý kết thúc phiên đấu giá
     */
    public boolean settleAuction(String sessionId) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            AuctionSession session = sessionDAO.getSessionById(conn, sessionId);
            if (session == null || session.getStatus() != AuctionSession.Status.OPEN) {
                logger.warn("Phiên đấu giá {} không tồn tại hoặc không ở trạng thái OPEN", sessionId);
                return false;
            }

            Bid winningBid = bidDAO.getHighestBid(conn, sessionId);

            if (winningBid != null) {
                int buyerId = winningBid.getBidder().getID();
                int sellerId = session.getSeller().getID();
                double finalPrice = winningBid.getAmount();

                userDAO.deductFrozenMoneyAtomic(conn, buyerId, finalPrice);
                userDAO.addMoneyAtomic(conn, sellerId, finalPrice);
                itemDAO.updateItemOwner(conn, session.getItem().getItemID(), buyerId);

                logger.info("Đấu giá thành công! Phiên {}, giá {}, người mua ID: {}", sessionId, finalPrice, buyerId);
            } else {
                logger.info("Phiên đấu giá {} kết thúc. Không có ai đặt giá.", sessionId);
            }

            sessionDAO.updateSessionStatusAtomic(conn, sessionId, AuctionSession.Status.CLOSED);
            conn.commit();
            return true;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.warn("Đã rollback transaction cho session {}", sessionId);
                } catch (SQLException ex) {
                    logger.error("Lỗi khi rollback transaction: {}", ex.getMessage(), ex);
                }
            }
            logger.error("Lỗi hệ thống khi chốt phiên {}: {}", sessionId, e.getMessage(), e);
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Lỗi khi đóng kết nối: {}", e.getMessage(), e);
                }
            }
        }
    }
}