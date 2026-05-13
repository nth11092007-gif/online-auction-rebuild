package model.state;

import model.AuctionSession;
import model.Bid;

public class ClosedState implements AuctionState {
    @Override public boolean addBid(AuctionSession session, Bid newBid) { return false; }
    @Override public boolean open(AuctionSession session) { return false; }
    @Override public boolean close(AuctionSession session) { return false; }
    @Override public boolean settle(AuctionSession session) { return false; }
    @Override public boolean cancel(AuctionSession session) { return false; }
    @Override public AuctionSession.Status getStatus() { return AuctionSession.Status.CLOSED; }
}