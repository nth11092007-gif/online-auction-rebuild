package com.auction.client.controller;

import com.auction.client.MainApp;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.auction.client.network.AuctionWebSocketClient;

/** UITest - test harness for launching the JavaFX UI with a WebSocket connection. */
public class UITest extends Application {
  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    // Gán primaryStage cho MainApp để các static method hoạt động
    MainApp.setPrimaryStage(primaryStage);
    // Tạo WebSocket client mới (không dùng singleton)
    AuctionWebSocketClient wsClient = new AuctionWebSocketClient();
    wsClient.connectBlocking();
    MainApp.setWebSocketClient(wsClient);

    FXMLLoader loader =
        new FXMLLoader(getClass().getResource("/Login.fxml"));
    Scene scene = new Scene(loader.load());
    primaryStage.setScene(scene);
    primaryStage.show();
  }
}

