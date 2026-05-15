package Controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import server.AuctionWebSocketClient;

public class UITest extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Gán primaryStage cho MainApp để các static method hoạt động
        MainApp.setPrimaryStage(primaryStage);
        MainApp.setWebSocketClient(AuctionWebSocketClient.getInstance()); // nếu cần

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}