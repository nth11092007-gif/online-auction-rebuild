package Controller;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import dao.BidDAO;
import dao.BidDAOImpl;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.AuctionSession;
import model.Bid;
import model.Item;
import service.AuctionService;
import utils.SessionManager;

public class AuctionDetailController {

    @FXML private VBox Container;
    @FXML private VBox bidHistoryContainer;   // mới
    @FXML private Label lblBidCount;          // mới
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
    @FXML private ImageView imgItem;

    private Item currentItem;
    private String currentSessionId;
    private int stepValue;
    private Timeline timeline;

    private final AuctionService auctionService = new AuctionService();
    private final BidDAO bidDAO = new BidDAOImpl();



    @FXML
    public void initialize() {

    }

    public void setAuctionData(AuctionSession session) {
        if (session == null || session.getItem() == null) return;

        this.currentItem = session.getItem();
        this.currentSessionId = session.getSessionID();

        this.stepValue = (int) session.getIncrementStep();
        txtItemID.setText("ID: " + currentItem.getItemID());
        txtItemName.setText(currentItem.getItemName());
        txtDescription.setText(currentItem.getDescription());
        BufferedImage bImage = session.getItem().getAvatar();
        Image fxImage = SwingFXUtils.toFXImage(bImage, null);
        imgItem.setImage(fxImage);


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
        loadBidHistory();
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
                        Integer.MAX_VALUE,
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
    private void loadBidHistory() {
        if (bidHistoryContainer == null || currentSessionId == null) return;
        bidHistoryContainer.getChildren().clear();

        List<Bid> bids = bidDAO.getBidsBySession(currentSessionId); // ← bỏ limit
        int total = bids.size(); // ← đếm trực tiếp từ list, không cần query thêm

        if (lblBidCount != null) {
            lblBidCount.setText(total + " luot");
        }

        if (bids.isEmpty()) {
            Label empty = new Label("Chua co ai dat gia trong phien nay");
            empty.setStyle("-fx-font-size: 13; -fx-text-fill: #bbb; -fx-padding: 12 0;");
            bidHistoryContainer.getChildren().add(empty);
            return;
        }

        for (int i = 0; i < bids.size(); i++) {
            bidHistoryContainer.getChildren().add(buildBidRow(bids.get(i), i));
        }
    }
    private HBox buildBidRow(Bid bid, int rank) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);

        // Top 1 nổi bật hơn
        boolean isTop = rank == 0;
        row.setStyle(isTop
                ? """
              -fx-background-color: #f0f7ff;
              -fx-background-radius: 10;
              -fx-border-color: #b3d1ff;
              -fx-border-radius: 10;
              -fx-border-width: 1.5;
              -fx-padding: 10 14;
              """
                : """
              -fx-background-color: white;
              -fx-background-radius: 10;
              -fx-border-color: #f0f4ff;
              -fx-border-radius: 10;
              -fx-border-width: 1.5;
              -fx-padding: 10 14;
              """);

        // Tên người đặt
        String username = (bid.getBidder() != null)
                ? bid.getBidder().getUsername() : "An danh";
        Label name = new Label(username);
        name.setStyle(isTop
                ? "-fx-font-size: 13; -fx-text-fill: #1a1a2e; -fx-font-weight: bold;"
                : "-fx-font-size: 13; -fx-text-fill: #444;");
        HBox.setHgrow(name, Priority.ALWAYS);

        // Số tiền
        Label amount = new Label(String.format("%,.0f d", bid.getAmount()));
        amount.setPrefWidth(150);
        amount.setStyle(isTop
                ? "-fx-font-size: 14; -fx-text-fill: #1a73e8; -fx-font-weight: bold;"
                : "-fx-font-size: 13; -fx-text-fill: #555;");

        // Hạng
        String rankText = switch (rank) {
            case 0 -> "#1";
            case 1 -> "#2";
            case 2 -> "#3";
            default -> "#" + (rank + 1);
        };
        String rankStyle = switch (rank) {
            case 0 -> "-fx-background-color: #ffd700; -fx-text-fill: #7a5c00;";
            case 1 -> "-fx-background-color: #e0e0e0; -fx-text-fill: #555;";
            case 2 -> "-fx-background-color: #ffe0cc; -fx-text-fill: #b85c00;";
            default -> "-fx-background-color: #f5f5f5; -fx-text-fill: #888;";
        };
        Label rankLabel = new Label(rankText);
        rankLabel.setPrefWidth(60);
        rankLabel.setAlignment(Pos.CENTER);
        rankLabel.setStyle(rankStyle
                + "-fx-font-size: 11; -fx-font-weight: bold;"
                + "-fx-background-radius: 6; -fx-padding: 3 6;");

        row.getChildren().addAll(name, amount, rankLabel);
        return row;
    }

    private Label buildBidEmptyState() {
        Label empty = new Label("Chua co ai dat gia trong phien nay");
        empty.setStyle("-fx-font-size: 13; -fx-text-fill: #bbb; -fx-padding: 16 0;");
        return empty;
    }
}