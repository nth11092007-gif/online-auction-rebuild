package server;

import java.net.URI;
import java.util.function.Consumer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.JsonObject;

public class AuctionWebSocketClient extends WebSocketClient {
    private static AuctionWebSocketClient instance;
    private Consumer<String> onMessageCallback;

    private AuctionWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    public static AuctionWebSocketClient getInstance() {
        if (instance == null) {
            try {
                instance = new AuctionWebSocketClient(new URI("ws://localhost:8887"));
                instance.connectBlocking();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }   

    public void setOnMessageCallback(Consumer<String> callback) {
        this.onMessageCallback = callback;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("WebSocket connected to server");
    }

    @Override
    public void onMessage(String message) {
        if (onMessageCallback != null) {
            // Đảm bảo cập nhật UI trên JavaFX Application Thread
            javafx.application.Platform.runLater(() -> onMessageCallback.accept(message));
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket closed: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
    }

    public void sendJson(JsonObject msg) {
        if (msg != null && isOpen()) {
            send(msg.toString());
        }
    }

    public void login(String username, String password) {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "LOGIN");
        msg.addProperty("username", username);
        msg.addProperty("password", password);
        sendJson(msg);
    }

    public void joinSession(String sessionId) {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", type);

        // Gộp các thuộc tính từ data vào msg
        if (data != null) {
            for (String key : data.keySet()) {
                msg.add(key, data.get(key));
            }
        }

        // Gửi chuỗi JSON qua WebSocket
        if (this.isOpen()) {
            send(msg.toString());
        } else {
            System.err.println("WebSocket chưa mở, không thể gửi lệnh: " + type);
        }
    }
}