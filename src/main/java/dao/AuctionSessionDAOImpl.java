package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
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
        // Đảm bảo session có startTime và endTime
        LocalDateTime startTime = session.getStartTime();
        LocalDateTime endTime = session.getEndTime();
        if (startTime == null) {
            startTime = LocalDateTime.now();
            session.setStartTime(startTime);
        }
        if (endTime == null) {
            // Mặc định 3 ngày nếu chưa set
            endTime = startTime.plusDays(3);
            session.setEndTime(endTime);
        }
        int durationDays = (int) Duration.between(startTime, endTime).toDays();
        if (durationDays < 0) durationDays = 0;

        String sql = "INSERT INTO auction_sessions (session_id, owner_id, item_id, starting_price, step_price, "
                + "start_time, end_time, duration_days, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, session.getSessionID());
            pstmt.setInt(2, session.getSeller().getID());
            pstmt.setInt(3, itemId);
            pstmt.setDouble(4, session.getStartingPrice());
            pstmt.setDouble(5, session.getIncrementStep());
            pstmt.setTimestamp(6, Timestamp.valueOf(startTime));
            pstmt.setTimestamp(7, Timestamp.valueOf(endTime));
            pstmt.setInt(8, durationDays);
            pstmt.setString(9, session.getStatus().name());
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
                    User seller = userDAO.getUserById(conn, rs.getInt("owner_id"));
                    Items item = itemDAO.getItemById(conn, rs.getInt("item_id"));
                    double startingPrice = rs.getDouble("starting_price");
                    double stepPrice = rs.getDouble("step_price");
                    Timestamp startTs = rs.getTimestamp("start_time");
                    Timestamp endTs = rs.getTimestamp("end_time");
                    LocalDateTime startTime = startTs != null ? startTs.toLocalDateTime() : null;
                    LocalDateTime endTime = endTs != null ? endTs.toLocalDateTime() : null;
                    int extensionCount = rs.getInt("extension_count"); // nếu cột tồn tại

                    // Tạo session (constructor có startTime)
                    AuctionSession session = new AuctionSession(seller, item, startingPrice, stepPrice, startTime, sessionId);
                    session.setEndTime(endTime);
                    session.setStatus(AuctionSession.Status.valueOf(rs.getString("status")));
                    // Nếu model có extensionCount, set vào
                    // session.setExtensionCount(extensionCount);

                    // Lấy currentPrice và highestBidder từ bids
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
    public List<AuctionSession> getAllSessions(Connection conn) throws SQLException {
        List<AuctionSession> list = new ArrayList<>();
        String sql = "SELECT session_id FROM auction_sessions";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                AuctionSession session = getSessionById(conn, rs.getString("session_id"));
                if (session != null) list.add(session);
            }
        }
        return list;
    }

    @Override
    public List<AuctionSession> getAllSessions() {
        try (Connection conn = DBConnection.getConnection()) {
            return getAllSessions(conn);
        } catch (SQLException e) {
            logger.error("Lỗi khi lấy danh sách tất cả phiên: {}", e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    @Override
    public List<AuctionSession> getSessionsStartBefore(Connection conn, LocalDateTime time, AuctionSession.Status status) throws SQLException {
        List<AuctionSession> list = new ArrayList<>();
        String sql = "SELECT session_id FROM auction_sessions WHERE start_time <= ? AND status = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(time));
            pstmt.setString(2, status.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AuctionSession session = getSessionById(conn, rs.getString("session_id"));
                    if (session != null) list.add(session);
                }
            }
        }
        return list;
    }

    @Override
    public List<AuctionSession> getSessionsStartBefore(LocalDateTime time, AuctionSession.Status status) {
        try (Connection conn = DBConnection.getConnection()) {
            return getSessionsStartBefore(conn, time, status);
        } catch (SQLException e) {
            logger.error("Lỗi lấy phiên startBefore: {}", e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    @Override
    public List<AuctionSession> getSessionsEndBefore(Connection conn, LocalDateTime time, AuctionSession.Status status) throws SQLException {
        List<AuctionSession> list = new ArrayList<>();
        String sql = "SELECT session_id FROM auction_sessions WHERE end_time < ? AND status = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(time));
            pstmt.setString(2, status.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AuctionSession session = getSessionById(conn, rs.getString("session_id"));
                    if (session != null) list.add(session);
                }
            }
        }
        return list;
    }

    @Override
    public List<AuctionSession> getSessionsEndBefore(LocalDateTime time, AuctionSession.Status status) {
        try (Connection conn = DBConnection.getConnection()) {
            return getSessionsEndBefore(conn, time, status);
        } catch (SQLException e) {
            logger.error("Lỗi lấy phiên endBefore: {}", e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    @Override
    public AuctionSession getSessionForUpdate(Connection conn, String sessionId) throws SQLException {
        // Sử dụng cùng logic với getSessionById nhưng có FOR UPDATE trong SQL để khoá dòng
        String sql = "SELECT * FROM auction_sessions WHERE session_id = ? FOR UPDATE";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User seller = userDAO.getUserById(conn, rs.getInt("owner_id"));
                    Items item = itemDAO.getItemById(conn, rs.getInt("item_id"));
                    double startingPrice = rs.getDouble("starting_price");
                    double stepPrice = rs.getDouble("step_price");
                    Timestamp startTs = rs.getTimestamp("start_time");
                    Timestamp endTs = rs.getTimestamp("end_time");
                    LocalDateTime startTime = startTs != null ? startTs.toLocalDateTime() : null;
                    LocalDateTime endTime = endTs != null ? endTs.toLocalDateTime() : null;

                    AuctionSession session = new AuctionSession(seller, item, startingPrice, stepPrice, startTime, sessionId);
                    session.setEndTime(endTime);
                    session.setStatus(AuctionSession.Status.valueOf(rs.getString("status")));

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
        }
        catch (SQLException e) {
            logger.error("Lỗi khi lấy phiên đấu giá có khoá theo ID {}: {}", sessionId, e.getMessage(), e);
        }
        return null;
    }
    // =========================================================================
    // 3. CẬP NHẬT TRẠNG THÁI PHIÊN & GIA HẠN
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
            logger.error("Lỗi cập nhật trạng thái phiên {}: {}", sessionId, e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean updateCurrentPrice(Connection conn, String sessionId, double newPrice) {
        String sql = "UPDATE auction_sessions SET current_price = ? WHERE session_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newPrice);
            pstmt.setString(2, sessionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Lỗi cập nhật giá hiện tại phiên {}: {}", sessionId, e.getMessage(), e);
        }
        return false;
    }
}