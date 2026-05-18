package Controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import dao.AuctionSessionDAO;
import dao.AuctionSessionDAOImpl;
import dao.ItemDAO;
import dao.ItemDAOImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Arts;
import model.AuctionSession;
import model.Electronics;
import model.Items;
import model.Seller;
import model.Vehicles;
import server.AuctionWebSocketClient;
import utils.SessionManager;


public class CreateAutionController {
    AuctionSessionDAO auctionSessionDAO = new AuctionSessionDAOImpl();
    private File selectedImageFile;
    @FXML
    private VBox Container;
    @FXML
    private Button btnClearForm;

    @FXML
    private Button btnCreateAution;

    @FXML
    private ComboBox<String> cbItemType;

    @FXML
    private ImageView imgPreview;

    @FXML
    private Label lblImagePath;

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
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
        selectedImageFile = null;
        lblImagePath.setText("Chưa có ảnh nào được chọn");
        imgPreview.setImage(null);
    }
    @FXML
void HandleCreatAution(ActionEvent event) {
    try {
        // Lấy seller an toàn từ SessionManager
        Seller seller = (Seller) SessionManager.getCurrentUser();

        String itemName = txtItemName.getText();
        String itemType = cbItemType.getValue();

        if (itemName.isEmpty() || itemType == null || txtStartingPrice.getText().isEmpty() || txtOpenDays.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng điền đầy đủ thông tin bắt buộc.");
            return;
        }

        double startPrice = Double.parseDouble(txtStartingPrice.getText());
        double stepPrice = Double.parseDouble(txtIncrementStep.getText());
        int openDays = Integer.parseInt(txtOpenDays.getText());
        String description = txtDescription.getText();

        // Lấy tên chủ sở hữu từ seller (giả sử Seller có phương thức getName())
        String ownerName = seller.getUsername(); // nếu không có, dùng seller.getUsername()
        int itemId = 0;
        Items initItem = null;

        switch (itemType) {
            case "Vehicles":
                itemId = 2;
                TextField txtVehicleBrand = (TextField) Container.lookup("txtBrand");
                TextField txtMileage = (TextField) Container.lookup("txtMileage");
                TextField txtVehicleID = (TextField) Container.lookup("txtVehicleID");

                String vBrand = (txtVehicleBrand != null) ? txtVehicleBrand.getText() : "";
                int vMileage = (txtMileage != null && !txtMileage.getText().isEmpty()) ? Integer.parseInt(txtMileage.getText()) : 0;
                String vIdStr = (txtVehicleID != null) ? txtVehicleID.getText() : "";

                initItem = new Vehicles(itemId, ownerName, startPrice, description, vBrand, vMileage, vIdStr);
                break;

            case "Arts":
                itemId = 1;
                TextField txtArtistName = (TextField) Container.lookup("txtArtistName");
                DatePicker dpReleaseDate = (DatePicker) Container.lookup("txtReleaseDate");

                String aArtistName = (txtArtistName != null) ? txtArtistName.getText() : "";
                LocalDate aReleaseDate = (dpReleaseDate != null && dpReleaseDate.getValue() != null) ? dpReleaseDate.getValue() : LocalDate.now();

                initItem = new Arts(itemId, ownerName, startPrice, description, aArtistName, aReleaseDate);
                break;

            case "Electronics":
                itemId = 3;
                TextField txtElectronicBrand = (TextField) Container.lookup("txtBrand");
                TextField txtWarranty = (TextField) Container.lookup("txtWarranty");

                String eBrand = (txtElectronicBrand != null) ? txtElectronicBrand.getText() : "";
                int eWarranty = (txtWarranty != null && !txtWarranty.getText().isEmpty()) ? Integer.parseInt(txtWarranty.getText()) : 0;

                initItem = new Electronics(itemId, ownerName, startPrice, description, eWarranty, eBrand);
                break;
        }
        ItemDAO itemDAO = new ItemDAOImpl();
        itemDAO.addItem(initItem); // insert vào DB, trả về ID mới tạo (nếu cần)
        // Tạo AuctionSession với seller từ SessionManager (không dùng ProfileController.currentUser)
        AuctionSession auctionSession = new AuctionSession(seller, initItem, startPrice);

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusDays(openDays);

        auctionSession.setStartTime(startTime);
        auctionSession.setEndTime(endTime);
        auctionSession.setStatus(AuctionSession.Status.OPEN);

        boolean success = auctionSessionDAO.createSession(auctionSession, initItem.getItemID());
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Phiên đấu giá đã được tạo thành công!");
            HandleClearForm(event);
            try {
            Parent root;
            root = FXMLLoader.load(getClass().getResource("/Home.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            AuctionWebSocketClient.getInstance().send("{\"action\":\"GET_SESSIONS\"}");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // showAlert("Không thể quay lại màn hình chính!", Alert.AlertType.ERROR);
        }
        } else {
            showAlert(Alert.AlertType.ERROR, "Thất bại", "Không thể tạo phiên đấu giá. Vui lòng kiểm tra lại.");
        }

    } catch (NumberFormatException e) {
        showAlert(Alert.AlertType.ERROR, "Lỗi định dạng", "Giá tiền và số ngày phải là con số.");
    } catch (IllegalStateException e) {
        showAlert(Alert.AlertType.ERROR, "Lỗi đăng nhập", "Bạn chưa đăng nhập hoặc không phải người bán.");
    } catch (Exception e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Đã xảy ra lỗi: " + e.getMessage());
    }
}
    private void loadDynamicAttributes(String itemType) {
        // Xóa thuộc tính của sản phẩm trước đó
        Container.getChildren().clear();

        if (itemType == null) return;

        try {
            Node nodeToAdd = null;
            switch (itemType) {
                case "Arts":
                    nodeToAdd = FXMLLoader.load(getClass().getResource("/ArtDetail.fxml"));
                    break;
                case "Vehicles":
                    nodeToAdd = FXMLLoader.load(getClass().getResource("/VehicleDetail.fxml"));
                    break;
                case "Electronics":
                    nodeToAdd = FXMLLoader.load(getClass().getResource("/ElectronicDetail.fxml"));
                    break;
            }
            //thêm thuouộc tính
            if (nodeToAdd != null) {
                Container.getChildren().add(nodeToAdd);
            }

        } catch (IOException e) {
            e.printStackTrace();
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
        // Lắng nghe sự thay đổi của ComboBox
        cbItemType.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            loadDynamicAttributes(newValue);
        });
    }
    @FXML
    void handleSelectImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh sản phẩm");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        // Lấy Stage hiện tại
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            selectedImageFile = file;
            lblImagePath.setText(file.getName());

            Image image = new Image(file.toURI().toString());
            imgPreview.setImage(image);
        }
    }
}
