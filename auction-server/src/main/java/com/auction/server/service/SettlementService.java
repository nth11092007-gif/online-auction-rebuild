package com.auction.server.service;

import com.auction.server.dao.AuctionSessionDAO;
import com.auction.server.dao.AuctionSessionDAOImpl;
import com.auction.server.dao.BidDAO;
import com.auction.server.dao.BidDAOImpl;
import com.auction.server.dao.ItemDAO;
import com.auction.server.dao.ItemDAOImpl;
import com.auction.server.dao.UserDAO;
import com.auction.server.dao.UserDAOImpl;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import com.auction.common.model.AuctionSession;
import com.auction.common.model.Bid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.auction.server.utils.DBConnection;

/** SettlementService - handles auction settlement, payment transfer, and item ownership updates. */
public class SettlementService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(SettlementService.class);

  private final DataSource dataSource;
  private final AuctionSessionDAO sessionDao;
  private final BidDAO bidDao;
  private final UserDAO userDao;
  private final ItemDAO itemDao;

  /** Constructs a SettlementService with default DAO implementations. */
  public SettlementService() {
    this(DBConnection.getDataSource(),
        new AuctionSessionDAOImpl(),
        new BidDAOImpl(), new UserDAOImpl(),
        new ItemDAOImpl());
  }

  /**
   * Constructs a SettlementService with explicit DataSource and DAO dependencies.
   *
   * @param dataSource the database connection pool
   * @param sessionDao the auction session data access object
   * @param bidDao the bid data access object
   * @param userDao the user data access object
   * @param itemDao the item data access object
   */
  public SettlementService(DataSource dataSource,
      AuctionSessionDAO sessionDao, BidDAO bidDao,
      UserDAO userDao, ItemDAO itemDao) {
    this.dataSource = dataSource;
    this.sessionDao = sessionDao;
    this.bidDao = bidDao;
    this.userDao = userDao;
    this.itemDao = itemDao;
  }

  /**
   * Constructs a SettlementService with DAO dependencies and default DataSource.
   *
   * @param sessionDao the auction session data access object
   * @param bidDao the bid data access object
   * @param userDao the user data access object
   * @param itemDao the item data access object
   */
  public SettlementService(AuctionSessionDAO sessionDao,
      BidDAO bidDao, UserDAO userDao,
      ItemDAO itemDao) {
    this.dataSource = DBConnection.getDataSource();
    this.sessionDao = sessionDao;
    this.bidDao = bidDao;
    this.userDao = userDao;
    this.itemDao = itemDao;
  }

  /**
   * Hàm xử lý kết thúc phiên đấu giá.
   */
  public boolean settleAuction(String sessionId) {
    Connection conn = null;
    try {
      conn = dataSource.getConnection();
      conn.setAutoCommit(false);

      AuctionSession session =
          sessionDao.getSessionById(conn, sessionId);
      if (session == null || !session.settle()) {
        LOGGER.warn(
            "Session {} not found or not OPEN",
            sessionId);
        return false;
      }

      Bid winningBid =
          bidDao.getHighestBid(conn, sessionId);

      if (winningBid != null) {
        int buyerId = winningBid.getBidder().getId();
        int sellerId = session.getSeller().getId();
        double finalPrice = winningBid.getAmount();

        userDao.deductFrozenMoneyAtomic(
            conn, buyerId, finalPrice);
        userDao.addMoneyAtomic(
            conn, sellerId, finalPrice);
        itemDao.updateItemOwner(
            conn, session.getItem().getItemId(),
            buyerId);

        LOGGER.info(
            "Auction settled! Session {}, price {},"
            + " buyer ID: {}",
            sessionId, finalPrice, buyerId);
      } else {
        LOGGER.info(
            "Session {} ended. No bids placed.",
            sessionId);
      }

      sessionDao.updateSessionStatusAtomic(
          conn, sessionId,
          AuctionSession.Status.SETTLED);
      session.setStatus(AuctionSession.Status.SETTLED);
      conn.commit();
      return true;

    } catch (Exception e) {
      if (conn != null) {
        try {
          conn.rollback();
          LOGGER.warn(
              "Rolled back transaction for session {}",
              sessionId);
        } catch (SQLException ex) {
          LOGGER.error("Rollback error: {}",
              ex.getMessage(), ex);
        }
      }
      LOGGER.error(
          "Error settling session {}: {}",
          sessionId, e.getMessage(), e);
      return false;
    } finally {
      if (conn != null) {
        try {
          conn.setAutoCommit(true);
          conn.close();
        } catch (SQLException e) {
          LOGGER.error("Close error: {}",
              e.getMessage(), e);
        }
      }
    }
  }
}
