package com.auction.server.websocket;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** AuctionFeedServer - singleton pub/sub hub for auction event notifications. */
public class AuctionFeedServer {

  private AuctionFeedServer() {
  }

  private static class SingletonHelper {

    private static final AuctionFeedServer AUCTION_FEED_SERVER =
        new AuctionFeedServer();
  }

  public static AuctionFeedServer getInstance() {
    return SingletonHelper.AUCTION_FEED_SERVER;
  }

  private final Map<String, List<Observer>> sessionObservers =
      new ConcurrentHashMap<>();

  /**
   * Subscribes an observer to a specific auction session.
   *
   * @param sessionId the auction session identifier
   * @param observer the observer to subscribe
   */
  public void subscribe(String sessionId, Observer observer) {
    sessionObservers
        .computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>())
        .add(observer);
  }

  /**
   * Unsubscribes an observer from a specific auction session.
   *
   * @param sessionId the auction session identifier
   * @param observer the observer to remove
   */
  public void unsubscribe(String sessionId, Observer observer) {
    List<Observer> observers = sessionObservers.get(sessionId);
    if (observers != null) {
      observers.remove(observer);
    }
  }

  /**
   * Sends a message to all observers subscribed to the given session.
   *
   * @param sessionId the auction session identifier
   * @param message the message to broadcast
   */
  public void notifyObservers(String sessionId, String message) {
    List<Observer> observers = sessionObservers.get(sessionId);

    // Kiem tra xem co ai dang theo doi phien nay khong
    if (observers != null) {
      // Chay vong lap va gui thong bao
      for (Observer observer : observers) {
        observer.update(message);
      }
    }
  }
}
