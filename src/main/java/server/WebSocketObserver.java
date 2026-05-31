package server;

import org.java_websocket.WebSocket;

/** WebSocketObserver - sends auction event messages to a WebSocket connection. */
public class WebSocketObserver implements Observer {

  private final WebSocket conn;

  public WebSocketObserver(WebSocket conn) {
    this.conn = conn;
  }

  @Override
  public void update(String message) {
    if (conn != null && conn.isOpen()) {
      conn.send(message);
    }
  }
}
