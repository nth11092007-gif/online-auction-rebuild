package Controller;

import dao.UserDAO;
import java.io.IOException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ServiceFactory;
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
    colId.setCellValueFactory(
        new PropertyValueFactory<>("id"));
    colUsername.setCellValueFactory(
        new PropertyValueFactory<>("username"));
    colRealName.setCellValueFactory(
        new PropertyValueFactory<>("realName"));
    colEmail.setCellValueFactory(
        new PropertyValueFactory<>("email"));
    colBalance.setCellValueFactory(
        new PropertyValueFactory<>("balance"));
    colRole.setCellValueFactory(
        new PropertyValueFactory<>("role"));

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
      protected void updateItem(
          Boolean banned, boolean empty) {
        super.updateItem(banned, empty);
        if (empty || banned == null) {
          setGraphic(null);
          return;
        }

        Label badge = new Label(
            banned ? "Đã ban" : "Hoạt động");
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
          User user =
              getTableView().getItems().get(getIndex());
          handleToggleBan(user);
        });
      }

      @Override
      protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || getTableRow() == null
            || getTableRow().getItem() == null) {
          setGraphic(null);
          return;
        }
        User user =
            getTableView().getItems().get(getIndex());
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
    lblTotalUsers.setText(
        allUsers.size() + " nguoi dung");
  }

  private void handleToggleBan(User user) {
    boolean willBan = !user.isBanned();

    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle(
        willBan ? "Xac nhan ban" : "Xac nhan go ban");
    confirm.setHeaderText(null);
    confirm.setContentText(willBan
        ? "Ban co chac muon BAN nguoi dung: "
            + user.getUsername() + "?"
        : "Ban co chac muon GO BAN nguoi dung: "
            + user.getUsername() + "?");

    if (confirm.showAndWait().orElse(ButtonType.CANCEL)
        != ButtonType.OK) {
      return;
    }

    boolean success =
        userDao.setBanned(user.getId(), willBan);
    if (success) {
      user.setBanned(willBan);
      userTable.refresh();
      showAlert(
          willBan ? "Da ban nguoi dung thanh cong!"
              : "Da go ban nguoi dung thanh cong!",
          Alert.AlertType.INFORMATION);
    } else {
      showAlert(
          "That bai! Vui long thu lai.",
          Alert.AlertType.ERROR);
    }
  }

  @FXML
  void handleLogout(ActionEvent event) {
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Xac nhan dang xuat");
    confirm.setHeaderText(null);
    confirm.setContentText("Ban co chac muon dang xuat?");

    if (confirm.showAndWait().orElse(ButtonType.CANCEL)
        == ButtonType.OK) {
      try {
        SessionManager.logout();
        Parent root = FXMLLoader.load(
            getClass().getResource("/Login.fxml"));
        Stage stage = (Stage) ((Node) event.getSource())
            .getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
      } catch (IOException e) {
        showAlert(
            "Loi chuyen man hinh!", Alert.AlertType.ERROR);
      }
    }
  }

  private void showAlert(
      String content, Alert.AlertType type) {
    Alert alert = new Alert(type);
    alert.setTitle("Thong bao");
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }
}
