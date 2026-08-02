package com.auction.client.controller;

import com.auction.client.MainApp;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import com.auction.common.model.AuctionSession;
import com.auction.common.model.Bid;
import com.auction.common.model.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.auction.client.network.AuctionWebSocketClient;
import com.auction.client.utils.AlertUtils;
import com.auction.client.utils.NavigationManager;
import com.auction.client.utils.SessionManager;


/** AuctionDetailController - controls the auction detail view with bidding and countdown. */
public class AuctionDetailController {

  @FXML
  private VBox Container;
  @FXML
  private VBox bidHistoryContainer;
  @FXML
  private Label lblBidCount;
  @FXML
  private Spinner<Double> bidSpinner;
  @FXML
  private Label txtCurrentPrice;
  @FXML
  private Label txtDescription;
  @FXML
  private Label txtItemID;
  @FXML
  private Label txtItemName;
  @FXML
  private Label lblStatus;
  @FXML
  private Label lblTimeRemaining;
  @FXML
  private Label lblEndTime;
  @FXML
  private Label lblHighestBidder;
  @FXML
  private Button btnPlaceBid;
  @FXML
  private HBox hboxQuickBids;
  @FXML
  private ImageView imgItem;
  @FXML
  private Label lblStartingPrice;
  @FXML
  private Label lblIncrementStep;

  private Item currentItem;
  private String currentSessionId;
  private int stepValue;
  private Timeline timeline;
  private double currentPriceValue = 0;
  private int creatorId = -1;
  private LocalDateTime currentEndTime;

    private final Logger logger =
      LoggerFactory.getLogger(AuctionDetailController.class);

  @FXML
  public void initialize() { }

  /**
   * Populates the view with auction session data and starts the countdown.
   *
   * @param session the auction session to display
   */
  public void setAuctionData(AuctionSession session) {
    if (session == null || session.getItem() == null) {
      return;
    }
    // Ghi chú: Đáng ra phải gọi WebSocket "GET_SESSION" để làm mới, tạm thời dùng data cũ truyền sang
    this.currentItem = session.getItem();
    this.currentSessionId = session.getSessionId();
    this.stepValue = (int) session.getIncrementStep();
    this.currentPriceValue = session.getCurrentPrice();

    if (session.getSeller() != null) {
      this.creatorId = session.getSeller().getId();
    }

    txtItemID.setText(
        "ID: " + currentItem.getItemId());
    txtItemName.setText(currentItem.getItemName());
    txtDescription.setText(currentItem.getDescription());
    lblStartingPrice.setText(
        Double.toString(session.getStartingPrice()));
    lblIncrementStep.setText(
        Double.toString(session.getIncrementStep()));

    BufferedImage bufferedImage = session.getItem().getAvatar();
    if (bufferedImage != null) {
      imgItem.setImage(
          SwingFXUtils.toFXImage(bufferedImage, null));
    }

    txtCurrentPrice.setText(
        String.format("%,.0f VND", currentPriceValue));
    updateBidSpinner(currentPriceValue);

    if (session.getHighestBidder() != null) {
      lblHighestBidder.setText("Người dẫn đầu: "
          + session.getHighestBidder().getUsername());
    } else {
      lblHighestBidder.setText("Chưa có ai đặt giá.");
    }

    DateTimeFormatter fmt =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    lblEndTime.setText(
        "Kết thúc: " + session.getEndTime().format(fmt));

    startCountdown(session);
    loadBidHistory();
    setupWebSocket();
  }

  private void refreshCurrentPrice() {
    // Tạm thời ẩn đi vì không gọi thẳng DB nữa. Sẽ cập nhật giá thông qua WebSockets message "BID_UPDATE".
  }

  @FXML
  void handleBid(ActionEvent event) {
    if (SessionManager.getCurrentUser() == null) {
      showAlert("Vui lòng đăng nhập để đặt giá!",
          Alert.AlertType.WARNING);
      return;
    }
    if (SessionManager.getCurrentUser().getId()
        == creatorId) {
      showAlert(
          "Lỗi: Bạn không thể tự đặt giá cho "
          + "phiên đấu giá của chính mình!",
          Alert.AlertType.WARNING);
      return;
    }
    if (currentSessionId == null) {
      showAlert(
          "Lỗi: Không tìm thấy thông tin phiên đấu giá!",
          Alert.AlertType.ERROR);
      return;
    }

    btnPlaceBid.setDisable(true);
    btnPlaceBid.setText("Đang đặt giá...");

    double bidAmount = bidSpinner.getValue();
    JsonObject bidData = new JsonObject();
    bidData.addProperty("auctionId", currentSessionId);
    bidData.addProperty("amount", bidAmount);

    AuctionWebSocketClient wsClient =
        MainApp.getWebSocketClient();
    if (wsClient == null || !wsClient.isOpen()) {
      showAlert(
          "Không thể kết nối đến server đấu giá. "
          + "Vui lòng kiểm tra server và thử lại!",
          Alert.AlertType.ERROR);
      btnPlaceBid.setDisable(false);
      btnPlaceBid.setText("Đặt giá");
      return;
    }
    wsClient.sendCommand("BID", bidData);
  }

  @FXML
  void handleQuickBid(ActionEvent event) {
    Button btn = (Button) event.getSource();
    double add = switch (btn.getText()) {
      case "+0.5M" -> 500_000.0;
      case "+1.0M" -> 1_000_000.0;
      case "+2.5M" -> 2_500_000.0;
      default -> 0.0;
    };
    bidSpinner.getValueFactory().setValue(
        bidSpinner.getValue() + add);
  }

  private void loadBidHistory() {
    if (bidHistoryContainer == null
        || currentSessionId == null) {
      return;
    }
    bidHistoryContainer.getChildren().clear();

    // Tạm thời gửi một danh sách trống để tránh gọi thẳng DB
    List<Bid> bids = new java.util.ArrayList<>();
    if (lblBidCount != null) {
      lblBidCount.setText(bids.size() + " lượt");
    }

    if (bids.isEmpty()) {
      bidHistoryContainer.getChildren()
          .add(buildBidEmptyState());
      return;
    }
    for (int i = 0; i < bids.size(); i++) {
      bidHistoryContainer.getChildren()
          .add(buildBidRow(bids.get(i), i));
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

    String username = (bid.getBidder() != null)
        ? bid.getBidder().getUsername() : "Ẩn danh";
    Label name = new Label(username);
    name.setStyle(isTop
        ? "-fx-font-size: 13; -fx-text-fill: #1a1a2e;"
          + " -fx-font-weight: bold;"
        : "-fx-font-size: 13; -fx-text-fill: #444;");
    HBox.setHgrow(name, Priority.ALWAYS);

    Label amount = new Label(
        String.format("%,.0f d", bid.getAmount()));
    amount.setPrefWidth(150);
    amount.setStyle(isTop
        ? "-fx-font-size: 14; -fx-text-fill: #1a73e8;"
          + " -fx-font-weight: bold;"
        : "-fx-font-size: 13; -fx-text-fill: #555;");

    String rankText = "#" + (rank + 1);
    String rankStyle = switch (rank) {
      case 0 ->
          "-fx-background-color: #ffd700;"
          + " -fx-text-fill: #7a5c00;";
      case 1 ->
          "-fx-background-color: #e0e0e0;"
          + " -fx-text-fill: #555;";
      case 2 ->
          "-fx-background-color: #ffe0cc;"
          + " -fx-text-fill: #b85c00;";
      default ->
          "-fx-background-color: #f5f5f5;"
          + " -fx-text-fill: #888;";
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
    Label empty = new Label(
        "Chưa có ai đặt giá trong phiên này");
    empty.setStyle(
        "-fx-font-size: 13; -fx-text-fill: #bbb;"
        + " -fx-padding: 16 0;");
    return empty;
  }

  private void startCountdown(AuctionSession session) {
    this.currentEndTime = session.getEndTime();
    startCountdown(session.getEndTime());
  }

  private void startCountdown(LocalDateTime endTime) {
    if (timeline != null) {
      timeline.stop();
    }

    timeline = new Timeline(
        new KeyFrame(Duration.seconds(1), e -> {
          LocalDateTime now = LocalDateTime.now();

          if (now.isBefore(endTime)) {
            setStatus("Đang đấu giá", "#34a853");
            lblTimeRemaining.setText(
                "Còn: " + formatDuration(now, endTime));
            setBiddingEnabled(true);
          } else {
            setStatus("Đã kết thúc", "#ea4335");
            lblTimeRemaining.setText(
                "Phiên đấu giá đã kết thúc");
            setBiddingEnabled(false);
            timeline.stop();
          }
        }));

    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.play();
  }

  private void setStatus(String text, String color) {
    lblStatus.setText(text);
    lblStatus.setStyle(
        "-fx-background-color: " + color
        + "; -fx-text-fill: white;"
        + " -fx-padding: 6 14;"
        + " -fx-background-radius: 20;"
        + "-fx-font-size: 12;"
        + " -fx-font-weight: bold;");
  }

  private void setBiddingEnabled(boolean enabled) {
    if (btnPlaceBid != null) {
      btnPlaceBid.setDisable(!enabled);
    }
    if (hboxQuickBids != null) {
      hboxQuickBids.setDisable(!enabled);
    }
    if (bidSpinner != null) {
      bidSpinner.setDisable(!enabled);
    }
  }

  private String formatDuration(
      LocalDateTime from, LocalDateTime to) {
    long days = ChronoUnit.DAYS.between(from, to);
    from = from.plusDays(days);
    long hours = ChronoUnit.HOURS.between(from, to);
    from = from.plusHours(hours);
    long minutes = ChronoUnit.MINUTES.between(from, to);
    from = from.plusMinutes(minutes);
    long seconds = ChronoUnit.SECONDS.between(from, to);
    return days > 0
        ? String.format(
            "%d ngày %02d:%02d:%02d",
            days, hours, minutes, seconds)
        : String.format(
            "%02d:%02d:%02d",
            hours, minutes, seconds);
  }

  private void updateBidSpinner(double currentPrice) {
    bidSpinner.setValueFactory(
        new SpinnerValueFactory
            .DoubleSpinnerValueFactory(
            currentPrice + stepValue,
            Double.MAX_VALUE,
            currentPrice + stepValue, stepValue));
  }

  @FXML
  void handleGoBack(ActionEvent event) {
    if (timeline != null) {
      timeline.stop();
    }
    AuctionWebSocketClient wsClient =
        MainApp.getWebSocketClient();
    if (wsClient != null) {
      wsClient.setOnMessageCallback(null);
    }
    NavigationManager.navigateTo(event, "/Home.fxml");
  }

  private void showAlert(
      String content, Alert.AlertType type) {
    AlertUtils.showAlert("Thông báo", content, type);
  }


  private void setupWebSocket() {
      AuctionWebSocketClient wsClient = MainApp.getWebSocketClient();
      if (wsClient == null) {
          logger.error("WebSocket client không khả dụng - không thể thiết lập callback");
          return;
      }

      wsClient.setOnMessageCallback(message -> {
          // BẮT BUỘC BỌC TRONG Platform.runLater ĐỂ CẬP NHẬT UI TRÊN JAVAFX THREAD
          Platform.runLater(() -> {
              try {
                  JsonObject json = JsonParser.parseString(message).getAsJsonObject();
                  if (!json.has("type")) return;

                  String type = json.get("type").getAsString();

                  switch (type) {
                      case "NEW_BID", "BID_UPDATE", "UPDATE" -> {
                          // Nếu đang xem phiên này thì mới cập nhật UI chi tiết
                          if (json.has("sessionId") && !json.get("sessionId").getAsString().equals(currentSessionId)) {
                              return;
                          }

                          // Cập nhật lại giá và bảng lịch sử
                          refreshCurrentPrice();
                          loadBidHistory();

                          // Cập nhật đếm ngược chống snipe (nếu có)
                          if (json.has("endTime")) {
                              LocalDateTime newEnd = LocalDateTime.parse(json.get("endTime").getAsString());
                              if (newEnd.isAfter(currentEndTime)) {
                                  currentEndTime = newEnd;
                                  DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                                  lblEndTime.setText("Kết thúc: " + newEnd.format(fmt));
                                  startCountdown(newEnd);
                              }
                          }
                      }

                      case "PLACE_BID_RESULT" -> {
                          String status = json.has("status") ? json.get("status").getAsString() : "";
                          String msg = json.has("message") ? json.get("message").getAsString() : "";

                          if ("SUCCESS".equals(status)) {
                              showAlert("Đặt giá thành công!", Alert.AlertType.INFORMATION);
                          } else {
                              showAlert(msg.isEmpty() ? "Đặt giá thất bại!" : msg, Alert.AlertType.ERROR);
                          }
                          btnPlaceBid.setDisable(false);
                          btnPlaceBid.setText("Đặt giá");
                      }

                      case "SESSION_SETTLED" -> {
                          setStatus("Đã kết thúc", "#ea4335");
                          setBiddingEnabled(false);
                          if (timeline != null) timeline.stop();
                      }

                      case "JOIN_FAILURE" -> {
                          String joinMsg = json.has("message") ? json.get("message").getAsString() : "Không thể tham gia phiên";
                          showAlert(joinMsg, Alert.AlertType.WARNING);
                          setBiddingEnabled(false);
                      }
                  }
              } catch (Exception e) {
                  logger.error("Lỗi khi xử lý tin nhắn WebSocket: {}", e.getMessage());
              }
          });
      });

      JsonObject joinData = new JsonObject();
      joinData.addProperty("sessionId", currentSessionId);
      wsClient.sendCommand("JOIN", joinData);
  }
}




