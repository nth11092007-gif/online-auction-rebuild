package Controller;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import dao.BidDAO;
import dao.BidDAOImpl;
import dao.ProxyBidDAO;
import dao.ProxyBidDAOImpl;
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
import javafx.scene.control.*;
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
import model.ProxyBid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AuctionService;
import service.ProxyBiddingService;
import utils.DBConnection;
import utils.SessionManager;

import javax.sql.DataSource;

public class AuctionDetailController {

    @FXML private VBox Container;
    @FXML private VBox bidHistoryContainer;
    @FXML private Label lblBidCount;
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
    @FXML private TextField txtMaxBid;
    @FXML private Button btnSetAutoBid;
    @FXML private Button btnCancelAutoBid;
    @FXML private Label lblAutoBidStatus;
    @FXML private Label lblStartingPrice;
    @FXML private Label lblIncrementStep;


    private Item currentItem;
    private String currentSessionId;
    private int stepValue;
    private Timeline timeline;
    private double currentPriceValue = 0;
    private int creatorId = -1;

    private final AuctionService auctionService = new AuctionService();
    private final ProxyBiddingService proxyBiddingService =
            new ProxyBiddingService(auctionService);
    private final BidDAO bidDAO = new BidDAOImpl();
    private final ProxyBidDAO proxyBidDAO = new ProxyBidDAOImpl();
    private final DataSource dataSource = DBConnection.getDataSource();
    private final Logger logger = LoggerFactory.getLogger(AuctionDetailController.class);

    @FXML
    public void initialize() { }


    public void setAuctionData(AuctionSession session) {
        if (session == null || session.getItem() == null) return;

        this.currentItem = session.getItem();
        this.currentSessionId = session.getSessionID();
        this.stepValue = (int) session.getIncrementStep();
        this.currentPriceValue = session.getCurrentPrice();

        if (session.getSeller() != null) {
            this.creatorId = session.getSeller().getID();
        }

        txtItemID.setText("ID: " + currentItem.getItemID());
        txtItemName.setText(currentItem.getItemName());
        txtDescription.setText(currentItem.getDescription());
        lblStartingPrice.setText(Double.toString(session.getStartingPrice()));
        lblIncrementStep.setText(Double.toString(session.getIncrementStep()));

        BufferedImage bImage = session.getItem().getAvatar();
        if (bImage != null) {
            imgItem.setImage(SwingFXUtils.toFXImage(bImage, null));
        }

        txtCurrentPrice.setText(String.format("%,.0f VND", currentPriceValue));
        updateBidSpinner((int) currentPriceValue);

        if (session.getHighestBidder() != null) {
            lblHighestBidder.setText("Nguoi dan dau: " + session.getHighestBidder().getUsername());
        } else {
            lblHighestBidder.setText("Chua co ai dat gia.");
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        lblEndTime.setText("Ket thuc: " + session.getEndTime().format(fmt));

        startCountdown(session);
        loadBidHistory();

    }





    private void refreshCurrentPrice() {
        try {
            AuctionSession latest = auctionService.getSessionById(currentSessionId);
            if (latest != null) {
                currentPriceValue = latest.getCurrentPrice();
                txtCurrentPrice.setText(String.format("%,.0f VND", currentPriceValue));
                updateBidSpinner((int) currentPriceValue);

                if (latest.getHighestBidder() != null) {
                    lblHighestBidder.setText("Nguoi dan dau: "
                            + latest.getHighestBidder().getUsername());
                }
            }
        } catch (Exception e) {
            logger.error("Loi refresh current price: {}", e.getMessage());
        }
    }

    @FXML
    void HandleBid(ActionEvent event) {
        if (SessionManager.getCurrentUser() == null) {
            showAlert("Vui long dang nhap de dat gia!", Alert.AlertType.WARNING);
            return;
        }
        if (SessionManager.getCurrentUser().getID() == creatorId ) {
            showAlert("Lỗi: Bạn không thể tự đặt giá cho phiên đấu giá của chính mình!", Alert.AlertType.WARNING);
            return;
        }
        if (currentSessionId == null) {
            showAlert("Loi: Khong tim thay thong tin phien dau gia!", Alert.AlertType.ERROR);
            return;
        }

        int bidAmount = bidSpinner.getValue();
        int currentUserId = SessionManager.getCurrentUser().getID();
        boolean isSuccess = auctionService.placeBid(currentUserId, currentSessionId, bidAmount);

        if (isSuccess) {
            currentPriceValue = bidAmount;
            txtCurrentPrice.setText(String.format("%,d VND", bidAmount));
            updateBidSpinner(bidAmount);
            SessionManager.getCurrentUser().withdraw(bidAmount);
            lblHighestBidder.setText("Nguoi dan dau: "
                    + SessionManager.getCurrentUser().getUsername());

            loadBidHistory();
            refreshCurrentPrice();

            showAlert("Dat gia thanh cong!", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Dat gia that bai! Kiem tra lai so du hoac da co nguoi tra gia cao hon.",
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    void handleQuickBid(ActionEvent event) {
        Button btn = (Button) event.getSource();
        int add = switch (btn.getText()) {
            case "+0.5M" -> 500_000;
            case "+1.0M" -> 1_000_000;
            case "+2.5M" -> 2_500_000;
            default      -> 0;
        };
        bidSpinner.getValueFactory().setValue(bidSpinner.getValue() + add);
    }

    private void loadBidHistory() {
        if (bidHistoryContainer == null || currentSessionId == null) return;
        bidHistoryContainer.getChildren().clear();

        List<Bid> bids = bidDAO.getBidsBySession(currentSessionId);
        if (lblBidCount != null) lblBidCount.setText(bids.size() + " luot");

        if (bids.isEmpty()) {
            bidHistoryContainer.getChildren().add(buildBidEmptyState());
            return;
        }
        for (int i = 0; i < bids.size(); i++) {
            bidHistoryContainer.getChildren().add(buildBidRow(bids.get(i), i));
        }
    }

    private HBox buildBidRow(Bid bid, int rank) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        boolean isTop = rank == 0;
        row.setStyle(isTop ? """
                -fx-background-color: #f0f7ff;
                -fx-background-radius: 10;
                -fx-border-color: #b3d1ff;
                -fx-border-radius: 10;
                -fx-border-width: 1.5;
                -fx-padding: 10 14;
                """ : """
                -fx-background-color: white;
                -fx-background-radius: 10;
                -fx-border-color: #f0f4ff;
                -fx-border-radius: 10;
                -fx-border-width: 1.5;
                -fx-padding: 10 14;
                """);

        String username = (bid.getBidder() != null) ? bid.getBidder().getUsername() : "An danh";
        Label name = new Label(username);
        name.setStyle(isTop
                ? "-fx-font-size: 13; -fx-text-fill: #1a1a2e; -fx-font-weight: bold;"
                : "-fx-font-size: 13; -fx-text-fill: #444;");
        HBox.setHgrow(name, Priority.ALWAYS);

        Label amount = new Label(String.format("%,.0f d", bid.getAmount()));
        amount.setPrefWidth(150);
        amount.setStyle(isTop
                ? "-fx-font-size: 14; -fx-text-fill: #1a73e8; -fx-font-weight: bold;"
                : "-fx-font-size: 13; -fx-text-fill: #555;");

        String rankText  = rank < 3 ? "#" + (rank + 1) : "#" + (rank + 1);
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

    // ─── COUNTDOWN ───────────────────────────────────────────────────

    private void startCountdown(AuctionSession session) {
        if (timeline != null) timeline.stop();

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LocalDateTime now   = LocalDateTime.now();
            LocalDateTime start = session.getStartTime();
            LocalDateTime end   = session.getEndTime();

            if (now.isBefore(start)) {
                setStatus("Sap dien ra", "#fbbc04");
                lblTimeRemaining.setText("Bat dau sau: " + formatDuration(now, start));
                setBiddingEnabled(false);


            } else if (now.isBefore(end)) {
                setStatus("Dang dau gia", "#34a853");
                lblTimeRemaining.setText("Con: " + formatDuration(now, end));
                setBiddingEnabled(true);

            } else {
                setStatus("Da ket thuc", "#ea4335");
                lblTimeRemaining.setText("Phien dau gia da khep lai");
                setBiddingEnabled(false);

                if (session.getHighestBidder() != null) {
                    lblHighestBidder.setText("Nguoi chien thang: "
                            + session.getHighestBidder().getUsername());
                    lblHighestBidder.setStyle("-fx-text-fill: #d81b60; -fx-font-weight: bold;");
                } else {
                    lblHighestBidder.setText("Vat pham chua duoc ban.");
                }
                timeline.stop();
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void setStatus(String text, String color) {
        lblStatus.setText(text);
        lblStatus.setStyle("-fx-background-color: " + color
                + "; -fx-text-fill: white; -fx-padding: 6 14; -fx-background-radius: 20;"
                + "-fx-font-size: 12; -fx-font-weight: bold;");
    }

    private void setBiddingEnabled(boolean enabled) {
        if (btnPlaceBid != null) btnPlaceBid.setDisable(!enabled);
        if (hboxQuickBids != null) hboxQuickBids.setDisable(!enabled);
        if (bidSpinner != null) bidSpinner.setDisable(!enabled);
    }

    private String formatDuration(LocalDateTime from, LocalDateTime to) {
        long days    = ChronoUnit.DAYS.between(from, to);   from = from.plusDays(days);
        long hours   = ChronoUnit.HOURS.between(from, to);  from = from.plusHours(hours);
        long minutes = ChronoUnit.MINUTES.between(from, to); from = from.plusMinutes(minutes);
        long seconds = ChronoUnit.SECONDS.between(from, to);
        return days > 0
                ? String.format("%d ngay %02d:%02d:%02d", days, hours, minutes, seconds)
                : String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void updateBidSpinner(int currentPrice) {
        bidSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                currentPrice + stepValue, Integer.MAX_VALUE,
                currentPrice + stepValue, stepValue));
    }


    @FXML
    void HandleGoBack(ActionEvent event) {
        if (timeline != null) timeline.stop();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Home.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Khong the quay lai man hinh chinh!", Alert.AlertType.ERROR);
        }
    }


    private void showAlert(String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}