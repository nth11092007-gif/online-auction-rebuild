package Controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.AuctionSession;
import model.Item;
import service.AuctionService;
import utils.SessionManager;

public class AuctionDetailController {

    @FXML private VBox Container;
    @FXML private Spinner<Integer> bidSpinner;
    @FXML private Label txtCurrentPrice;
    @FXML private Label txtDescription;
    @FXML private Label txtItemID;
    @FXML private Label txtItemName;
    @FXML private Label lblStatus;
    @FXML private Label lblTimeRemaining;
    @FXML private Label lblEndTime;
    @FXML private Label lblHighestBidder;
    @FXML private Button btnPlaceBid;
    @FXML private HBox hboxQuickBids;

    private Item currentItem;
    private String currentSessionId;
    private int stepValue;

    private Timeline timeline;
    private final AuctionService auctionService = new AuctionService();

    @FXML
    public void initialize() {

    }

    public void setAuctionData(AuctionSession session) {
        if (session == null || session.getItem() == null) return;

        this.currentItem = session.getItem();
        this.currentSessionId = session.getSessionID();

        this.stepValue = (int) session.getIncrementStep();
        txtItemID.setText("ID: " + currentItem.getItemID());
        txtItemName.setText(currentItem.getClass().getSimpleName());
        txtDescription.setText(currentItem.getDescription());

        double currentPrice = session.getCurrentPrice();
        txtCurrentPrice.setText(String.format("%,.0f VNĐ", currentPrice));

        updateBidSpinner((int) currentPrice);

        // Hiển thị người dẫn đầu hiện tại
        if (session.getHighestBidder() != null) {
            lblHighestBidder.setText("Người dẫn đầu: " + session.getHighestBidder().getUsername());
        } else {
            lblHighestBidder.setText("Chưa có ai đặt giá.");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        lblEndTime.setText("Kết thúc: " + session.getEndTime().format(formatter));

        startCountdown(session);
    }

    private void startCountdown(AuctionSession session) {
        if (timeline != null) {
            timeline.stop();
        }

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = session.getStartTime();
            LocalDateTime end = session.getEndTime();

            if (now.isBefore(start)) {
                lblStatus.setText("Sắp diễn ra");
                lblStatus.setStyle("-fx-background-color: #fbbc04; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 20;");
                lblTimeRemaining.setText("Bắt đầu sau: " + formatDuration(now, start));
                setBiddingEnabled(false);

            } else if (now.isBefore(end)) {
                lblStatus.setText("Đang đấu giá");
                lblStatus.setStyle("-fx-background-color: #34a853; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 20;");
                lblTimeRemaining.setText("Còn: " + formatDuration(now, end));
                setBiddingEnabled(true);

            } else {
                lblStatus.setText("Đã kết thúc");
                lblStatus.setStyle("-fx-background-color: #ea4335; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 20;");
                lblTimeRemaining.setText("Phiên đấu giá đã khép lại");
                setBiddingEnabled(false);

                if (session.getHighestBidder() != null) {
                    lblHighestBidder.setText("🏆 Người chiến thắng: " + session.getHighestBidder().getUsername());
                    lblHighestBidder.setStyle("-fx-text-fill: #d81b60; -fx-font-weight: bold;");
                } else {
                    lblHighestBidder.setText("Vật phẩm chưa được bán.");
                }

                timeline.stop();
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private String formatDuration(LocalDateTime from, LocalDateTime to) {
        long days = ChronoUnit.DAYS.between(from, to);
        from = from.plusDays(days);

        long hours = ChronoUnit.HOURS.between(from, to);
        from = from.plusHours(hours);

        long minutes = ChronoUnit.MINUTES.between(from, to);
        from = from.plusMinutes(minutes);

        long seconds = ChronoUnit.SECONDS.between(from, to);

        if (days > 0) {
            return String.format("%d ngày %02d:%02d:%02d", days, hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
    }

    private void setBiddingEnabled(boolean enabled) {
        if (btnPlaceBid != null) btnPlaceBid.setDisable(!enabled);
        if (hboxQuickBids != null) hboxQuickBids.setDisable(!enabled);
        if (bidSpinner != null) bidSpinner.setDisable(!enabled);
    }

    private void updateBidSpinner(int currentPrice) {

    }

    public void setAuctionData(AuctionSession session) {
        if (session == null || session.getItem() == null) return;

        this.currentItem = session.getItem();
        this.currentSessionId = session.getSessionID();

        this.stepValue = (int) session.getIncrementStep();
        txtItemID.setText("ID: " + currentItem.getItemID());
        txtItemName.setText(currentItem.getClass().getSimpleName());
        txtDescription.setText(currentItem.getDescription());

        double currentPrice = session.getCurrentPrice();
        txtCurrentPrice.setText(String.format("%,.0f VNĐ", currentPrice));

        updateBidSpinner((int) currentPrice);

        // Hiển thị người dẫn đầu hiện tại
        if (session.getHighestBidder() != null) {
            lblHighestBidder.setText("Người dẫn đầu: " + session.getHighestBidder().getUsername());
        } else {
            lblHighestBidder.setText("Chưa có ai đặt giá.");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        lblEndTime.setText("Kết thúc: " + session.getEndTime().format(formatter));

        startCountdown(session);
    }

    private void startCountdown(AuctionSession session) {
        if (timeline != null) {
            timeline.stop();
        }

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = session.getStartTime();
            LocalDateTime end = session.getEndTime();

            if (now.isBefore(start)) {
                lblStatus.setText("Sắp diễn ra");
                lblStatus.setStyle("-fx-background-color: #fbbc04; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 20;");
                lblTimeRemaining.setText("Bắt đầu sau: " + formatDuration(now, start));
                setBiddingEnabled(false);

            } else if (now.isBefore(end)) {
                lblStatus.setText("Đang đấu giá");
                lblStatus.setStyle("-fx-background-color: #34a853; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 20;");
                lblTimeRemaining.setText("Còn: " + formatDuration(now, end));
                setBiddingEnabled(true);

            } else {
                lblStatus.setText("Đã kết thúc");
                lblStatus.setStyle("-fx-background-color: #ea4335; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 20;");
                lblTimeRemaining.setText("Phiên đấu giá đã khép lại");
                setBiddingEnabled(false);

                if (session.getHighestBidder() != null) {
                    lblHighestBidder.setText("🏆 Người chiến thắng: " + session.getHighestBidder().getUsername());
                    lblHighestBidder.setStyle("-fx-text-fill: #d81b60; -fx-font-weight: bold;");
                } else {
                    lblHighestBidder.setText("Vật phẩm chưa được bán.");
                }

                timeline.stop();
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private String formatDuration(LocalDateTime from, LocalDateTime to) {
        long days = ChronoUnit.DAYS.between(from, to);
        from = from.plusDays(days);

        long hours = ChronoUnit.HOURS.between(from, to);
        from = from.plusHours(hours);

        long minutes = ChronoUnit.MINUTES.between(from, to);
        from = from.plusMinutes(minutes);

        long seconds = ChronoUnit.SECONDS.between(from, to);

        if (days > 0) {
            return String.format("%d ngày %02d:%02d:%02d", days, hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
    }

    private void setBiddingEnabled(boolean enabled) {
        if (btnPlaceBid != null) btnPlaceBid.setDisable(!enabled);
        if (hboxQuickBids != null) hboxQuickBids.setDisable(!enabled);
        if (bidSpinner != null) bidSpinner.setDisable(!enabled);
    }

    private void updateBidSpinner(int currentPrice) {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        currentPrice + stepValue,
                        currentPrice + stepValue,
                        Integer.MAX_VALUE,
                        currentPrice + stepValue,
                        currentPrice + stepValue,
                        stepValue
                );
        bidSpinner.setValueFactory(valueFactory);
    }


    @FXML
    void handleQuickBid(ActionEvent event) {
        Button btn = (Button) event.getSource();
        int amountToAdd = 0;
        switch (btn.getText()) {
            case "+0.5M":
                amountToAdd = 500000;
                break;
            case "+1.0M":
                amountToAdd = 1000000;
                break;
            case "+2.5M":
                amountToAdd = 2500000;
                break;
            default:
                amountToAdd = 0;
}

        int newBid = bidSpinner.getValue() + amountToAdd;
        bidSpinner.getValueFactory().setValue(newBid);
    }

    @FXML
    void HandleBid(ActionEvent event) {
        if (SessionManager.getCurrentUser() == null) {
            showAlert("Vui lòng đăng nhập để đặt giá!", Alert.AlertType.WARNING);
            return;
        }

        if (currentSessionId == null) {
            showAlert("Lỗi: Không tìm thấy thông tin phiên đấu giá!", Alert.AlertType.ERROR);
            return;
        }

        int bidAmount = bidSpinner.getValue();
        int currentUserId = SessionManager.getCurrentUser().getID();
        System.out.println("Bid on session: " + currentSessionId + " amount: " + bidAmount);
        boolean isSuccess = auctionService.placeBid(currentUserId, currentSessionId, bidAmount);

        if (isSuccess) {
            txtCurrentPrice.setText(String.format("%,d VNĐ", bidAmount));
            updateBidSpinner(bidAmount);
            double currentBalance = SessionManager.getCurrentUser().getBalance();
            double currentFrozen = SessionManager.getCurrentUser().getFrozenBalance();
            SessionManager.getCurrentUser().withdraw(bidAmount);

            // Cập nhật người dẫn đầu ngay lập tức trên giao diện
            lblHighestBidder.setText("Người dẫn đầu: " + SessionManager.getCurrentUser().getUsername());

            showAlert("Đặt giá thành công!", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Đặt giá thất bại! Vui lòng kiểm tra lại số dư hoặc đã có người trả giá cao hơn.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void HandleGoBack(ActionEvent event) {
        if (timeline != null) {
            timeline.stop();
        }

        if (timeline != null) {
            timeline.stop();
        }

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Home.fxml"));
            Parent root = FXMLLoader.load(getClass().getResource("/Home.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Không thể quay lại màn hình Home.fxml");
            System.err.println("Không thể quay lại màn hình Home.fxml");
            e.printStackTrace();
        }
    }

    private void showAlert(String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
    private void showAlert(String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}