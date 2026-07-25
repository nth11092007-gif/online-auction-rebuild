package Controller;

import dao.UserDAO;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ServiceFactory;
import utils.AlertUtils;
import utils.NavigationManager;
import utils.SessionManager;

/** HomeAdminController - admin dashboard for managing users and banning/unbanning accounts. */
public class HomeAdminController {

  private static final Logger logger =
      LoggerFactory.getLogger(HomeAdminController.class);

  @FXML
  private TableView<User> userTable;
  @FXML
  private TableColumn<User, Integer> colId;
  @FXML
  private TableColumn<User, String> colUsername;
  @FXML
  private TableColumn<User, String> colRealName;
  @FXML
  private TableColumn<User, String> colEmail;
  @FXML
  private TableColumn<User, Double> colBalance;
  @FXML
  private TableColumn<User, String> colRole;
  @FXML
  private TableColumn<User, Boolean> colStatus;
  @FXML
  private TableColumn<User, Void> colAction;
  @FXML
  private Label lblTotalUsers;

  private final UserDAO userDao =
      ServiceFactory.getInstance().getUserDao();
  private ObservableList<User> allUsers;

  /**
   * Initializes the admin dashboard by setting up table columns,
   * row styles, and loading the user list.
   */
  @FXML
  public void initialize() {
    setupColumns();
    setupRowStyle();
    loadUsers();
  }

  private void setupColumns() {
    colId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
    colRealName.setCellValueFactory(new PropertyValueFactory<>("realName"));
    colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
    colBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
    colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

    colBalance.setCellFactory(col -> new TableCell<>() {
      @Override
      protected void updateItem(Double value, boolean empty) {
        super.updateItem(value, empty);
        if (empty || value == null) {
          setText(null);
          return;
        }
        setText(String.format("%,.0f d", value));
      }
    });

    colStatus.setCellValueFactory(data ->
        new javafx.beans.property.SimpleBooleanProperty(
            data.getValue().isBanned()).asObject()
    );
    colStatus.setCellFactory(col -> new TableCell<>() {
      @Override
      protected void updateItem(Boolean banned, boolean empty) {
        super.updateItem(banned, empty);
        if (empty || banned == null) {
          setGraphic(null);
          return;
        }

        Label badge = new Label(banned ? "Đã khóa" : "Hoạt động");
        badge.setStyle(banned
            ? """
              -fx-background-color: #fff0f0;
              -fx-text-fill: #c0392b;
              -fx-font-size: 11;
              -fx-font-weight: bold;
              -fx-background-radius: 20;
              -fx-padding: 3 10;
              """
            : """
              -fx-background-color: #e8f5e9;
              -fx-text-fill: #0f9d58;
              -fx-font-size: 11;
              -fx-font-weight: bold;
              -fx-background-radius: 20;
              -fx-padding: 3 10;
              """);
        setAlignment(Pos.CENTER);
        setGraphic(badge);
        setText(null);
      }
    });

    colAction.setCellFactory(col -> new TableCell<>() {
      private final Button btnToggle = new Button();

      {
        btnToggle.setOnAction(e -> {
          User user = getTableView().getItems().get(getIndex());
          handleToggleBan(user);
        });
      }

      @Override
      protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || getTableRow() == null || getTableRow().getItem() == null) {
          setGraphic(null);
          return;
        }
        User user = getTableView().getItems().get(getIndex());
        if (user.isBanned()) {
          btnToggle.setText("Gỡ ban");
          btnToggle.setStyle("""
              -fx-background-color: #e8f5e9;
              -fx-text-fill: #0f9d58;
              -fx-font-size: 11;
              -fx-font-weight: bold;
              -fx-border-color: #c8e6c9;
              -fx-border-radius: 7;
              -fx-border-width: 1.5;
              -fx-background-radius: 7;
              -fx-padding: 4 12;
              -fx-cursor: hand;
              """);
        } else {
          btnToggle.setText("Ban");
          btnToggle.setStyle("""
              -fx-background-color: #fff0f0;
              -fx-text-fill: #c0392b;
              -fx-font-size: 11;
              -fx-font-weight: bold;
              -fx-border-color: #f5c6c6;
              -fx-border-radius: 7;
              -fx-border-width: 1.5;
              -fx-background-radius: 7;
              -fx-padding: 4 12;
              -fx-cursor: hand;
              """);
        }
        setAlignment(Pos.CENTER);
        setGraphic(btnToggle);
        setText(null);
      }
    });
  }

  private void setupRowStyle() {
    userTable.setRowFactory(tv -> new TableRow<>() {
      @Override
      protected void updateItem(User user, boolean empty) {
        super.updateItem(user, empty);
        if (empty || user == null) {
          setStyle("");
        } else if (user.isBanned()) {
          setStyle("-fx-background-color: #fff8f7;");
        } else if (getIndex() % 2 == 0) {
          setStyle("-fx-background-color: white;");
        } else {
          setStyle("-fx-background-color: #f8faff;");
        }
      }
    });
  }

  private void loadUsers() {
    List<User> users = userDao.getAllUsers();
    allUsers = FXCollections.observableArrayList(users);
    userTable.setItems(allUsers);
    lblTotalUsers.setText(allUsers.size() + " người dùng");
  }

  private void handleToggleBan(User user) {
    boolean willBan = !user.isBanned();

    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle(willBan ? "Xác nhận khóa" : "Xác nhận gỡ khóa");
    confirm.setHeaderText(null);
    confirm.setContentText(willBan
        ? "Bạn có chắc muốn KHÓA người dùng: " + user.getUsername() + "?"
        : "Bạn có chắc muốn GỠ KHÓA người dùng: " + user.getUsername() + "?");

    if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
      return;
    }

    boolean success = userDao.setBanned(user.getId(), willBan);
    if (success) {
      user.setBanned(willBan);
      userTable.refresh();
      AlertUtils.showInfo(willBan ? "Đã khóa người dùng thành công!" : "Đã gỡ khóa người dùng thành công!");
    } else {
      AlertUtils.showError("Thất bại! Vui lòng thử lại.");
    }
  }

  @FXML
  void handleLogout(ActionEvent event) {
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Xác nhận đăng xuất");
    confirm.setHeaderText(null);
    confirm.setContentText("Bạn có chắc muốn đăng xuất?");

    if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
      SessionManager.logout();
      NavigationManager.navigateTo(event, "/Login.fxml");
    }
  }
}
