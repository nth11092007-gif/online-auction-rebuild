package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.ProxyBid;
import utils.DBConnection;

import javax.sql.DataSource;

public class ProxyBidDAOImpl implements ProxyBidDAO {
    private final DataSource dataSource;
    public ProxyBidDAOImpl() {
        this(DBConnection.getDataSource());
    }

    public ProxyBidDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void addProxyBid(Connection conn, ProxyBid proxyBid) throws SQLException {
        String sql = "INSERT INTO proxy_bids (user_id, session_id, max_amount, active) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, proxyBid.getUserId());
            ps.setString(2, proxyBid.getSessionId());
            ps.setDouble(3, proxyBid.getMaxAmount());
            ps.setBoolean(4, proxyBid.isActive());
            ps.executeUpdate();
        }
    }

    @Override
    public List<ProxyBid> getActiveProxyBidsBySession(Connection conn, String sessionId) throws SQLException {
        List<ProxyBid> list = new ArrayList<>();
        String sql = "SELECT * FROM proxy_bids WHERE session_id = ? AND active = TRUE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProxyBid pb = new ProxyBid();
                    pb.setId(rs.getInt("id"));
                    pb.setUserId(rs.getInt("user_id"));
                    pb.setSessionId(rs.getString("session_id"));
                    pb.setMaxAmount(rs.getDouble("max_amount"));
                    pb.setActive(rs.getBoolean("active"));
                    list.add(pb);
                }
            }
        }
        return list;
    }

    @Override
    public ProxyBid getActiveProxyBid(Connection conn, int userId, String sessionId) throws SQLException {
        String sql = "SELECT * FROM proxy_bids WHERE user_id = ? AND session_id = ? AND active = TRUE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ProxyBid pb = new ProxyBid();
                    pb.setId(rs.getInt("id"));
                    pb.setUserId(rs.getInt("user_id"));
                    pb.setSessionId(rs.getString("session_id"));
                    pb.setMaxAmount(rs.getDouble("max_amount"));
                    pb.setActive(rs.getBoolean("active"));
                    return pb;
                }
            }
        }
        return null;
    }

    @Override
    public void deactivateProxyBid(Connection conn, int proxyBidId) throws SQLException {
        String sql = "UPDATE proxy_bids SET active = FALSE WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, proxyBidId);
            ps.executeUpdate();
        }
    }

    @Override
    public void deactivateAllProxyBidsForSession(Connection conn, String sessionId) throws SQLException {
        String sql = "UPDATE proxy_bids SET active = FALSE WHERE session_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sessionId);
            ps.executeUpdate();
        }
    }
}