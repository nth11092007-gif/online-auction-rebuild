package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.Bid;
import model.User;
import utils.DBConnection;

public class BidDAOImpl implements BidDAO {

    private static final Logger logger = LoggerFactory.getLogger(BidDAOImpl.class);
    private final UserDAO userDAO = new UserDAOImpl();

    // =========================================================================
    // 1. LẤY GIÁ CAO NHẤT
    // =========================================================================
    @Override
    public Bid getHighestBid(Connection conn, String sessionId) throws SQLException {
        String sql = "SELECT * FROM bids WHERE session_id = ? ORDER BY amount DESC LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User bidder = userDAO.getUserById(conn, rs.getInt("user_id"));
                    return new Bid(bidder, rs.getDouble("amount"));
                }
            }
        }
        return null;
    }

    @Override
    public Bid getHighestBid(String sessionId) {
        try (Connection conn = DBConnection.getConnection()) {
            return getHighestBid(conn, sessionId);
        } catch (SQLException e) {
            logger.error("Lỗi khi lấy giá cao nhất cho session {}: {}", sessionId, e.getMessage(), e);
            return null;
        }
    }

    // =========================================================================
    // 2. LƯU LƯỢT ĐẶT GIÁ MỚI
    // =========================================================================
    @Override
    public boolean addBid(Connection conn, String sessionId, Bid bid) throws SQLException {
        String sql = "INSERT INTO bids (session_id, user_id, amount) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            pstmt.setInt(2, bid.getBidder().getID());
            pstmt.setDouble(3, bid.getAmount());
            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean addBid(String sessionId, Bid bid) {
        try (Connection conn = DBConnection.getConnection()) {
            return addBid(conn, sessionId, bid);
        } catch (SQLException e) {
            logger.error("Lỗi khi thêm lượt đặt giá cho session {}: {}", sessionId, e.getMessage(), e);
            return false;
        }
    }

    // =========================================================================
    // 3. LẤY LỊCH SỬ ĐẶT GIÁ
    // =========================================================================
    @Override
    public List<Bid> getBidsBySession(Connection conn, String sessionId) throws SQLException {
        List<Bid> bidList = new ArrayList<>();
        String sql = "SELECT * FROM bids WHERE session_id = ? ORDER BY amount DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User bidder = userDAO.getUserById(conn, rs.getInt("user_id"));
                    bidList.add(new Bid(bidder, rs.getDouble("amount")));
                }
            }
        }
        return bidList;
    }

    @Override
    public List<Bid> getBidsBySession(String sessionId) {
        try (Connection conn = DBConnection.getConnection()) {
            return getBidsBySession(conn, sessionId);
        } catch (SQLException e) {
            logger.error("Lỗi khi lấy lịch sử đặt giá cho session {}: {}", sessionId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}