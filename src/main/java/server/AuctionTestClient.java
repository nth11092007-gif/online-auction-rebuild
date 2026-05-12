package server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class AuctionTestClient extends Application {

    private WebSocketClient webSocketClient;
    private TextArea logArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // 1. Khởi tạo các thành phần giao diện (UI)
        TextField sessionInput = new TextField("SS001");
        sessionInput.setPromptText("Nhập mã phiên (VD: SS001)");

        Button btnJoin = new Button("Tham gia phiên (JOIN)");

        TextField bidInput = new TextField("500");
        bidInput.setPromptText("Nhập giá tiền");

        Button btnBid = new Button("Đặt giá (BID)");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(200);

        // 2. Gắn sự kiện cho các nút bấm
        btnJoin.setOnAction(e -> {
            String sessionId = sessionInput.getText();
            // Tạo chuỗi JSON thủ công cho nhanh để test
            String joinMsg = String.format("{\"type\":\"JOIN\", \"sessionId\":\"%s\"}", sessionId);
            sendToServer(joinMsg);
        });

        btnBid.setOnAction(e -> {
            String sessionId = sessionInput.getText();
            String amount = bidInput.getText();
            String bidMsg = String.format("{\"type\":\"BID\", \"userId\":1, \"sessionId\":\"%s\", \"bidAmount\":%s}", sessionId, amount);
            sendToServer(bidMsg);
        });

        // Cấu hình Layout
        VBox root = new VBox(10, sessionInput, btnJoin, bidInput, btnBid, logArea);
        root.setPadding(new Insets(15));

        primaryStage.setScene(new Scene(root, 350, 400));
        primaryStage.setTitle("Client Đấu Giá Test");
        primaryStage.show();

        // 3. Kết nối tới Server ngay khi mở app
        connectToServer();
    }

    private void connectToServer() {
        try {
            webSocketClient = new WebSocketClient(new URI("ws://localhost:8080")) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    logMessage("🟢 Đã kết nối tới Server!");
                }

                @Override
                public void onMessage(String message) {
                    logMessage("📩 Nhận từ Server: " + message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    logMessage("🔴 Đã ngắt kết nối.");
                }

                @Override
                public void onError(Exception ex) {
                    logMessage("❌ Lỗi: " + ex.getMessage());
                }
            };
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void sendToServer(String message) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(message);
            logMessage("📤 Đã gửi: " + message);
        } else {
            logMessage("⚠️ Chưa kết nối được Server!");
        }
    }

    // Hàm phụ trợ để in log lên TextArea an toàn từ luồng khác
    private void logMessage(String msg) {
        Platform.runLater(() -> logArea.appendText(msg + "\n"));
    }
}