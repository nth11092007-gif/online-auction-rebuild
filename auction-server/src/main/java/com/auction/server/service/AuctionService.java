package com.auction.server.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auction.server.dao.AuctionSessionDAO;
import com.auction.server.dao.AuctionSessionDAOImpl;
import com.auction.server.dao.BidDAO;
import com.auction.server.dao.BidDAOImpl;
import com.auction.server.dao.UserDAO;
import com.auction.server.dao.UserDAOImpl;
import com.auction.common.model.AuctionSession;
import com.auction.common.model.AuctionSession.Status;
import com.auction.common.model.Bid;
import com.auction.common.model.Bidder;
import com.auction.server.utils.DBConnection;

/** AuctionService - manages auction sessions, bidding, and anti-sniping logic. */
public class AuctionService {

  private static final Logger logger =
      LoggerFactory.getLogger(AuctionService.class);

  private static final int SNIPING_THRESHOLD_MS =
      3 * 60 * 1000;
  private static final int EXTENSION_TIME_MINUTES = 3;

  private final UserDAO userDao;
  private final BidDAO bidDao;
  private final AuctionSessionDAO sessionDao;
  private AuctionEventPublisher eventPublisher;
  private final DataSource dataSource;

  public AuctionService() {
    this(DBConnection.getDataSource(), new UserDAOImpl(),
        new BidDAOImpl(), new AuctionSessionDAOImpl());
  }

  /**
   * Constructs an AuctionService with explicit DataSource and DAO dependencies.
   *
   * @param dataSource the database connection pool
   * @param userDao the user data access object
   * @param bidDao the bid data access object
   * @param sessionDao the auction session data access object
   */
  public AuctionService(DataSource dataSource,
      UserDAO userDao, BidDAO bidDao,
      AuctionSessionDAO sessionDao) {
    this.dataSource = dataSource;
    this.userDao = userDao;
    this.bidDao = bidDao;
    this.sessionDao = sessionDao;
  }

  /**
   * Constructs an AuctionService with DAO dependencies and default DataSource.
   *
   * @param userDao the user data access object
   * @param bidDao the bid data access object
   * @param sessionDao the auction session data access object
   */
  public AuctionService(UserDAO userDao, BidDAO bidDao,
      AuctionSessionDAO sessionDao) {
    this.dataSource = DBConnection.getDataSource();
    this.userDao = userDao;
    this.bidDao = bidDao;
    this.sessionDao = sessionDao;
  }

  public void setEventPublisher(
      AuctionEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  public List<AuctionSession> getAllSessions() {
    return sessionDao.getAllSessions();
  }

  public AuctionSession getSessionById(String sessionId) {
    return sessionDao.getSessionById(sessionId);
  }

  public List<AuctionSession> getSessionsByStatus(
      AuctionSession.Status status) {
    return sessionDao.getSessionsByStatus(status);
  }

  public List<Bid> getBidsBySession(String sessionId) {
    return bidDao.getBidsBySession(sessionId);
  }

  /**
   * Places a bid on an auction session with anti-sniping and refund logic.
   *
   * @param currentUserId the ID of the user placing the bid
   * @param sessionId the auction session identifier
   * @param bidAmount the bid amount to place
   * @return true if the bid was placed successfully, false otherwise
   */
  public boolean placeBid(int currentUserId,
      String sessionId, double bidAmount) {
    Connection conn = null;
    try {
      conn = dataSource.getConnection();
      conn.setAutoCommit(false);

      // Lightweight fetch
      AuctionSession session =
          sessionDao.getSessionForPlaceBid(
              conn, sessionId);
      if (session == null) {
        logger.warn("Session {} not found", sessionId);
        return false;
      }

      // 1. Freeze money immediately (atomic in DB)
      boolean isDeducted = userDao.freezeMoneyAtomic(
          conn, currentUserId, bidAmount);
      if (!isDeducted) {
        logger.warn(
            "Cannot freeze {} from user {}",
            bidAmount, currentUserId);
        conn.rollback();
        return false;
      }

      // 2. Check session status and conditions
      if (session.getStatus() != Status.OPEN
          || !session.joinable()) {
        logger.warn(
            "Session {} not OPEN or not joinable",
            sessionId);
        conn.rollback();
        return false;
      }

      double currentPrice = session.getCurrentPrice();
      double minValidBid =
          currentPrice + session.getIncrementStep();
      if (bidAmount < minValidBid) {
        userDao.refundMoneyAtomic(
            conn, currentUserId, bidAmount);
        conn.rollback();
        return false;
      }

      long timeDiff = ChronoUnit.MILLIS.between(
          LocalDateTime.now(), session.getEndTime());

      if (timeDiff > 0
          && timeDiff < SNIPING_THRESHOLD_MS) {
        LocalDateTime newEndTime =
            session.getEndTime().plusMinutes(
                EXTENSION_TIME_MINUTES);
        session.setEndTime(newEndTime);

        sessionDao.updateEndTime(
            conn, sessionId,
            Timestamp.valueOf(newEndTime));

        logger.info(
            "Anti-sniping: Extended session {} to {}",
            sessionId, newEndTime);
      }

      // 3. Refund previous highest bidder
      Bidder previousBidder =
          session.getHighestBidder();
      if (previousBidder != null) {
        userDao.refundMoneyAtomic(
            conn, previousBidder.getId(), currentPrice);
      }

      // 4. Add new bid
      Bid newBid = new Bid(
          userDao.getUserById(conn, currentUserId),
          bidAmount);
      session.addBid(newBid);
      bidDao.addBid(conn, sessionId, newBid);

      sessionDao.updateCurrentPrice(
          conn, sessionId, bidAmount);
      conn.commit();

      if (eventPublisher != null) {
        String msg = String.format(
            "{\"type\":\"NEW_BID\","
            + "\"sessionId\":\"%s\","
            + "\"newPrice\":%f,"
            + "\"endTime\":\"%s\"}",
            sessionId, bidAmount,
            session.getEndTime().toString());
        eventPublisher.notifyObservers(sessionId, msg);
      }

      logger.info(
          "Bid placed: user {}, session {}, amount {}",
          currentUserId, sessionId, bidAmount);
      return true;
    } catch (SQLException e) {
      logger.error(
          "Error placing bid: user {}, session {},"
          + " amount {}: {}",
          currentUserId, sessionId, bidAmount,
          e.getMessage());
      if (conn != null) {
        try {
          conn.rollback();
        } catch (SQLException rollbackEx) {
          logger.error("Rollback error: {}",
              rollbackEx.getMessage(), rollbackEx);
        }
      }
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
        logger.error("Close error: {}",
            e.getMessage(), e);
      }
    }
    return false;
  }
}
