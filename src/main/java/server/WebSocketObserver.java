package server;

import org.java_websocket.WebSocket;

public class WebSocketObserver implements Observer{
    private final WebSocket conn;

    public WebSocketObserver(WebSocket conn) {
        this.conn = conn;
    }

    @Override
    public void update(String message) {
        if (conn != null && conn.isOpen()) {
            conn.send(message);
        }
        // TODO: Đẩy tin nhắn tới client qua kết nối WebSocket
    }
}
