package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.AuctionSession;
import model.Bid;
import model.Items;
import model.User;
import utils.DBConnection;

public class AuctionSessionDAOImpl implements AuctionSessionDAO {

    private static final Logger logger = LoggerFactory.getLogger(AuctionSessionDAOImpl.class);
    private final UserDAO userDAO = new UserDAOImpl();
    private final ItemDAO itemDAO = new ItemDAOImpl();
    private final BidDAO bidDAO = new BidDAOImpl();

    // =========================================================================
    // 1. TẠO PHIÊN ĐẤU GIÁ
    // =========================================================================
    
    @Override
    public boolean createSession(Connection conn, AuctionSession session, int itemId) throws SQLException {
        String sql = "INSERT INTO auction_sessions (session_id, owner_id, item_id, starting_price, step_price, duration_days, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, session.getSessionID());
            pstmt.setInt(2, session.getSeller().getID());
            pstmt.setInt(3, itemId);
            pstmt.setDouble(4, session.getStartingPrice());
            pstmt.setDouble(5, session.getIncrementStep());

            int durationDays = (int) Duration.between(session.getStartTime(), session.getEndTime()).toDays();
            pstmt.setInt(6, durationDays > 0 ? durationDays : 3);
            pstmt.setString(7, session.status.name());

            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean createSession(AuctionSession session, int itemId) {
        try (Connection conn = DBConnection.getConnection()) {
            return createSession(conn, session, itemId);
        } catch (SQLException e) {
            logger.error("❌ Lỗi khi tạo phiên đấu giá: {}", e.getMessage(), e);
        }
        return false;
    }

    // =========================================================================
    // 2. LẤY THÔNG TIN MỘT PHIÊN ĐẤU GIÁ
    // =========================================================================
    @Override
    public AuctionSession getSessionById(Connection conn, String sessionId) throws SQLException {
        String sql = "SELECT * FROM auction_sessions WHERE session_id = ? FOR UPDATE";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int ownerId = rs.getInt("owner_id");
                    User seller = userDAO.getUserById(conn, ownerId);
                    Items item = itemDAO.getItemById(conn, rs.getInt("item_id"));
                    double startingPrice = rs.getDouble("starting_price");
                    double stepPrice = rs.getDouble("step_price");
                    int durationDays = rs.getInt("duration_days");

                    AuctionSession session = new AuctionSession(seller, item, startingPrice, stepPrice, durationDays);
                    session.status = AuctionSession.Status.valueOf(rs.getString("status"));

                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        session.setStartTime(createdAt.toLocalDateTime());
                        session.setEndTime(session.getStartTime().plusDays(durationDays));
                    }

                    List<Bid> bids = bidDAO.getBidsBySession(conn, sessionId);
                    if (bids != null && !bids.isEmpty()) {
                        Bid highestBid = bids.get(0);
                        session.setCurrentPrice(highestBid.getAmount());
                        session.setHighestBidder(highestBid.getBidder());
                    } else {
                        session.setCurrentPrice(startingPrice);
                    }
                    return session;
                }
            }
        } catch (SQLException e) {
            logger.error("Lỗi khi lấy phiên đấu giá theo ID {}: {}", sessionId, e.getMessage(), e);
        }
        return null;
    }

    @Override
    public AuctionSession getSessionById(String sessionId) {
        try (Connection conn = DBConnection.getConnection()) {
            return getSessionById(conn, sessionId);
        } catch (SQLException e) {
            logger.error("Lỗi kết nối khi lấy session {}: {}", sessionId, e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<AuctionSession> getAllSessions() {
        List<AuctionSession> list = new ArrayList<>();
        String sql = "SELECT session_id FROM auction_sessions";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                AuctionSession session = getSessionById(rs.getString("session_id"));
                if (session != null) {
                    list.add(session);
                }
            }
        } catch (SQLException e) {
            logger.error("Lỗi khi lấy danh sách tất cả phiên đấu giá: {}", e.getMessage(), e);
        }
        return list;
    }

    // =========================================================================
    // 3. CẬP NHẬT TRẠNG THÁI PHIÊN
    // =========================================================================
    @Override
    public boolean updateSessionStatusAtomic(Connection conn, String sessionId, AuctionSession.Status status) throws SQLException {
        String sql = "UPDATE auction_sessions SET status = ? WHERE session_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setString(2, sessionId);
            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateSessionStatusAtomic(String sessionId, AuctionSession.Status status) {
        try (Connection conn = DBConnection.getConnection()) {
            return updateSessionStatusAtomic(conn, sessionId, status);
        } catch (SQLException e) {
            logger.error("Lỗi khi cập nhật trạng thái phiên {}: {}", sessionId, e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean updateEndTime(Connection conn, String sessionId, Timestamp newEndTime) throws SQLException {
        String sql = "UPDATE auction_sessions SET end_time = ? WHERE session_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, newEndTime);
            pstmt.setString(2, sessionId);
            return pstmt.executeUpdate() > 0;
        }
    }
}