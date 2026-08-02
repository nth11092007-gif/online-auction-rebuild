package com.auction.common.model;

import java.util.List;

/**
 * Interface representing a seller who can create and manage auction sessions.
 */
public interface Seller {

  int getId();

  String getUsername();

  List<AuctionSession> getCreatedAuctionSessions();

  void addCreatedAuctionSession(AuctionSession session);
}
