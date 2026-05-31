package model;

import java.time.LocalDateTime;

/**
 * Represents a bid placed by a bidder in an auction session.
 */
public class Bid {

  private final Bidder bidder;

  private final double amount;

  private LocalDateTime time;

  /**
   * Constructs a Bid with the current timestamp.
   *
   * @param bidder the bidder placing the bid
   * @param amount the bid amount
   */
  public Bid(Bidder bidder, double amount) {
    this.bidder = bidder;
    this.amount = amount;
    this.time = LocalDateTime.now();
  }

  /**
   * Full constructor used by DAO when loading from database.
   *
   * @param bidder the bidder placing the bid
   * @param amount the bid amount
   * @param time the time the bid was placed
   */
  public Bid(User bidder, double amount, LocalDateTime time) {
    this.bidder = bidder;
    this.amount = amount;
    this.time = time;
  }

  public double getAmount() {
    return amount;
  }

  public Bidder getBidder() {
    return bidder;
  }

  public LocalDateTime getTime() {
    return time;
  }
}
