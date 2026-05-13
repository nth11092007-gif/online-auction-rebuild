package model.state;

import model.AuctionSession;
import model.Bid;

public interface AuctionState {
    boolean addBid(AuctionSession session, Bid newBid);
    boolean open(AuctionSession session);
    boolean close(AuctionSession session);
    boolean settle(AuctionSession session);
    boolean cancel(AuctionSession session);
    AuctionSession.Status getStatus();
    default boolean canJoin() { return false; }
}