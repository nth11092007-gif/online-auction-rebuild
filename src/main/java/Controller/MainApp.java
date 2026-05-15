package Controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.AuctionSession;
import server.AuctionWebSocketClient;

public class MainApp extends Application {
    private static Stage primaryStage;
    private static AuctionWebSocketClient webSocketClient;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        webSocketClient = AuctionWebSocketClient.getInstance();
        webSocketClient.connect();
        showLogin(); // hoặc showHomeBidder nếu muốn test
    }

    public static AuctionWebSocketClient getWebSocketClient() {
        return webSocketClient;
    }

    public static void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/Login.fxml"));
            Parent root = loader.load();
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showHomeBidder() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/HomeBidder.fxml"));
            Parent root = loader.load();
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showAuctionDetail(AuctionSession session) {
        if (primaryStage == null) {
            System.err.println("primaryStage chưa được khởi tạo!");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/AuctionDetail.fxml"));
            Parent root = loader.load();
            AuctionDetailController controller = loader.getController();
            controller.initData(
                session.getSessionID(),
                session.getItem().getDescription(),
                session.getCurrentPrice(),
                (int) session.getIncrementStep()
            );
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
    public static void setPrimaryStage(Stage stage) { primaryStage = stage; }
    public static void setWebSocketClient(AuctionWebSocketClient client) { webSocketClient = client; }
}