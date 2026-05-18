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

    // Phương thức chung để hiển thị màn hình chính (có thể dùng cho HomeBidder hoặc HomeSeller)
    public static void showHome() {
        // Tùy chỉnh theo logic role, ở đây tạm thời dùng HomeBidder
        showHomeBidder();
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
            // Gọi phương thức setAuctionData thay vì initData
            controller.setAuctionData(session);
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Các setter dùng cho mục đích test (có thể bỏ nếu không cần)
    public static void setPrimaryStage(Stage stage) { primaryStage = stage; }
    public static void setWebSocketClient(AuctionWebSocketClient client) { webSocketClient = client; }
}