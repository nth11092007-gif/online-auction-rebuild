package com.auction.client.controller;

import com.auction.client.MainApp;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.auction.common.model.AuctionSession;
import com.auction.common.model.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** ItemCardController - controls the item card display in the auction listing view. */
public class ItemCardController {

  private static final Logger logger =
      LoggerFactory.getLogger(ItemCardController.class);
  private AuctionSession currentSession;

  @FXML
  private Label lblDescribe;
  @FXML
  private Label lblName;
  @FXML
  private ImageView imgItem;
  @FXML
  private Label lblCurrentPrice;
  @FXML
  private Label lblItemId;
  @FXML
  private Label lblOwner;
  @FXML
  private Label lblType;
  @FXML
  private Button btnJoin;

  /**
   * Populates the card labels and image from the given item.
   *
   * @param item the item whose data is displayed on the card
   */
  public void setItemData(Item item) {
    lblItemId.setText("ID: " + item.getItemId());
    lblCurrentPrice.setText(
        "Giá hiện tại: " + item.getStartingPrice()
        + " VND");
    lblType.setText(
        "Phân loại: " + item.getClass().getSimpleName());
    lblOwner.setText(
        "Người sở hữu: " + item.getOwnerName());
    lblDescribe.setText(
        "Mô tả: " + item.getDescription());
    if (item.getAvatar() != null) {
      Image image =
          SwingFXUtils.toFXImage(item.getAvatar(), null);
      if (image != null) {
        imgItem.setImage(image);
      } else {
        logger.error(
            "Không chuyển đổi được ảnh từ database.");
      }
    }
  }

  /**
   * Populates the card with data from an auction session, including
   * the item image and current price.
   *
   * @param session the auction session to display on the card
   */
  public void setAuctionData(AuctionSession session) {
    if (session == null) {
      logger.error(
          "setAuctionData nhận session null — bỏ qua.");
      return;
    }

    this.currentSession = session;
    logger.info("setAuctionData OK, sessionId = {}",
        session.getSessionId());

    try {
      Item item = session.getItem();
      if (item != null) {
        setItemData(item);
        lblCurrentPrice.setText(
            "Giá hiện tại: "
            + session.getCurrentPrice() + " VND");
      } else {
        lblItemId.setText(
            "Session: " + session.getSessionId());
        lblCurrentPrice.setText(
            "Giá hiện tại: "
            + session.getCurrentPrice() + " VND");
        lblType.setText("Loại: Đấu giá");
        lblOwner.setText("Người bán: "
            + (session.getSeller() != null
                ? session.getSeller().getUsername()
                : "Không xác định"));
        lblDescribe.setText("");
        logger.info("Cảnh báo: session {} không có item "
            + "— có thể Gson chưa map đúng field.",
            session.getSessionId());
        BufferedImage bufferedImage =
            session.getItem().getAvatar();
        Image fxImage =
            SwingFXUtils.toFXImage(bufferedImage, null);
        imgItem.setImage(fxImage);
      }
    } catch (Exception e) {
      logger.error(
          "Lỗi trong setAuctionData (session={}): {}",
          session.getSessionId(), e.getMessage(), e);
    }
  }

  @FXML
  private void handleJoinAuction() {
    if (currentSession != null) {
      logger.info("Joining auction: {}",
          currentSession.getSessionId());
      MainApp.showAuctionDetail(currentSession);
    } else {
      logger.error("currentSession is null");
    }
  }
}




