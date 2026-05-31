package server;

import com.google.gson.JsonObject;
import java.net.URI;
import java.util.function.Consumer;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.AppConfig;

/** AuctionWebSocketClient - WebSocket client for connecting to the auction server. */
public class AuctionWebSocketClient extends WebSocketClient {

  private static final Logger logger =
      LoggerFactory.getLogger(AuctionWebSocketClient.class);

  private Consumer<String> onMessageCallback;

  public AuctionWebSocketClient(URI serverUri) {
    super(serverUri);
  }

  /**
   * Tao client moi voi URI mac dinh.
   * Moi client instance nen tao 1 AuctionWebSocketClient rieng
   * de co ket noi WebSocket doc lap.
   */
  public AuctionWebSocketClient() {
    this(URI.create(AppConfig.getWsUri()));
  }

  public void setOnMessageCallback(Consumer<String> callback) {
    this.onMessageCallback = callback;
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
        logger.info("Dang ket noi WebSocket (lan {}/{})...",
            i + 1, maxRetries);
        connectBlocking();
        if (isOpen()) {
          logger.info("WebSocket da ket noi thanh cong.");
          return true;
        }
      } catch (Exception e) {
        logger.warn("Ket noi WebSocket that bai (lan {}): {}",
            i + 1, e.getMessage());
      }
      if (i < maxRetries - 1) {
        try {
          Thread.sleep(delayMs);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          logger.warn("Bi gian doan trong khi doi ket noi lai.");
          return false;
        }
      }
    }
    logger.error(
        "Khong the ket noi WebSocket sau {} lan thu.", maxRetries);
    return false;
  }

  @Override
  public void onOpen(ServerHandshake handshake) {
    logger.info("WebSocket connected to server");
  }

  @Override
  public void onMessage(String message) {
    if (onMessageCallback != null) {
      // Dam bao cap nhat UI tren JavaFX Application Thread
      javafx.application.Platform.runLater(
          () -> onMessageCallback.accept(message));
    }
  }

  @Override
  public void onClose(int code, String reason, boolean remote) {
    logger.info("WebSocket closed: {}", reason);
  }

  @Override
  public void onError(Exception ex) {
    logger.error("WebSocket error: {}", ex.getMessage(), ex);
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
