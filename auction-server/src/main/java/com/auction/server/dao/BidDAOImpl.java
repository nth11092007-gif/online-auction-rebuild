package com.auction.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import com.auction.common.model.Bid;
import com.auction.common.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.auction.server.utils.DBConnection;

/**
 * Implementation of {@link BidDAO} using JDBC.
 */
public class BidDAOImpl implements BidDAO {

  private static final Logger logger =
      LoggerFactory.getLogger(BidDAOImpl.class);
  private final UserDAO userDao;
  private final DataSource dataSource;

  public BidDAOImpl() {
    this(DBConnection.getDataSource(), new UserDAOImpl());
  }

  public BidDAOImpl(DataSource dataSource, UserDAO userDao) {
    this.dataSource = dataSource;
    this.userDao = userDao;
  }

  // =========================================================================
  // 1. LAY GIA CAO NHAT
  // =========================================================================
  @Override
  public Bid getHighestBid(
      Connection conn, String sessionId) throws SQLException {
    String sql = "SELECT * FROM bids"
        + " WHERE session_id = ? ORDER BY amount DESC LIMIT 1";
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, sessionId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          User bidder =
              userDao.getUserById(conn, rs.getInt("user_id"));
          return new Bid(bidder, rs.getDouble("amount"));
        }
      }
    }
    return null;
  }

  @Override
  public Bid getHighestBid(String sessionId) {
    try (Connection conn = dataSource.getConnection()) {
      return getHighestBid(conn, sessionId);
    } catch (SQLException e) {
      logger.error(
          "Lỗi khi lấy giá cao nhất cho session {}: {}",
          sessionId, e.getMessage(), e);
      return null;
    }
  }

  // =========================================================================
  // 2. LUU LUOT DAT GIA MOI
  // =========================================================================
  @Override
  public boolean addBid(
      Connection conn, String sessionId, Bid bid)
      throws SQLException {
    String sql = "INSERT INTO bids"
        + " (session_id, user_id, amount) VALUES (?, ?, ?)";
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, sessionId);
      pstmt.setInt(2, bid.getBidder().getId());
      pstmt.setDouble(3, bid.getAmount());
      return pstmt.executeUpdate() > 0;
    }
  }

  @Override
  public boolean addBid(String sessionId, Bid bid) {
    try (Connection conn = dataSource.getConnection()) {
      return addBid(conn, sessionId, bid);
    } catch (SQLException e) {
      logger.error(
          "Lỗi khi thêm lượt đặt giá cho session {}: {}",
          sessionId, e.getMessage(), e);
      return false;
    }
  }

  // =========================================================================
  // 3. LAY LICH SU DAT GIA
  // =========================================================================
  @Override
  public List<Bid> getBidsBySession(
      Connection conn, String sessionId) throws SQLException {
    List<Bid> bidList = new ArrayList<>();
    String sql = "SELECT * FROM bids"
        + " WHERE session_id = ? ORDER BY amount DESC";
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, sessionId);
      // Phase 1: Thu thap tat ca bids va user IDs
      List<int[]> rawData = new ArrayList<>();
      List<Double> amounts = new ArrayList<>();
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          amounts.add(rs.getDouble("amount"));
          rawData.add(
              new int[]{rs.getInt("user_id"),
                  amounts.size() - 1});
        }
      }
      if (rawData.isEmpty()) {
        return bidList;
      }

      // Phase 2: Batch fetch users (1 query thay vi N query)
      List<Integer> userIds = rawData.stream()
          .map(row -> row[0])
          .distinct()
          .collect(Collectors.toList());
      Map<Integer, User> userMap =
          userDao.getUsersByIds(conn, userIds);

      // Phase 3: Map bids
      for (int[] row : rawData) {
        User bidder = userMap.get(row[0]);
        bidList.add(new Bid(bidder, amounts.get(row[1])));
      }
    }
    return bidList;
  }

  @Override
  public List<Bid> getBidsBySession(String sessionId) {
    try (Connection conn = dataSource.getConnection()) {
      return getBidsBySession(conn, sessionId);
    } catch (SQLException e) {
      logger.error(
          "Lỗi khi lấy lịch sử đặt giá cho session {}: {}",
          sessionId, e.getMessage(), e);
      return new ArrayList<>();
    }
  }
}
