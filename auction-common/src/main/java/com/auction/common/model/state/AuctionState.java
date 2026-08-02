package com.auction.common.model.state;

import com.auction.common.model.AuctionSession;
import com.auction.common.model.Bid;

/**
 * State interface for the auction session state machine.
 * Each implementation represents a distinct auction lifecycle state.
 */
public interface AuctionState {

  boolean addBid(AuctionSession session, Bid newBid);

  boolean open(AuctionSession session);

  boolean close(AuctionSession session);

  boolean settle(AuctionSession session);

  boolean cancel(AuctionSession session);

  AuctionSession.Status getStatus();

  /** Returns whether new bidders can join in this state. */
  default boolean canJoin() {
    return false;
  }
}

