package Controller;

import java.io.IOException;
import java.util.List;

import dao.AuctionSessionDAO;
import dao.AuctionSessionDAOImpl;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import model.AuctionSession;

public class HomeBidderController {

    @FXML private FlowPane productContainer;
    private AuctionSessionDAO sessionDAO = new AuctionSessionDAOImpl();

    @FXML
    public void initialize() {
        System.out.println("HomeBidderController initialized");
        // Tải trực tiếp từ database
        List<AuctionSession> sessions = sessionDAO.getAllSessions();
        System.out.println("Số phiên từ DB: " + sessions.size());
        displaySessions(sessions);
    }

    private void displaySessions(List<AuctionSession> sessions) {
        if (productContainer == null) {
            System.err.println("productContainer is null");
            return;
        }
        productContainer.getChildren().clear();
        if (sessions == null || sessions.isEmpty()) {
            System.out.println("Không có phiên đấu giá nào.");
            return;
        }
        for (AuctionSession session : sessions) {
            if (session == null) continue;
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ItemCard.fxml"));
                VBox card = loader.load();
                ItemCardController controller = loader.getController();
                if (controller != null) {
                    controller.setAuctionData(session);
                    productContainer.getChildren().add(card);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Các phương thức điều hướng tạm thời
    @FXML public void GoToProfile() {}
    @FXML public void filterAll() {}
    @FXML public void filterElectronics() {}
    @FXML public void filterVehicles() {}
    @FXML public void filterArts() {}
}