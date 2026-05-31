package service;

/**
 * Tách tầng service khỏi server:
 * push sự kiện đấu giá (WebSocket feed, v.v.).
 */
@FunctionalInterface
public interface AuctionEventPublisher {

  void notifyObservers(String sessionId, String message);
}
