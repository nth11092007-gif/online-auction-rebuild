package Controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.VBox;
import server.AuctionWebSocketClient;

public class AuctionDetailController {

    @FXML
    private VBox Container;

    @FXML
    private Spinner<Integer> bidSpinner;

    @FXML
    private Label txtCurrentPrice;

    @FXML
    private Label txtDescription;

    @FXML
    private Label txtItemID;

    @FXML
    private Label txtItemName;

    private String sessionId;
    private double currentPrice;
    private int stepValue;
    private AuctionWebSocketClient client;

    /**
     * Được gọi từ MainApp.showAuctionDetail() để truyền dữ liệu phiên.
     */
    public void initData(String sessionId, String itemName, double currentPrice, int stepValue) {
        this.sessionId = sessionId;
        this.currentPrice = currentPrice;
        this.stepValue = stepValue;

        txtItemName.setText(itemName);
        txtCurrentPrice.setText(String.valueOf(currentPrice));

        // Cấu hình spinner
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        (int) currentPrice + stepValue,  // min = giá hiện tại + 1 bước
                        Integer.MAX_VALUE,                // max (có thể giới hạn theo số dư sau)
                        (int) currentPrice + stepValue,  // giá trị khởi tạo
                        stepValue                         // bước nhảy
                );
        bidSpinner.setValueFactory(valueFactory);

        // Lấy WebSocket client từ MainApp
        client = MainApp.getWebSocketClient();
        if (client != null) {
            client.setOnMessageCallback(this::handleMessage);

            if (client.isOpen()) {
                client.send("{\"type\":\"JOIN\", \"sessionId\":\"" + sessionId + "\"}");
            } else {
                System.out.println("Socket chưa mở khi JOIN");
            }
        }
    }

    /**
     * Xử lý tin nhắn từ server.
     */
    private void handleMessage(String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String type = json.get("type").getAsString();
            Platform.runLater(() -> {
                switch (type) {
                    case "NEW_BID":
                        double newPrice = json.getAsJsonObject("data").get("newPrice").getAsDouble();
                        currentPrice = newPrice;
                        txtCurrentPrice.setText(String.valueOf(newPrice));
                        // Cập nhật lại spinner min
                        SpinnerValueFactory<Integer> factory =
                                (SpinnerValueFactory<Integer>) bidSpinner.getValueFactory();
                        factory.setValue((int) newPrice + stepValue);
                        break;

                    case "PLACE_BID_RESULT":
                        String result = json.get("data").getAsString();
                        if (!result.contains("thành công")) {
                            showAlert("Đặt giá thất bại", result);
                        }
                        break;

                    case "ERROR":
                        showAlert("Lỗi", json.get("message").getAsString());
                        break;

                    case "SESSION_CLOSED":
                        showAlert("Phiên kết thúc", "Phiên đấu giá đã kết thúc.");
                        bidSpinner.setDisable(true);
                        break;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleQuickBid(ActionEvent event) {
        Button btn = (Button) event.getSource();
        int amountToAdd = 0;

        switch (btn.getText()) {
            case "+0.5M": amountToAdd = 500000; break;
            case "+1.0M": amountToAdd = 1000000; break;
            case "+2.5M": amountToAdd = 2500000; break;
        }

        int newBid = bidSpinner.getValue() + amountToAdd;
        bidSpinner.getValueFactory().setValue(newBid);
    }

    @FXML
    void HandleBid(ActionEvent event) {
        if (client == null || sessionId == null) {
            showAlert("Lỗi", "Chưa kết nối đến máy chủ.");
            return;
        }
        int bidAmount = bidSpinner.getValue();
        String bidMsg = String.format(
            "{\"type\":\"BID\", \"auctionId\":\"%s\", \"amount\":%d}",
            sessionId, bidAmount
        );
        client.send(bidMsg);
    }

    @FXML
    void HandleGoBack(ActionEvent event) {
        // Quay về danh sách phiên (HomeBidder)
        MainApp.showHomeBidder();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}