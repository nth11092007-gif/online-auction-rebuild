package com.auction.client.controller;

import java.io.IOException;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import com.auction.common.model.AuctionSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.auction.client.utils.NavigationManager;

/** HomeController - displays and filters auction sessions on the bidder home screen. */
public class HomeController {

  private static final Logger logger =
      LoggerFactory.getLogger(HomeController.class);

  
  @FXML
  private FlowPane productContainer;

  @FXML
  void filterAll(ActionEvent event) {
    loadSessions(new java.util.ArrayList<>());
  }

  @FXML
  void filterArts(ActionEvent event) {
    List<AuctionSession> allSessions = new java.util.ArrayList<>();
    List<AuctionSession> filteredSessions = allSessions.stream()
        .filter(session -> session.getItem() instanceof com.auction.common.model.Arts)
        .toList();
    loadSessions(filteredSessions);
  }

  @FXML
  void filterElectronics(ActionEvent event) {
    List<AuctionSession> allSessions = new java.util.ArrayList<>();
    List<AuctionSession> filteredSessions = allSessions.stream()
        .filter(session -> session.getItem() instanceof com.auction.common.model.Electronics)
        .toList();
    loadSessions(filteredSessions);
  }

  @FXML
  void filterVehicles(ActionEvent event) {
    List<AuctionSession> allSessions = new java.util.ArrayList<>();
    List<AuctionSession> filteredSessions = allSessions.stream()
        .filter(session -> session.getItem() instanceof com.auction.common.model.Vehicles)
        .toList();
    loadSessions(filteredSessions);
  }

  @FXML
  void goToCreateSession(ActionEvent event) {
    NavigationManager.navigateTo(event, "/CreateAuction.fxml");
  }

  @FXML
  void goToProfile(ActionEvent event) {
    NavigationManager.navigateTo(event, "/Profile.fxml");
  }

  private void loadSessions(List<AuctionSession> sessions) {
    productContainer.getChildren().clear();
    try {
      for (AuctionSession session : sessions) {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/ItemCard.fxml"));
        VBox card = loader.load();

        ItemCardController controller = loader.getController();
        controller.setAuctionData(session);

        card.setOnMouseClicked(event -> goToAuctionDetail(event, session));

        productContainer.getChildren().add(card);
      }
    } catch (IOException e) {
      logger.error("Lỗi khi tải thẻ phiên đấu giá: {}", e.getMessage(), e);
    }
  }

  @FXML
  public void initialize() {
    loadSessions(new java.util.ArrayList<>());
  }

  private void goToAuctionDetail(MouseEvent event, AuctionSession session) {
    NavigationManager.<AuctionDetailController>navigateTo(
        event,
        "/AuctionDetail.fxml",
        controller -> controller.setAuctionData(session));
  }
}



