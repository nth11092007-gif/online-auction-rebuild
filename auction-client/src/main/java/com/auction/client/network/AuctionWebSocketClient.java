package com.auction.client.network;

import java.net.URI;
import java.util.function.Consumer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** AuctionWebSocketClient - WebSocket client for connecting to the auction server. */
public class AuctionWebSocketClient extends WebSocketClient {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AuctionWebSocketClient.class);

  private static final String DEFAULT_URI = "ws://localhost:8887";

  private Consumer<String> onMessageCallback;
  private final Map<String, Consumer<JsonObject>> messageHandlers = new ConcurrentHashMap<>();

  public AuctionWebSocketClient(URI serverUri) {
    super(serverUri);
  }

  /**
   * Tao client moi voi URI mac dinh.
   * Moi client instance nen tao 1 AuctionWebSocketClient rieng
   * de co ket noi WebSocket doc lap.
   */
  public AuctionWebSocketClient() {
    this(URI.create(DEFAULT_URI));
  }

  public void setOnMessageCallback(Consumer<String> callback) {
    this.onMessageCallback = callback;
  }

  public void addHandler(String type, Consumer<JsonObject> handler) {
    messageHandlers.put(type, handler);
  }

  public void removeHandler(String type) {
    messageHandlers.remove(type);
  }

  /**
   * Thu ket noi den server voi so lan thu lai.
   * Moi lan that bai se doi truoc khi thu lai.
   *
   * @param maxRetries so lan thu toi da
   * @param delayMs    thoi gian doi giua cac lan thu (ms)
   * @return true neu ket noi thanh cong
   */
  public boolean connectWithRetry(int maxRetries, long delayMs) {
    for (int i = 0; i < maxRetries; i++) {
      try {
        LOGGER.info("Đang kết nối WebSocket (lần {}/{})...",
            i + 1, maxRetries);
        connectBlocking();
        if (isOpen()) {
          LOGGER.info("WebSocket đã kết nối thành công.");
          return true;
        }
      } catch (Exception e) {
        LOGGER.warn("Kết nối WebSocket thất bại (lần {}): {}",
            i + 1, e.getMessage());
      }
      if (i < maxRetries - 1) {
        try {
          Thread.sleep(delayMs);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          LOGGER.warn("Bị gián đoạn trong khi đợi kết nối lại.");
          return false;
        }
      }
    }
    LOGGER.error(
        "Không thể kết nối WebSocket sau {} lần thử.", maxRetries);
    return false;
  }

  @Override
  public void onOpen(ServerHandshake handshake) {
    logger.info("WebSocket đã kết nối đến máy chủ");
  }

  @Override
  public void onMessage(String message) {
    javafx.application.Platform.runLater(() -> {
      try {
        JsonObject json = JsonParser.parseString(message).getAsJsonObject();
        String type = json.has("type") ? json.get("type").getAsString() : null;
        if (type != null && messageHandlers.containsKey(type)) {
          messageHandlers.get(type).accept(json);
        }
        if (onMessageCallback != null) {
          onMessageCallback.accept(message);
        }
      } catch (Exception e) {
        LOGGER.error("Lỗi parse JSON: {}", e.getMessage());
        if (onMessageCallback != null) {
          onMessageCallback.accept(message);
        }
      }
    });
  }

  @Override
  public void onClose(int code, String reason, boolean remote) {
    logger.info("WebSocket đã đóng: {}", reason);
  }

  @Override
  public void onError(Exception ex) {
    logger.error("WebSocket lỗi: {}", ex.getMessage(), ex);
  }

  /**
   * Sends a JSON command to the auction server via WebSocket.
   *
   * @param type the command type identifier
   * @param data the JSON payload for the command
   */
  public void sendCommand(String type, JsonObject data) {
    JsonObject msg = new JsonObject();
    msg.addProperty("type", type);

    // Gop cac thuoc tinh tu data vao msg
    if (data != null) {
      for (String key : data.keySet()) {
        msg.add(key, data.get(key));
      }
    }

    // Gui chuoi JSON qua WebSocket
    if (this.isOpen()) {
      send(msg.toString());
    } else {
      logger.error(
          "WebSocket chưa mở, không thể gửi lệnh: {}", type);
    }
  }
}

