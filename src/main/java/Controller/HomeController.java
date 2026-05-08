package Controller;

import dao.ItemDAO;
import dao.ItemDAOImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Items;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class HomeController {
    private ItemDAO itemDAO = new ItemDAOImpl();
    @FXML
    private FlowPane productContainer;

    @FXML
    private TextField txtSearch;

    @FXML
    void filterAll(ActionEvent event) {
        try {
            loadProducts(itemDAO.getAllItems());
        } catch (SQLException e) {
            System.out.println("tht shit bro");
        }
    }

    @FXML
    void filterArts(ActionEvent event) {

    }

    @FXML
    void filterElectronics(ActionEvent event) {

    }

    @FXML
    void filterVehicles(ActionEvent event) {

    }

    @FXML
    void GoToCreateSession(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CreateSession.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void GoToProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CreateSession.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void loadProducts(List<Items> items) {
        productContainer.getChildren().clear(); // Xóa các thẻ cũ
        try {
            for (Items item : items) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ItemCard.fxml"));
                VBox card = loader.load();

                ItemCardController controller = loader.getController();
                controller.setItemData(item); // Cho dữ liệu vào thẻ

                productContainer.getChildren().add(card);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void initialize() {
        // Tải toàn bộ sản phẩm lên màn hình khi vừa mở ứng dụng
        try {
            loadProducts(itemDAO.getAllItems());
        } catch (SQLException e) {
            System.out.println("tht shit bro");
        }
    }
}
//thêm phương thức getAllItems