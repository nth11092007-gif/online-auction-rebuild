package model;

import java.util.List;

public interface Seller {
    int getID();
    String getUsername();
    List<AuctionSession> getCreatedAuctionSessions();
    void addCreatedAuctionSession(AuctionSession session);
}