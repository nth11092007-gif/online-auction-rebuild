package com.auction.common.model.state;

import com.auction.common.model.AuctionSession;

/**
 * Factory for creating AuctionState instances from a given status.
 */
public class AuctionStateFactory {

  /**
   * Creates the appropriate AuctionState for the given status.
   *
   * @param status the auction session status
   * @return the corresponding AuctionState
   * @throws IllegalArgumentException if the status is unknown
   */
  public static AuctionState fromStatus(
      AuctionSession.Status status) {
    switch (status) {
      case PENDING:
        return new PendingState();
      case OPEN:
        return new OpenState();
      case CLOSED:
        return new ClosedState();
      case SETTLED:
        return new SettledState();
      default:
        throw new IllegalArgumentException(
            "Unknown status: " + status);
    }
  }
}

