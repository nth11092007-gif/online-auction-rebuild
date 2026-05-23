package Controller;

import java.io.IOException;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox; // Import service xử lý đấu giá
import javafx.stage.Stage;
import model.AuctionSession;
import service.AuctionService;

public class HomeController {

    // Đổi sang dùng AuctionService để lấy danh sách các PHIÊN đang diễn ra
    private final AuctionService auctionService = new AuctionService();

    @FXML private FlowPane productContainer;
    @FXML private TextField txtSearch;

    @FXML
    void filterAll(ActionEvent event) {
        // Load danh sách các phiên đấu giá
        loadSessions(auctionService.getAllSessions());
    }

    @FXML
    void filterArts(ActionEvent event) {
        // Logic filter sau này...
    }

    @FXML
    void filterElectronics(ActionEvent event) {
        // Logic filter sau này...
    }

    @FXML
    void filterVehicles(ActionEvent event) {
        // Logic filter sau này...
    }

    @FXML
    void GoToCreateSession(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/CreateAuction.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void GoToProfile(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Profile.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSessions(List<AuctionSession> sessions) {
        productContainer.getChildren().clear();
        try {
            for (AuctionSession session : sessions) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ItemCard.fxml"));
                VBox card = loader.load();

                ItemCardController controller = loader.getController();
                controller.setItemData(session.getItem());

                //Khi click vào card, ta truyền nguyên Phiên Đấu Giá sang màn Detail
                card.setOnMouseClicked(event -> {
                    goToAuctionDetail(event, session);
                });

                productContainer.getChildren().add(card);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        loadSessions(auctionService.getAllSessions());
    }

    private void goToAuctionDetail(MouseEvent event, AuctionSession session) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AuctionDetail.fxml"));
            Parent root = loader.load();

            AuctionDetailController detailController = loader.getController();
            // Đưa toàn bộ thông tin Session vào Detail Controller
            detailController.setAuctionData(session);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Không thể load file AuctionDetail.fxml");
        }
    }
}