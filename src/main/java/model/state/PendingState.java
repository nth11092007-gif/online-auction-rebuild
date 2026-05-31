package model.state;

import model.AuctionSession;
import model.Bid;

/**
 * Represents the PENDING state of an auction session.
 * The session has not yet started; it can be opened or cancelled.
 */
public class PendingState implements AuctionState {

  @Override
  public boolean addBid(AuctionSession session, Bid newBid) {
    return false;
  }

  @Override
  public boolean open(AuctionSession session) {
    session.setState(new OpenState());
    return true;
  }

  @Override
  public boolean close(AuctionSession session) {
    session.setState(new ClosedState());
    return true;
  }

  @Override
  public boolean settle(AuctionSession session) {
    return false;
  }

  @Override
  public boolean cancel(AuctionSession session) {
    return close(session);
  }

  @Override
  public AuctionSession.Status getStatus() {
    return AuctionSession.Status.PENDING;
  }

  @Override
  public boolean canJoin() {
    return false;
  }
}
