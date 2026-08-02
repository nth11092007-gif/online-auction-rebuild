package com.auction.common.model;

import java.util.List;

/**
 * Interface representing a bidder who can participate in auction sessions.
 */
public interface Bidder {

  int getId();

  String getUsername();

  double getBalance();

  double getFrozenBalance();

  List<AuctionSession> getJoinedAuctionSessions();

  void addJoinedAuctionSession(AuctionSession session);
}
