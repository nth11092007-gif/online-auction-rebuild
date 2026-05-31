package Controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.AuctionSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.AuctionWebSocketClient;

/** MainApp - JavaFX application entry point and screen navigation manager. */
public class MainApp extends Application {

  private static final Logger logger =
      LoggerFactory.getLogger(MainApp.class);
  private static Stage primaryStage;
  private static AuctionWebSocketClient webSocketClient;

  @Override
  public void start(Stage stage) throws Exception {
    primaryStage = stage;
    webSocketClient = new AuctionWebSocketClient();
    webSocketClient.connectBlocking();
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
      logger.error("Lỗi: {}", e.getMessage(), e);
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
      logger.error("Lỗi: {}", e.getMessage(), e);
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
      logger.error("primaryStage chưa được khởi tạo!");
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
      logger.error("Lỗi: {}", e.getMessage(), e);
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
