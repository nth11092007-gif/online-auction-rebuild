package com.auction.server.websocket;

/** Observer - interface for receiving auction event notifications. */
public interface Observer {
  void update(String message);
}
