package model.state;

import model.AuctionSession;
import model.Bid;

public class OpenState implements AuctionState {
    @Override
    public boolean addBid(AuctionSession session, Bid newBid) {
        if (newBid == null) return false;
        if (newBid.getAmount() < session.getCurrentPrice() + session.getIncrementStep()) {
            return false;
        }
        session.getBidHistory().add(newBid);
        session.setCurrentPrice(newBid.getAmount());
        session.setHighestBidder(newBid.getBidder());
        return true;
    }
    @Override public boolean open(AuctionSession session) { return false; }
    @Override public boolean close(AuctionSession session) {
        session.setState(new ClosedState());
        return true;
    }
    @Override public boolean settle(AuctionSession session) {
        session.setState(new SettledState());
        return true;
    }
    @Override public boolean cancel(AuctionSession session) {
        return close(session);
    }
    @Override public AuctionSession.Status getStatus() { return AuctionSession.Status.OPEN; }
    @Override public boolean canJoin() { return true; }
}