package Controller;

import dao.UserDAO;
import java.io.IOException;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ServiceFactory;
import utils.SessionManager;

/**
 * Controller for the user profile screen.
 *
 * <p>Handles deposit, withdraw, balance display, navigation,
 * and logout functionality.</p>
 */
public class ProfileController {

  private static final Logger logger =
      LoggerFactory.getLogger(ProfileController.class);
  private UserDAO userDao =
      ServiceFactory.getInstance().getUserDao();
  protected static User currentUser;

  @FXML
  private Label lblBalance;
  @FXML
  private Label lblFrozenBalance;
  @FXML
  private Label lblEmail;
  @FXML
  private Label lblPhoneNumber;
  @FXML
  private Label lblRealName;
  @FXML
  private TextField txtDepositAmount;
  @FXML
  private TextField txtWithdrawAmount;

  @FXML
  public void initialize() {
    loadUserData();
  }

  private void loadUserData() {
    SessionManager.refreshCurrentUserFromDb();
    currentUser = SessionManager.getCurrentUser();
    if (currentUser != null) {
      lblRealName.setText(currentUser.getRealName());
      lblEmail.setText(currentUser.getEmail());
      lblPhoneNumber.setText(currentUser.getPhoneNumber());
      updateBalanceUi();
    } else {
      showAlert(
          "Vui lòng đăng nhập lại!",
          Alert.AlertType.WARNING);
    }
  }

  private void updateBalanceUi() {
    if (currentUser != null) {
      lblBalance.setText(
          String.format("%,.2f VNĐ",
              currentUser.getBalance()));
      lblFrozenBalance.setText(
          String.format("%,.2f VNĐ",
              currentUser.getFrozenBalance()));
    }
  }

  @FXML
  void handleDeposit(ActionEvent event) {
    try {
      double amount =
          Double.parseDouble(txtDepositAmount.getText());
      if (amount <= 0) {
        showAlert("Số tiền nạp phải lớn hơn 0!",
            Alert.AlertType.ERROR);
        return;
      }
      double newBalance =
          currentUser.getBalance() + amount;
      if (userDao.updateBalance(
          currentUser.getId(), newBalance,
          currentUser.getFrozenBalance())) {
        currentUser.deposit(amount);
        updateBalanceUi();
        txtDepositAmount.clear();
        showAlert("Nạp tiền thành công!",
            Alert.AlertType.INFORMATION);
      }
    } catch (NumberFormatException e) {
      showAlert("Vui lòng nhập số tiền hợp lệ!",
          Alert.AlertType.WARNING);
    } catch (SQLException e) {
      showAlert("Lỗi cập nhật số dư!",
          Alert.AlertType.ERROR);
    }
  }

  @FXML
  void handleWithdraw(ActionEvent event) {
    try {
      double amount =
          Double.parseDouble(txtWithdrawAmount.getText());
      if (amount <= 0) {
        showAlert("Số tiền rút phải lớn hơn 0!",
            Alert.AlertType.ERROR);
        return;
      }
      if (amount > currentUser.getBalance()) {
        showAlert("Số dư khả dụng không đủ!",
            Alert.AlertType.ERROR);
        return;
      }
      double newBalance =
          currentUser.getBalance() - amount;
      if (userDao.updateBalance(
          currentUser.getId(), newBalance,
          currentUser.getFrozenBalance())) {
        currentUser.withdraw(amount);
        updateBalanceUi();
        txtWithdrawAmount.clear();
        showAlert("Rút tiền thành công!",
            Alert.AlertType.INFORMATION);
      }
    } catch (NumberFormatException e) {
      showAlert("Vui lòng nhập số tiền hợp lệ!",
          Alert.AlertType.WARNING);
    } catch (SQLException e) {
      showAlert("Lỗi cập nhật số dư!",
          Alert.AlertType.ERROR);
    }
  }

  @FXML
  void handleGoBack(ActionEvent event) {
    try {
      Parent root;
      root = FXMLLoader.load(
          getClass().getResource("/Home.fxml"));
      Stage stage =
          (Stage) ((Node) event.getSource())
              .getScene().getWindow();
      stage.setScene(new Scene(root));
      stage.show();
    } catch (IOException e) {
      logger.error("Lỗi: {}", e.getMessage(), e);
      showAlert("Không thể quay lại màn hình chính!",
          Alert.AlertType.ERROR);
    }
  }

  private void showAlert(
      String content, Alert.AlertType type) {
    Alert alert = new Alert(type);
    alert.setTitle("Thông báo");
    alert.setContentText(content);
    alert.showAndWait();
  }

  @FXML
  void handleLogout(ActionEvent event) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Xác nhận đăng xuất");
    alert.setHeaderText(null);
    alert.setContentText(
        "Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?");

    if (alert.showAndWait().orElse(ButtonType.CANCEL)
        == ButtonType.OK) {
      try {
        SessionManager.logout();

        if (MainApp.getWebSocketClient() != null) {
          MainApp.getWebSocketClient().close();
        }

        Parent root = FXMLLoader.load(
            getClass().getResource("/Login.fxml"));
        Stage stage = (Stage) ((Node) event.getSource())
            .getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();

      } catch (Exception e) {
        logger.error("Lỗi: {}", e.getMessage(), e);
        Alert errorAlert =
            new Alert(Alert.AlertType.ERROR);
        errorAlert.setContentText(
            "Đã xảy ra lỗi khi đăng xuất. "
            + "Vui lòng thử lại!");
        errorAlert.showAndWait();
      }
    }
  }
}
