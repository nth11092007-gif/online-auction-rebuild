package com.auction.server.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.auction.server.exception.AuctionClosedException;
import com.auction.server.exception.InvalidBidException;

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

/** AuctionService - manages auction sessions, bidding, and anti-sniping logic. */
public class AuctionService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AuctionService.class);

  private static final int SNIPING_THRESHOLD_MS =
      3 * 60 * 1000;
  private static final int EXTENSION_TIME_MINUTES = 3;

  private final UserDAO userDao;
  private final BidDAO bidDao;
  private final AuctionSessionDAO sessionDao;
  private AuctionEventPublisher eventPublisher;
  public AuctionService() {
    this(new UserDAOImpl(),
        new BidDAOImpl(), new AuctionSessionDAOImpl());
  }

  /**
   * Constructs an AuctionService with DAO dependencies.
   *
   * @param userDao the user data access object
   * @param bidDao the bid data access object
   * @param sessionDao the auction session data access object
   */
  public AuctionService(UserDAO userDao, BidDAO bidDao,
      AuctionSessionDAO sessionDao) {
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
    try {
      // Lightweight fetch
      AuctionSession session = sessionDao.getSessionForPlaceBid(sessionId);
      if (session == null) {
        LOGGER.warn("Session {} not found", sessionId);
        return false;
      }

      // 1. Freeze money immediately (atomic in DB)
      boolean isDeducted = userDao.freezeMoneyAtomic(currentUserId, bidAmount);
      if (!isDeducted) {
        LOGGER.warn(
            "Cannot freeze {} from user {}",
            bidAmount, currentUserId);
        return false;
      }

      // 2. Check session status and conditions
      if (session.getStatus() != Status.OPEN || !session.joinable()) {
        LOGGER.warn("Session {} not OPEN or not joinable", sessionId);
        userDao.refundMoneyAtomic(currentUserId, bidAmount);
        throw new AuctionClosedException("Auction is not open or not joinable");
      }

      double currentPrice = session.getCurrentPrice();
      double minValidBid = currentPrice + session.getIncrementStep();
      if (bidAmount < minValidBid) {
        userDao.refundMoneyAtomic(currentUserId, bidAmount);
        throw new InvalidBidException("Bid amount is less than minimum valid bid");
      }

      long timeDiff = ChronoUnit.MILLIS.between(
          LocalDateTime.now(), session.getEndTime());

      if (timeDiff > 0 && timeDiff < SNIPING_THRESHOLD_MS) {
        LocalDateTime newEndTime = session.getEndTime().plusMinutes(EXTENSION_TIME_MINUTES);
        session.setEndTime(newEndTime);

        sessionDao.updateEndTime(sessionId, Timestamp.valueOf(newEndTime));

        LOGGER.info(
            "Anti-sniping: Extended session {} to {}",
            sessionId, newEndTime);
      }

      // 3. Refund previous highest bidder
      Bidder previousBidder = session.getHighestBidder();
      if (previousBidder != null) {
        userDao.refundMoneyAtomic(previousBidder.getId(), currentPrice);
      }

      // 4. Add new bid
      Bid newBid = new Bid(
          userDao.getUserById(currentUserId),
          bidAmount);
      session.addBid(newBid);
      bidDao.addBid(sessionId, newBid);

      sessionDao.updateCurrentPrice(sessionId, bidAmount);

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

      LOGGER.info(
          "Bid placed: user {}, session {}, amount {}",
          currentUserId, sessionId, bidAmount);
      return true;
    } catch (InvalidBidException | AuctionClosedException e) {
      LOGGER.error(
          "Error placing bid: user {}, session {},"
          + " amount {}: {}",
          currentUserId, sessionId, bidAmount,
          e.getMessage());
      throw e;
    } catch (Exception e) {
      LOGGER.error(
          "Error placing bid: user {}, session {},"
          + " amount {}: {}",
          currentUserId, sessionId, bidAmount,
          e.getMessage());
    }
    return false;
  }
}
