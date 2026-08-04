package com.auction.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import com.auction.common.model.AuctionSession;
import com.auction.common.model.Bid;
import com.auction.common.model.Item;
import com.auction.common.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.auction.server.utils.DBConnection;

/**
 * Implementation of {@link AuctionSessionDAO} using JDBC.
 */
public class AuctionSessionDAOImpl
    implements AuctionSessionDAO {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(
          AuctionSessionDAOImpl.class);
  private final UserDAO userDao;
  private final ItemDAO itemDao;
  private final BidDAO bidDao;
  private final DataSource dataSource;

  /** Default constructor using the application DataSource. */
  public AuctionSessionDAOImpl() {
    this(DBConnection.getDataSource(),
        new UserDAOImpl(), new ItemDAOImpl(),
        new BidDAOImpl());
  }

  /**
   * Constructs an AuctionSessionDAOImpl with the given dependencies.
   *
   * @param dataSource the DataSource to use
   * @param userDao    the UserDAO dependency
   * @param itemDao    the ItemDAO dependency
   * @param bidDao     the BidDAO dependency
   */
  public AuctionSessionDAOImpl(
      DataSource dataSource, UserDAO userDao,
      ItemDAO itemDao, BidDAO bidDao) {
    this.dataSource = dataSource;
    this.userDao = userDao;
    this.itemDao = itemDao;
    this.bidDao = bidDao;
  }

  // =========================================================================
  // 1. TAO PHIEN DAU GIA
  // =========================================================================

  @Override
  public boolean createSession(
      Connection conn, AuctionSession session,
      int itemId) throws SQLException {
    // Dam bao session co startTime va endTime
    LocalDateTime startTime = session.getStartTime();
    LocalDateTime endTime = session.getEndTime();
    if (startTime == null) {
      startTime = LocalDateTime.now();
      session.setStartTime(startTime);
    }
    if (endTime == null) {
      // Mac dinh 3 ngay neu chua set
      endTime = startTime.plusDays(3);
      session.setEndTime(endTime);
    }
    int durationDays =
        (int) Duration.between(startTime, endTime)
            .toDays();
    if (durationDays < 0) {
      durationDays = 0;
    }

    String sql = "INSERT INTO auction_sessions"
        + " (session_id, owner_id, item_id,"
        + " starting_price, step_price,"
        + " start_time, end_time,"
        + " duration_days, status)"
        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement pstmt =
        conn.prepareStatement(sql)) {
      pstmt.setString(1, session.getSessionId());
      pstmt.setInt(2, session.getSeller().getId());
      pstmt.setInt(3, itemId);
      pstmt.setDouble(4, session.getStartingPrice());
      pstmt.setDouble(5, session.getIncrementStep());
      pstmt.setTimestamp(6,
          Timestamp.valueOf(startTime));
      pstmt.setTimestamp(7,
          Timestamp.valueOf(endTime));
      pstmt.setInt(8, durationDays);
      pstmt.setString(9,
          session.getStatus().name());
      return pstmt.executeUpdate() > 0;
    }
  }

  @Override
  public boolean createSession(
      AuctionSession session, int itemId) {
    try (Connection conn =
        dataSource.getConnection()) {
      return createSession(conn, session, itemId);
    } catch (SQLException e) {
      LOGGER.error(
          "❌ Lỗi khi tạo phiên đấu giá: {}",
          e.getMessage(), e);
    }
    return false;
  }

  // =========================================================================
  // 2. LAY THONG TIN MOT PHIEN DAU GIA
  // =========================================================================

  @Override
  public AuctionSession getSessionById(
      Connection conn, String sessionId)
      throws SQLException {
    String sql = "SELECT * FROM auction_sessions"
        + " WHERE session_id = ? FOR UPDATE";
    try (PreparedStatement pstmt =
        conn.prepareStatement(sql)) {
      pstmt.setString(1, sessionId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          User seller = userDao.getUserById(
              conn, rs.getInt("owner_id"));
          Item item = itemDao.getItemById(
              conn, rs.getInt("item_id"));
          double startingPrice =
              rs.getDouble("starting_price");
          double stepPrice =
              rs.getDouble("step_price");
          Timestamp startTs =
              rs.getTimestamp("start_time");
          Timestamp endTs =
              rs.getTimestamp("end_time");
          LocalDateTime startTime =
              startTs != null
                  ? startTs.toLocalDateTime()
                  : null;
          LocalDateTime endTime =
              endTs != null
                  ? endTs.toLocalDateTime()
                  : null;


          // Tao session (constructor co startTime)
          AuctionSession session =
              new AuctionSession(seller, item,
                  startingPrice, stepPrice,
                  startTime, sessionId);
          session.setEndTime(endTime);
          session.setStatus(
              AuctionSession.Status.valueOf(
                  rs.getString("status")));

          // Lay currentPrice va highestBidder tu bids
          List<Bid> bids =
              bidDao.getBidsBySession(
                  conn, sessionId);
          if (bids != null && !bids.isEmpty()) {
            Bid highestBid = bids.get(0);
            session.setCurrentPrice(
                highestBid.getAmount());
            session.setHighestBidder(
                highestBid.getBidder());
          } else {
            session.setCurrentPrice(startingPrice);
          }
          return session;
        }
      }
    } catch (SQLException e) {
      LOGGER.error(
          "Lỗi khi lấy phiên đấu giá theo ID"
          + " {}: {}",
          sessionId, e.getMessage(), e);
    }
    return null;
  }

  @Override
  public AuctionSession getSessionById(
      String sessionId) {
    try (Connection conn =
        dataSource.getConnection()) {
      return getSessionByIdReadOnly(conn, sessionId);
    } catch (SQLException e) {
      LOGGER.error(
          "Lỗi kết nối khi lấy session {}: {}",
          sessionId, e.getMessage(), e);
    }
    return null;
  }

  @Override
  public AuctionSession getSessionByIdReadOnly(
      Connection conn, String sessionId)
      throws SQLException {
    String sql = "SELECT * FROM auction_sessions"
        + " WHERE session_id = ?";
    try (PreparedStatement pstmt =
        conn.prepareStatement(sql)) {
      pstmt.setString(1, sessionId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          User seller = userDao.getUserById(
              conn, rs.getInt("owner_id"));
          Item item = itemDao.getItemById(
              conn, rs.getInt("item_id"));
          double startingPrice =
              rs.getDouble("starting_price");
          double stepPrice =
              rs.getDouble("step_price");
          Timestamp startTs =
              rs.getTimestamp("start_time");
          LocalDateTime startTime =
              startTs != null
                  ? startTs.toLocalDateTime()
                  : null;
          Timestamp endTs =
              rs.getTimestamp("end_time");
          LocalDateTime endTime =
              endTs != null
                  ? endTs.toLocalDateTime()
                  : null;


          AuctionSession session =
              new AuctionSession(seller, item,
                  startingPrice, stepPrice,
                  startTime, sessionId);
          session.setEndTime(endTime);
          session.setStatus(
              AuctionSession.Status.valueOf(
                  rs.getString("status")));

          List<Bid> bids =
              bidDao.getBidsBySession(
                  conn, sessionId);
          if (bids != null && !bids.isEmpty()) {
            Bid highestBid = bids.get(0);
            session.setCurrentPrice(
                highestBid.getAmount());
            session.setHighestBidder(
                highestBid.getBidder());
          } else {
            session.setCurrentPrice(startingPrice);
          }
          return session;
        }
      }
    } catch (SQLException e) {
      LOGGER.error(
          "Lỗi khi lấy phiên đấu giá theo ID"
          + " {}: {}",
          sessionId, e.getMessage(), e);
    }
    return null;
  }

  @Override
  public Map<String, AuctionSession>
      getSessionsByIds(
          Connection conn,
          List<String> sessionIds)
          throws SQLException {
    Map<String, AuctionSession> map =
        new LinkedHashMap<>();
    if (sessionIds == null
        || sessionIds.isEmpty()) {
      return map;
    }

    String placeholders = sessionIds.stream()
        .map(id -> "?")
        .collect(Collectors.joining(","));
    String sql = "SELECT * FROM auction_sessions"
        + " WHERE session_id IN ("
        + placeholders + ")";
    try (PreparedStatement pstmt =
        conn.prepareStatement(sql)) {
      for (int i = 0; i < sessionIds.size(); i++) {
        pstmt.setString(
            i + 1, sessionIds.get(i));
      }
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          AuctionSession session =
              buildSessionFromResultSet(conn, rs);
          if (session != null) {
            map.put(
                session.getSessionId(), session);
          }
        }
      }
    }
    return map;
  }

  /**
   * Helper: xay dung AuctionSession tu ResultSet
   * (dung chung cho single & batch fetch).
   */
  private AuctionSession buildSessionFromResultSet(
      Connection conn, ResultSet rs)
      throws SQLException {
    User seller = userDao.getUserById(
        conn, rs.getInt("owner_id"));
    Item item = itemDao.getItemById(
        conn, rs.getInt("item_id"));
    double startingPrice =
        rs.getDouble("starting_price");
    double stepPrice =
        rs.getDouble("step_price");
    Timestamp startTs =
        rs.getTimestamp("start_time");
    Timestamp endTs =
        rs.getTimestamp("end_time");
    LocalDateTime startTime =
        startTs != null
            ? startTs.toLocalDateTime() : null;
    LocalDateTime endTime =
        endTs != null
            ? endTs.toLocalDateTime() : null;
    String sessionId =
        rs.getString("session_id");

    AuctionSession session =
        new AuctionSession(seller, item,
            startingPrice, stepPrice,
            startTime, sessionId);
    session.setEndTime(endTime);
    session.setStatus(
        AuctionSession.Status.valueOf(
            rs.getString("status")));

    List<Bid> bids =
        bidDao.getBidsBySession(conn, sessionId);
    if (bids != null && !bids.isEmpty()) {
      Bid highestBid = bids.get(0);
      session.setCurrentPrice(
          highestBid.getAmount());
      session.setHighestBidder(
          highestBid.getBidder());
    } else {
      session.setCurrentPrice(startingPrice);
    }
    return session;
  }

  @Override
  public List<AuctionSession> getAllSessions(
      Connection conn) throws SQLException {
    List<String> ids = new ArrayList<>();
    String sql =
        "SELECT session_id FROM auction_sessions";
    try (PreparedStatement pstmt =
            conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()) {
      while (rs.next()) {
        ids.add(rs.getString("session_id"));
      }
    }
    return new ArrayList<>(
        getSessionsByIds(conn, ids).values());
  }

  @Override
  public List<AuctionSession> getAllSessions() {
    try (Connection conn =
        dataSource.getConnection()) {
      return getAllSessions(conn);
    } catch (SQLException e) {
      LOGGER.error(
          "Lỗi khi lấy danh sách tất cả phiên: {}",
          e.getMessage(), e);
    }
    return new ArrayList<>();
  }

  // =========================================================================
  // 3. CAP NHAT TRANG THAI PHIEN & GIA HAN
  // =========================================================================

  @Override
  public boolean updateSessionStatusAtomic(
      Connection conn, String sessionId,
      AuctionSession.Status status)
      throws SQLException {
    String sql = "UPDATE auction_sessions"
        + " SET status = ? WHERE session_id = ?";
    try (PreparedStatement pstmt =
        conn.prepareStatement(sql)) {
      pstmt.setString(1, status.name());
      pstmt.setString(2, sessionId);
      return pstmt.executeUpdate() > 0;
    }
  }

  @Override
  public boolean updateSessionStatusAtomic(
      String sessionId,
      AuctionSession.Status status) {
    try (Connection conn =
        dataSource.getConnection()) {
      return updateSessionStatusAtomic(
          conn, sessionId, status);
    } catch (SQLException e) {
      LOGGER.error(
          "Lỗi cập nhật trạng thái phiên {}: {}",
          sessionId, e.getMessage(), e);
    }
    return false;
  }

  @Override
  public boolean updateEndTime(
      Connection conn, String sessionId,
      Timestamp newEndTime) throws SQLException {
    String sql = "UPDATE auction_sessions"
        + " SET end_time = ? WHERE session_id = ?";
    try (PreparedStatement pstmt =
        conn.prepareStatement(sql)) {
      pstmt.setTimestamp(1, newEndTime);
      pstmt.setString(2, sessionId);
      return pstmt.executeUpdate() > 0;
    }
  }

  @Override
  public boolean updateCurrentPrice(
      Connection conn, String sessionId,
      double newPrice) {
    String sql = "UPDATE auction_sessions"
        + " SET current_price = ?"
        + " WHERE session_id = ?";
    try (PreparedStatement pstmt =
        conn.prepareStatement(sql)) {
      pstmt.setDouble(1, newPrice);
      pstmt.setString(2, sessionId);
      return pstmt.executeUpdate() > 0;
    } catch (SQLException e) {
      LOGGER.error(
          "Lỗi cập nhật giá hiện tại phiên {}: {}",
          sessionId, e.getMessage(), e);
    }
    return false;
  }

  // =========================================================================
  // 4. LAY DANH SACH PHIEN THEO THOI GIAN
  // (CHO SCHEDULER)
  // =========================================================================

  @Override
  public List<AuctionSession>
      getSessionsStartBefore(
          Connection conn, LocalDateTime time,
          AuctionSession.Status status)
          throws SQLException {
    List<String> ids = new ArrayList<>();
    String sql = "SELECT session_id"
        + " FROM auction_sessions"
        + " WHERE start_time <= ? AND status = ?";
    try (PreparedStatement pstmt =
        conn.prepareStatement(sql)) {
      pstmt.setTimestamp(1,
          Timestamp.valueOf(time));
      pstmt.setString(2, status.name());
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          ids.add(rs.getString("session_id"));
        }
      }
    }
    return new ArrayList<>(
        getSessionsByIds(conn, ids).values());
  }

  @Override
  public List<AuctionSession>
      getSessionsStartBefore(
          LocalDateTime time,
          AuctionSession.Status status) {
    try (Connection conn =
        dataSource.getConnection()) {
      return getSessionsStartBefore(
          conn, time, status);
    } catch (SQLException e) {
      LOGGER.error(
          "Lỗi lấy phiên startBefore: {}",
          e.getMessage(), e);
    }
    return new ArrayList<>();
  }

  @Override
  public List<AuctionSession>
      getSessionsEndBefore(
          Connection conn, LocalDateTime time,
          AuctionSession.Status status)
          throws SQLException {
    List<String> ids = new ArrayList<>();
    String sql = "SELECT session_id"
        + " FROM auction_sessions"
        + " WHERE end_time < ? AND status = ?";
    try (PreparedStatement pstmt =
        conn.prepareStatement(sql)) {
      pstmt.setTimestamp(1,
          Timestamp.valueOf(time));
      pstmt.setString(2, status.name());
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          ids.add(rs.getString("session_id"));
        }
      }
    }
    return new ArrayList<>(
        getSessionsByIds(conn, ids).values());
  }

  @Override
  public List<AuctionSession>
      getSessionsEndBefore(
          LocalDateTime time,
          AuctionSession.Status status) {
    try (Connection conn =
        dataSource.getConnection()) {
      return getSessionsEndBefore(
          conn, time, status);
    } catch (SQLException e) {
      LOGGER.error(
          "Lỗi lấy phiên endBefore: {}",
          e.getMessage(), e);
    }
    return new ArrayList<>();
  }

  @Override
  public AuctionSession getSessionForUpdate(
      Connection conn, String sessionId)
      throws SQLException {
    String sql = "SELECT * FROM auction_sessions"
        + " WHERE session_id = ? FOR UPDATE";
    try (PreparedStatement pstmt =
        conn.prepareStatement(sql)) {
      pstmt.setString(1, sessionId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return buildSessionFromResultSet(
              conn, rs);
        }
      }
    } catch (SQLException e) {
      LOGGER.error(
          "Lỗi khi lấy phiên đấu giá có khoá"
          + " theo ID {}: {}",
          sessionId, e.getMessage(), e);
    }
    return null;
  }

  @Override
  public List<AuctionSession> getSessionsByStatus(
      AuctionSession.Status status) {
    List<String> ids = new ArrayList<>();
    String sql = "SELECT session_id"
        + " FROM auction_sessions WHERE status = ?";
    try (Connection conn =
            dataSource.getConnection();
        PreparedStatement pstmt =
            conn.prepareStatement(sql)) {
      pstmt.setString(1, status.name());
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          ids.add(rs.getString("session_id"));
        }
      }
      return new ArrayList<>(
          getSessionsByIds(conn, ids).values());
    } catch (SQLException e) {
      LOGGER.error(
          "Lỗi lấy phiên theo trạng thái {}: {}",
          status, e.getMessage(), e);
    }
    return new ArrayList<>();
  }

  /**
   * Lightweight fetch cho placeBid: chi lay session
   * (FOR UPDATE) + highest bid. Khong fetch tat ca
   * bids, khong fetch seller/item day du.
   * Giam tu ~5 queries xuong 2 queries.
   */
  @Override
  public AuctionSession getSessionForPlaceBid(
      Connection conn, String sessionId)
      throws SQLException {
    String sql = "SELECT * FROM auction_sessions"
        + " WHERE session_id = ? FOR UPDATE";
    try (PreparedStatement pstmt =
        conn.prepareStatement(sql)) {
      pstmt.setString(1, sessionId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          double startingPrice =
              rs.getDouble("starting_price");
          double stepPrice =
              rs.getDouble("step_price");
          Timestamp endTs =
              rs.getTimestamp("end_time");
          LocalDateTime endTime =
              endTs != null
                  ? endTs.toLocalDateTime()
                  : null;
          Timestamp startTs =
              rs.getTimestamp("start_time");
          LocalDateTime startTime =
              startTs != null
                  ? startTs.toLocalDateTime()
                  : null;

          // Lay seller (can cho constructor)
          User seller = userDao.getUserById(
              conn, rs.getInt("owner_id"));
          Item item = itemDao.getItemById(
              conn, rs.getInt("item_id"));

          AuctionSession session =
              new AuctionSession(seller, item,
                  startingPrice, stepPrice,
                  startTime, sessionId);
          session.setEndTime(endTime);
          session.setStatus(
              AuctionSession.Status.valueOf(
                  rs.getString("status")));

          // Chi lay highest bid (1 query)
          Bid highestBid =
              bidDao.getHighestBid(
                  conn, sessionId);
          if (highestBid != null) {
            session.setCurrentPrice(
                highestBid.getAmount());
            session.setHighestBidder(
                highestBid.getBidder());
          } else {
            session.setCurrentPrice(
                startingPrice);
          }
          return session;
        }
      }
    }
    return null;
  }
}
