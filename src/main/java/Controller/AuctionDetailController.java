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
import javafx.application.Platform;
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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import model.AuctionSession;
import model.Bid;
import model.Item;
import server.AuctionWebSocketClient;
import service.AuctionService;
import service.ProxyBiddingService;
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
    @FXML private Label lblStartingPrice;
    @FXML private Label lblIncrementStep;
    @FXML private Button btnPlaceBid;
    @FXML private HBox hboxQuickBids;
    @FXML private ImageView imgItem;

    private static final DateTimeFormatter END_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Item currentItem;
    private String currentSessionId;
    private int stepValue;
    private LocalDateTime countdownStart;
    private LocalDateTime countdownEnd;
    private Timeline timeline;
    private Timeline refreshTimeline;

    private final AuctionService auctionService = new AuctionService();
    private final ProxyBiddingService proxyBiddingService = new ProxyBiddingService(auctionService);
    private final BidDAO bidDAO = new BidDAOImpl();



    @FXML
    public void initialize() {

    }

    public void setAuctionData(AuctionSession session) {
        if (session == null || session.getItem() == null) return;

        this.currentItem = session.getItem();
        this.currentSessionId = session.getSessionID();

        txtItemID.setText("ID: " + currentItem.getItemID());
        txtItemName.setText(currentItem.getItemName());
        txtDescription.setText(currentItem.getDescription());
        BufferedImage bImage = session.getItem().getAvatar();
        if (bImage != null) {
            imgItem.setImage(SwingFXUtils.toFXImage(bImage, null));
        }

        startCountdown();
        refreshAuctionUi();
        startBidListAutoRefresh();
        registerWebSocketBidListener();
        joinWebSocketSession();
    }

    private void joinWebSocketSession() {
        AuctionWebSocketClient client = MainApp.getWebSocketClient();
        if (client != null && client.isOpen() && currentSessionId != null) {
            client.joinSession(currentSessionId);
        }
    }

    /** Đồng bộ toàn bộ nhãn phiên từ DB (giá, bước giá, thời gian, người dẫn đầu). */
    private void applySessionToUi(AuctionSession session) {
        if (session == null) {
            return;
        }

        stepValue = Math.max(1, (int) session.getIncrementStep());

        if (lblStartingPrice != null) {
            lblStartingPrice.setText(String.format("%,.0f ₫", session.getStartingPrice()));
        }
        if (lblIncrementStep != null) {
            lblIncrementStep.setText(String.format("%,.0f ₫", session.getIncrementStep()));
        }

        double currentPrice = session.getCurrentPrice() > 0
                ? session.getCurrentPrice()
                : session.getStartingPrice();
        txtCurrentPrice.setText(String.format("%,.0f VNĐ", currentPrice));
        updateBidSpinner((int) currentPrice);

        if (session.getHighestBidder() != null) {
            lblHighestBidder.setText("Người dẫn đầu: " + session.getHighestBidder().getUsername());
            lblHighestBidder.setStyle("-fx-font-size: 12; -fx-text-fill: #888;");
        } else {
            lblHighestBidder.setText("Chưa có ai đặt giá.");
            lblHighestBidder.setStyle("-fx-font-size: 12; -fx-text-fill: #888;");
        }

        if (session.getStartTime() != null) {
            countdownStart = session.getStartTime();
        }
        if (session.getEndTime() != null) {
            countdownEnd = session.getEndTime();
            lblEndTime.setText("Kết thúc: " + countdownEnd.format(END_TIME_FORMAT));
        }
    }

    private void refreshAuctionUi() {
        if (currentSessionId == null) {
            return;
        }
        loadBidHistory();
        AuctionSession session = auctionService.getSessionById(currentSessionId);
        applySessionToUi(session);
        SessionManager.refreshCurrentUserFromDb();
    }

    private void startBidListAutoRefresh() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> refreshAuctionUi()));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    private void registerWebSocketBidListener() {
        AuctionWebSocketClient client = MainApp.getWebSocketClient();
        if (client == null || currentSessionId == null) {
            return;
        }
        client.setOnMessageCallback(message -> {
            if (message == null || !message.contains("NEW_BID") || !message.contains(currentSessionId)) {
                return;
            }
            Platform.runLater(() -> {
                applyEndTimeFromFeedMessage(message);
                refreshAuctionUi();
            });
        });
    }

    private void applyEndTimeFromFeedMessage(String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            if (!json.has("endTime") || !json.has("sessionId")) {
                return;
            }
            if (!currentSessionId.equals(json.get("sessionId").getAsString())) {
                return;
            }
            countdownEnd = LocalDateTime.parse(json.get("endTime").getAsString());
            lblEndTime.setText("Kết thúc: " + countdownEnd.format(END_TIME_FORMAT));
        } catch (Exception ignored) {
            // refreshAuctionUi sẽ load end_time từ DB
        }
    }

    private void stopAutoRefresh() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
            refreshTimeline = null;
        }
    }

    private void startCountdown() {
        if (timeline != null) {
            timeline.stop();
        }

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (countdownStart == null || countdownEnd == null) {
                return;
            }
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = countdownStart;
            LocalDateTime end = countdownEnd;

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
                timeline.stop();
                refreshHighestBidderAfterClose();
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void refreshHighestBidderAfterClose() {
        AuctionSession session = auctionService.getSessionById(currentSessionId);
        if (session != null && session.getHighestBidder() != null) {
            lblHighestBidder.setText("🏆 Người chiến thắng: " + session.getHighestBidder().getUsername());
            lblHighestBidder.setStyle("-fx-text-fill: #d81b60; -fx-font-weight: bold;");
        } else {
            lblHighestBidder.setText("Vật phẩm chưa được bán.");
            lblHighestBidder.setStyle("-fx-font-size: 12; -fx-text-fill: #888;");
        }
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
            proxyBiddingService.processProxyBids(currentSessionId);
            refreshAuctionUi();
            showAlert("Đặt giá thành công!", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Đặt giá thất bại! Vui lòng kiểm tra lại số dư hoặc đã có người trả giá cao hơn.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void HandleGoBack(ActionEvent event) {
        stopAutoRefresh();
        if (timeline != null) {
            timeline.stop();
        }

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Home.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
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