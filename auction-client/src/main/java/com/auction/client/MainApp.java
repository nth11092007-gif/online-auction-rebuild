package com.auction.client;

import com.auction.client.controller.AuctionDetailController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.auction.common.model.AuctionSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.auction.client.network.AuctionWebSocketClient;

/** MainApp - JavaFX application entry point and screen navigation manager. */
public class MainApp extends Application {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MainApp.class);
  private static final int MAX_RETRY_COUNT = 5;
  private static final int RETRY_DELAY_MS = 2000;
  private static Stage primaryStage;
  private static AuctionWebSocketClient webSocketClient;

  @Override
  public void start(Stage stage) throws Exception {
    primaryStage = stage;
    webSocketClient = new AuctionWebSocketClient();
    webSocketClient.connectWithRetry(MAX_RETRY_COUNT, RETRY_DELAY_MS);
    showLogin();
  }

  public static AuctionWebSocketClient getWebSocketClient() {
    return webSocketClient;
  }

  /** Navigates the primary stage to the login screen. */
  public static void showLogin() {
    try {
      FXMLLoader loader =
          new FXMLLoader(MainApp.class.getResource("/Login.fxml"));
      Parent root = loader.load();
      primaryStage.setScene(new Scene(root));
      primaryStage.show();
    } catch (Exception e) {
      LOGGER.error("Lỗi: {}", e.getMessage(), e);
    }
  }

  /** Navigates the primary stage to the bidder home screen. */
  public static void showHomeBidder() {
    try {
      FXMLLoader loader =
          new FXMLLoader(MainApp.class.getResource("/HomeBidder.fxml"));
      Parent root = loader.load();
      primaryStage.setScene(new Scene(root));
      primaryStage.show();
    } catch (Exception e) {
      LOGGER.error("Lỗi: {}", e.getMessage(), e);
    }
  }

  public static void showHome() {
    showHomeBidder();
  }

  /**
   * Navigates the primary stage to the auction detail screen.
   *
   * @param session the auction session whose detail view to display
   */
  public static void showAuctionDetail(AuctionSession session) {
    if (primaryStage == null) {
      LOGGER.error("primaryStage chưa được khởi tạo!");
      return;
    }
    try {
      FXMLLoader loader = new FXMLLoader(
          MainApp.class.getResource("/AuctionDetail.fxml"));
      Parent root = loader.load();
      AuctionDetailController controller = loader.getController();
      controller.setAuctionData(session);
      primaryStage.setScene(new Scene(root));
      primaryStage.show();
    } catch (Exception e) {
      LOGGER.error("Lỗi: {}", e.getMessage(), e);
    }
  }

  public static void main(String[] args) {
    launch(args);
  }

  public static void setPrimaryStage(Stage stage) {
    primaryStage = stage;
  }

  public static void setWebSocketClient(
      AuctionWebSocketClient client) {
    webSocketClient = client;
  }
}




