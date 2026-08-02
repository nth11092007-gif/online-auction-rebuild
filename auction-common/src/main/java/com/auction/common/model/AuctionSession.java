package com.auction.common.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import com.auction.common.model.state.AuctionState;
import com.auction.common.model.state.AuctionStateFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.auction.common.utils.IDGenerator;

/**
 * Represents an auction session with a seller, item, bidding history,
 * and state management for the auction lifecycle.
 */
public class AuctionSession {

  private Seller seller;

  private Item item;

  private final String sessionId;

  private final double startingPrice;

  private final double incrementStep;

  private double currentPrice;

  private Bidder highestBidder = null;

  private final ArrayList<Bid> bidHistory = new ArrayList<>();

  private LocalDateTime startTime;

  private LocalDateTime endTime;

  /** Status enum representing auction lifecycle states. */
  public enum Status {
    PENDING,
    OPEN,
    CLOSED,
    SETTLED
  }

  private Status status;

  private AuctionState state;

  private final Logger logger =
      LoggerFactory.getLogger(AuctionSession.class);

  /**
   * Constructs an AuctionSession with full parameters.
   *
   * @param seller the seller who created the auction
   * @param item the item being auctioned
   * @param startingPrice the starting price
   * @param incrementStep the minimum bid increment
   * @param startTime the scheduled start time
   */
  public AuctionSession(Seller seller, Item item,
      double startingPrice, double incrementStep,
      LocalDateTime startTime) {
    this.seller = seller;
    this.item = item;
    this.sessionId = IDGenerator.generateSessionId();
    this.startingPrice = startingPrice;
    this.incrementStep = incrementStep;
    this.startTime = startTime;
    this.status = Status.PENDING;
    this.state = AuctionStateFactory.fromStatus(this.status);
    if (this.seller != null) {
      this.seller.addCreatedAuctionSession(this);
    }
  }

  /**
   * Constructs an AuctionSession with a specified session ID.
   *
   * @param seller the seller who created the auction
   * @param item the item being auctioned
   * @param startingPrice the starting price
   * @param incrementStep the minimum bid increment
   * @param startTime the scheduled start time
   * @param sessionId the unique session identifier
   */
  public AuctionSession(Seller seller, Item item,
      double startingPrice, double incrementStep,
      LocalDateTime startTime, String sessionId) {
    this.seller = seller;
    this.item = item;
    this.sessionId = sessionId;
    this.startingPrice = startingPrice;
    this.incrementStep = incrementStep;
    this.startTime = startTime;
    this.status = Status.PENDING;
    this.state = AuctionStateFactory.fromStatus(this.status);
    if (this.seller != null) {
      this.seller.addCreatedAuctionSession(this);
    }
  }

  /**
   * Constructs an AuctionSession with default increment and current time.
   *
   * @param seller the seller who created the auction
   * @param item the item being auctioned
   * @param startingPrice the starting price
   */
  public AuctionSession(Seller seller, Item item,
      double startingPrice) {
    this(seller, item, startingPrice, 0.1, LocalDateTime.now());
  }

  public void setCurrentPrice(double price) {
    this.currentPrice = price;
  }

  public void setHighestBidder(Bidder bidder) {
    this.highestBidder = bidder;
  }

  public void setStartTime(LocalDateTime time) {
    this.startTime = time;
  }

  public void setEndTime(LocalDateTime time) {
    this.endTime = time;
  }

  public void setStatus(Status status) {
    this.status = status;
    this.state = AuctionStateFactory.fromStatus(status);
  }

  public Seller getSeller() {
    return seller;
  }

  public Item getItem() {
    return item;
  }

  public double getStartingPrice() {
    return startingPrice;
  }

  public double getIncrementStep() {
    return incrementStep;
  }

  public double getCurrentPrice() {
    return currentPrice;
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public LocalDateTime getEndTime() {
    return endTime;
  }

  public Bidder getHighestBidder() {
    return highestBidder;
  }

  public ArrayList<Bid> getBidHistory() {
    return bidHistory;
  }

  public String getSessionId() {
    return this.sessionId;
  }

  public Status getStatus() {
    return this.status;
  }

  public AuctionState getState() {
    return this.state;
  }

  public void setState(AuctionState state) {
    this.state = state;
  }

  /**
   * Starts the auction session with the given number of open days.
   *
   * @param openDays the number of days the auction remains open
   */
  public void startSession(int openDays) {
    if (this.setOpen()) {
      this.setStatus(Status.OPEN);
    }
    this.currentPrice = startingPrice;
    this.startTime = LocalDateTime.now();
    this.endTime = this.startTime.plusDays(openDays);
    logger.info("Phiên đấu giá {} bắt đầu lúc {} "
        + "và sẽ kết thúc lúc {}", sessionId, startTime, endTime);
    logger.info("Giá khởi điểm: {}, Bước giá: {}",
        startingPrice, incrementStep);
  }

  /** Ends the auction session. */
  public void endSession() {
    if (this.setClose()) {
      this.setStatus(Status.CLOSED);
    }
    this.endTime = LocalDateTime.now();
    logger.info("Phiên đấu giá {} đã kết thúc lúc {}",
        sessionId, endTime);
    if (highestBidder != null) {
      logger.info("Người chiến thắng: {} với giá {}",
          highestBidder.getUsername(), currentPrice);
    } else {
      logger.info(
          "Không có ai tham gia trả giá. "
          + "Vật phẩm chưa được bán!");
    }
  }

  /**
   * Adds a bid to the auction session via the current state.
   *
   * @param newBid the bid to add
   * @return true if the bid was accepted
   */
  public boolean addBid(Bid newBid) {
    return this.state.addBid(this, newBid);
  }

  public boolean joinable() {
    return this.state.canJoin();
  }

  public boolean setOpen() {
    return this.state.open(this);
  }

  public boolean setClose() {
    return this.state.close(this);
  }

  public boolean settle() {
    return this.state.settle(this);
  }
}
