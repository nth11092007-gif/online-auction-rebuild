package com.auction.common.model.state;

import com.auction.common.model.AuctionSession;
import com.auction.common.model.Bid;

/**
 * Represents the OPEN state of an auction session.
 * Bids can be placed and the session can be closed or settled.
 */
public class OpenState implements AuctionState {

  @Override
  public boolean addBid(AuctionSession session, Bid newBid) {
    if (newBid == null) {
      return false;
    }
    if (newBid.getAmount() < session.getCurrentPrice()
        + session.getIncrementStep()) {
      return false;
    }
    session.getBidHistory().add(newBid);
    session.setCurrentPrice(newBid.getAmount());
    session.setHighestBidder(newBid.getBidder());
    return true;
  }

  @Override
  public boolean open(AuctionSession session) {
    return false;
  }

  @Override
  public boolean close(AuctionSession session) {
    session.setState(new ClosedState());
    return true;
  }

  @Override
  public boolean settle(AuctionSession session) {
    session.setState(new SettledState());
    return true;
  }

  @Override
  public boolean cancel(AuctionSession session) {
    return close(session);
  }

  @Override
  public AuctionSession.Status getStatus() {
    return AuctionSession.Status.OPEN;
  }

  @Override
  public boolean canJoin() {
    return true;
  }
}

