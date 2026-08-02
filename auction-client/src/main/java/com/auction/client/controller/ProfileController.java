package com.auction.client.controller;

import com.auction.client.MainApp;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import com.auction.common.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.auction.client.utils.AlertUtils;
import com.auction.client.utils.NavigationManager;
import com.auction.client.utils.SessionManager;

/**
 * Controller for the user profile screen.
 *
 * <p>Handles deposit, withdraw, balance display, navigation,
 * and logout functionality.</p>
 */
public class ProfileController {
  private final com.auction.server.dao.UserDAO userDao = com.auction.server.service.ServiceFactory.getInstance().getUserDao();

  private static final Logger logger =
      LoggerFactory.getLogger(ProfileController.class);
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
      AlertUtils.showWarning("Vui lòng đăng nhập lại!");
    }
  }

  private void updateBalanceUi() {
    if (currentUser != null) {
      lblBalance.setText(
          String.format("%,.2f VNĐ", currentUser.getBalance()));
      lblFrozenBalance.setText(
          String.format("%,.2f VNĐ", currentUser.getFrozenBalance()));
    }
  }

  @FXML
  void handleDeposit(ActionEvent event) {
    try {
      double amount = Double.parseDouble(txtDepositAmount.getText());
      if (amount <= 0) {
        AlertUtils.showError("Số tiền nạp phải lớn hơn 0!");
        return;
      }
      double newBalance = currentUser.getBalance() + amount;
      if (userDao.updateBalance(
          currentUser.getId(), newBalance, currentUser.getFrozenBalance())) {
        currentUser.deposit(amount);
        updateBalanceUi();
        txtDepositAmount.clear();
        AlertUtils.showInfo("Nạp tiền thành công!");
      }
    } catch (NumberFormatException e) {
      AlertUtils.showWarning("Vui lòng nhập số tiền hợp lệ!");
    } catch (SQLException e) {
      AlertUtils.showError("Lỗi cập nhật số dư!");
    }
  }

  @FXML
  void handleWithdraw(ActionEvent event) {
    try {
      double amount = Double.parseDouble(txtWithdrawAmount.getText());
      if (amount <= 0) {
        AlertUtils.showError("Số tiền rút phải lớn hơn 0!");
        return;
      }
      if (amount > currentUser.getBalance()) {
        AlertUtils.showError("Số dư khả dụng không đủ!");
        return;
      }
      double newBalance = currentUser.getBalance() - amount;
      if (userDao.updateBalance(
          currentUser.getId(), newBalance, currentUser.getFrozenBalance())) {
        currentUser.withdraw(amount);
        updateBalanceUi();
        txtWithdrawAmount.clear();
        AlertUtils.showInfo("Rút tiền thành công!");
      }
    } catch (NumberFormatException e) {
      AlertUtils.showWarning("Vui lòng nhập số tiền hợp lệ!");
    } catch (SQLException e) {
      AlertUtils.showError("Lỗi cập nhật số dư!");
    }
  }

  @FXML
  void handleGoBack(ActionEvent event) {
    NavigationManager.navigateTo(event, "/Home.fxml");
  }

  @FXML
  void handleLogout(ActionEvent event) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Xác nhận đăng xuất");
    alert.setHeaderText(null);
    alert.setContentText("Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?");

    if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
      try {
        SessionManager.logout();

        if (MainApp.getWebSocketClient() != null) {
          MainApp.getWebSocketClient().close();
        }

        NavigationManager.navigateTo(event, "/Login.fxml");

      } catch (Exception e) {
        logger.error("Lỗi đăng xuất: {}", e.getMessage(), e);
        AlertUtils.showError("Đã xảy ra lỗi khi đăng xuất. Vui lòng thử lại!");
      }
    }
  }
}







