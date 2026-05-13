package Controller;

import dao.AuctionSessionDAO;
import dao.AuctionSessionDAOImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.*;

import java.io.IOException;
import java.time.LocalDateTime;


public class CreateAutionController {
    AuctionSessionDAO auctionSessionDAO = new AuctionSessionDAOImpl();
    @FXML
    private Button btnClearForm;

    @FXML
    private Button btnCreateAution;

    @FXML
    private ComboBox<String> cbItemType;

    @FXML
    private TextArea txtDescription;

    @FXML
    private TextField txtIncrementStep;

    @FXML
    private TextField txtItemName;

    @FXML
    private TextField txtOpenDays;

    @FXML
    private TextField txtStartingPrice;

    @FXML
    void handleGoBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HomeSeller.fxml"));
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
    void HandleClearForm(ActionEvent event) {
        txtItemName.clear();
        txtStartingPrice.clear();
        txtIncrementStep.clear();
        txtOpenDays.clear();
        txtDescription.clear();
        cbItemType.getSelectionModel().clearSelection();
    }
    @FXML
    void HandleCreatAution(ActionEvent event) {
        try {
            String itemName = txtItemName.getText();
            double startPrice = Double.parseDouble(txtStartingPrice.getText());
            double stepPrice = Double.parseDouble(txtIncrementStep.getText());
            int openDays = Integer.parseInt(txtOpenDays.getText());
            String description = txtDescription.getText();
            if (itemName.isEmpty() || cbItemType == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng điền đầy đủ tên sản phẩm và loại hàng.");
                return;
            }
            int ItemId;
            Items initItem;
            switch (cbItemType.toString()) {
                case "Vehicles":
                    ItemId = 2;
                    initItem = new Vehicles(null, )
                case "Arts":
                    ItemId = 1;
                case "Electronics":
                    ItemId = 3;
            } ;


            // 3. Khởi tạo đối tượng AuctionSession
            AuctionSession auctionSession = new AuctionSession((Seller) ProfileController.currentUser, cbItemType, startPrice);
            AuctionSessionDAO newSession = new AuctionSessionDAOImpl();
            newSession.createSession()
            newSession.

                    // Tính toán thời gian bắt đầu và kết thúc
                    LocalDateTime startTime = LocalDateTime.now();
            LocalDateTime endTime = startTime.plusDays(openDays);

            newSession.setStartTime(startTime);
            newSession.setEndTime(endTime);
            newSession.setStatus(AuctionSession.Status.OPEN); // Mặc định là OPEN khi tạo mới

            // 4. Gọi DAO để lưu vào database
            boolean success = auctionSessionDAO.createSession(newSession, dummyItemId);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Phiên đấu giá đã được tạo thành công!");
                HandleClearForm(event); // Xoá form sau khi tạo
            } else {
                showAlert(Alert.AlertType.ERROR, "Thất bại", "Không thể tạo phiên đấu giá. Vui lòng kiểm tra lại.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi định dạng", "Giá tiền và số ngày phải là con số.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Đã xảy ra lỗi: " + e.getMessage());
        }
    }
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    public void initialize() {
        // Thêm các loại vật phẩm vào ComboBox
        cbItemType.getItems().addAll("Electronics", "Arts", "Vehicles");
    }

}
//thêm màn hình riêng cho arts,....