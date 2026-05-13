package model.state;

import model.AuctionSession;

public class AuctionStateFactory {
    public static AuctionState fromStatus(AuctionSession.Status status) {
        switch (status) {
            case PENDING: return new PendingState();
            case OPEN: return new OpenState();
            case CLOSED: return new ClosedState();
            case SETTLED: return new SettledState();
            default: throw new IllegalArgumentException("Unknown status: " + status);
        }
    }
}