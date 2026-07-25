package Controller;

import dao.AuctionSessionDAO;
import dao.ItemDAO;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
import model.Item;
import model.User;
import model.Vehicles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ServiceFactory;
import utils.AlertUtils;
import utils.NavigationManager;
import utils.SessionManager;

/** CreateAuctionController - handles creation of new auction sessions with item details. */
public class CreateAuctionController {
  private static final Logger logger =
      LoggerFactory.getLogger(CreateAuctionController.class);
  private static final String ITEM_TYPE_VEHICLES = "Vehicles";
  private static final String ITEM_TYPE_ARTS = "Arts";
  private static final String ITEM_TYPE_ELECTRONICS = "Electronics";

  private final AuctionSessionDAO auctionSessionDao =
      ServiceFactory.getInstance().getSessionDao();
  private final ItemDAO itemDao =
      ServiceFactory.getInstance().getItemDao();
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
    NavigationManager.navigateTo(event, "/Home.fxml");
  }

  @FXML
  void handleClearForm(ActionEvent event) {
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
  void handleCreateAuction(ActionEvent event) {
    try {
      double[] parsed = validateInput();
      if (parsed == null) {
        return;
      }
      double startPrice = parsed[0];
      double stepPrice = parsed[1];
      int openDays = (int) parsed[2];

      String ownerName = getOwnerName();
      if (ownerName == null) {
        return;
      }

      String itemName = txtItemName.getText().trim();
      String itemType = cbItemType.getValue();
      String description = txtDescription.getText();

      Item initItem = createItemByType(
          itemType, itemName, ownerName,
          startPrice, description);
      if (initItem == null) {
        return;
      }

      boolean success = saveAuctionSession(
          initItem, startPrice, stepPrice, openDays);
      if (success) {
        AlertUtils.showInfo("Thành công", "Phiên đấu giá đã được tạo thành công!");
        handleClearForm(event);
      } else {
        AlertUtils.showError("Thất bại", "Không thể tạo phiên đấu giá. Vui lòng kiểm tra lại.");
      }

    } catch (NumberFormatException e) {
      AlertUtils.showError("Lỗi định dạng", "Giá tiền, bước giá và số ngày phải là con số hợp lệ.");
    } catch (Exception e) {
      logger.error("Lỗi khi tạo phiên đấu giá: {}", e.getMessage(), e);
      AlertUtils.showError("Lỗi hệ thống", "Đã xảy ra lỗi: " + e.getMessage());
    }
  }

  private double[] validateInput() {
    String itemName = txtItemName.getText();
    String itemType = cbItemType.getValue();

    if (itemName == null || itemName.isEmpty()
        || itemType == null
        || txtStartingPrice.getText().isEmpty()
        || txtOpenDays.getText().isEmpty()
        || txtIncrementStep.getText().isEmpty()) {
      AlertUtils.showError("Lỗi", "Vui lòng điền đầy đủ thông tin bắt buộc.");
      return null;
    }

    double startPrice = Double.parseDouble(txtStartingPrice.getText());
    double stepPrice = Double.parseDouble(txtIncrementStep.getText());
    int openDays = Integer.parseInt(txtOpenDays.getText());
    return new double[]{startPrice, stepPrice, openDays};
  }

  private String getOwnerName() {
    User currentUser = SessionManager.getCurrentUser();
    if (currentUser != null) {
      return currentUser.getUsername();
    }
    AlertUtils.showWarning("Cảnh báo", "Vui lòng đăng nhập trước khi tạo phiên!");
    return null;
  }

  private Item createItemByType(
      String itemType, String itemName, String ownerName,
      double startPrice, String description) {
    return switch (itemType) {
      case ITEM_TYPE_VEHICLES -> {
        String brand = lookupText("#txtBrand");
        int mileage = lookupInt("#txtMileage");
        String vehicleId = lookupText("#txtVehicleID");
        yield new Vehicles(0, itemName, ownerName,
            startPrice, description, brand, mileage, vehicleId);
      }
      case ITEM_TYPE_ARTS -> {
        String artistName = lookupText("#txtArtistName");
        LocalDate releaseDate = lookupDate("#txtReleaseDate");
        if (releaseDate == null) {
          yield null;
        }
        yield new Arts(0, itemName, ownerName,
            startPrice, description, artistName, releaseDate);
      }
      case ITEM_TYPE_ELECTRONICS -> {
        String brand = lookupText("#txtBrand");
        int warranty = lookupInt("#txtWarranty");
        yield new Electronics(0, itemName, ownerName,
            startPrice, description, warranty, brand);
      }
      default -> null;
    };
  }

  private boolean saveAuctionSession(
      Item initItem, double startPrice,
      double stepPrice, int openDays) {
    itemDao.addItem(initItem);
    int generatedItemId = initItem.getItemId();

    if (generatedItemId <= 0) {
      AlertUtils.showError("Lỗi", "Không thể lưu sản phẩm vào cơ sở dữ liệu!");
      return false;
    }

    if (selectedImageFile != null) {
      itemDao.setAvatar(generatedItemId, selectedImageFile);
    }

    LocalDateTime startTime = LocalDateTime.now();
    LocalDateTime endTime = startTime.plusDays(openDays);

    AuctionSession auctionSession = new AuctionSession(
        SessionManager.getCurrentUser(), initItem,
        startPrice, stepPrice, startTime);
    auctionSession.setEndTime(endTime);
    auctionSession.setStatus(AuctionSession.Status.OPEN);

    return auctionSessionDao.createSession(
        auctionSession, generatedItemId);
  }

  private String lookupText(String fxId) {
    TextField field = (TextField) Container.lookup(fxId);
    return (field != null) ? field.getText() : "";
  }

  private int lookupInt(String fxId) {
    TextField field = (TextField) Container.lookup(fxId);
    if (field == null || field.getText().isEmpty()) {
      return 0;
    }
    return Integer.parseInt(field.getText());
  }

  private LocalDate lookupDate(String fxId) {
    TextField field = (TextField) Container.lookup(fxId);
    if (field == null || field.getText().isEmpty()) {
      return LocalDate.now();
    }
    try {
      return LocalDate.parse(field.getText());
    } catch (Exception e) {
      AlertUtils.showError("Lỗi định dạng ngày", "Ngày phát hành phải có định dạng YYYY-MM-DD");
      return null;
    }
  }

  private void loadDynamicAttributes(String itemType) {
    Container.getChildren().clear();

    if (itemType == null) {
      return;
    }

    try {
      Node nodeToAdd = switch (itemType) {
        case ITEM_TYPE_ARTS -> FXMLLoader.load(getClass().getResource("/ArtDetail.fxml"));
        case ITEM_TYPE_VEHICLES -> FXMLLoader.load(getClass().getResource("/VehicleDetail.fxml"));
        case ITEM_TYPE_ELECTRONICS -> FXMLLoader.load(getClass().getResource("/ElectronicDetail.fxml"));
        default -> null;
      };
      if (nodeToAdd != null) {
        Container.getChildren().add(nodeToAdd);
      }
    } catch (IOException e) {
      logger.error("Lỗi tải thuộc tính động: {}", e.getMessage(), e);
    }
  }

  /**
   * Initializes the controller by populating the item-type combo box
   * and registering a listener to load dynamic attributes on selection.
   */
  @FXML
  public void initialize() {
    cbItemType.getItems().addAll("Electronics", "Arts", "Vehicles");
    cbItemType.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> loadDynamicAttributes(newValue));
  }

  @FXML
  void handleSelectImage(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Chọn ảnh sản phẩm");

    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter(
            "Tệp hình ảnh", "*.png", "*.jpg", "*.jpeg")
    );

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
