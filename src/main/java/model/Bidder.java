package model;

import java.util.List;

public interface Bidder {
    int getID();
    String getUsername();
    double getBalance();
    double getFrozenBalance();
    List<AuctionSession> getJoinedAuctionSessions();
    void addJoinedAuctionSession(AuctionSession session);
}