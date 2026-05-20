package Controller;

import java.io.IOException;
import java.sql.SQLException;

import dao.UserDAO;
import dao.UserDAOImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import utils.SessionManager;

public class ProfileController {
    private UserDAO userDAO = new UserDAOImpl();
    protected static User currentUser; // không static, lấy từ SessionManager

    @FXML private Button HandleBack;
    @FXML private Button HandleWithdrawAmount;
    @FXML private Label lblBalance;
    @FXML private Label lblFrozenBalance;
    @FXML private Label lblEmail;
    @FXML private Label lblPhoneNumber;
    @FXML private Label lblRealName;
    @FXML private TextField txtDepositAmount;
    @FXML private TextField txtWithdrawAmount;

    @FXML
    public void initialize() {
        loadUserData();
    }

    private void loadUserData() {
        currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            lblRealName.setText(currentUser.getRealName());
            lblEmail.setText(currentUser.getEmail());
            lblPhoneNumber.setText(currentUser.getPhoneNumber());
            updateBalanceUI();
        } else {
            // Chưa đăng nhập, có thể chuyển về login
            showAlert("Vui lòng đăng nhập lại!", Alert.AlertType.WARNING);
        }
    }

    private void updateBalanceUI() {
        if (currentUser != null) {
            lblBalance.setText(String.format("%,.2f VNĐ", currentUser.getBalance()));
            lblFrozenBalance.setText(String.format("%,.2f VNĐ", currentUser.getFrozenBalance()));
        }
    }

    @FXML
    void HandleDeposit(ActionEvent event) {
        try {
            double amount = Double.parseDouble(txtDepositAmount.getText());
            if (amount <= 0) {
                showAlert("Số tiền nạp phải lớn hơn 0!", Alert.AlertType.ERROR);
                return;
            }
            double newBalance = currentUser.getBalance() + amount;
            if (userDAO.updateBalance(currentUser.getID(), newBalance, currentUser.getFrozenBalance())) {
                currentUser.deposit(amount);
                updateBalanceUI(); // Cập nhật UI
                txtDepositAmount.clear();
                showAlert("Nạp tiền thành công!", Alert.AlertType.INFORMATION);
            }
        } catch (NumberFormatException e) {
            showAlert("Vui lòng nhập số tiền hợp lệ!", Alert.AlertType.WARNING);
        } catch (SQLException e) {
            showAlert("Lỗi cập nhật số dư!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void HandleWithdraw(ActionEvent event) {
        try {
            double amount = Double.parseDouble(txtWithdrawAmount.getText());
            if (amount <= 0) {
                showAlert("Số tiền rút phải lớn hơn 0!", Alert.AlertType.ERROR);
                return;
            }
            if (amount > currentUser.getBalance()) {
                showAlert("Số dư khả dụng không đủ!", Alert.AlertType.ERROR);
                return;
            }
            double newBalance = currentUser.getBalance() - amount;
            if (userDAO.updateBalance(currentUser.getID(), newBalance, currentUser.getFrozenBalance())) {
                currentUser.withdraw(amount);
                updateBalanceUI();
                txtWithdrawAmount.clear();
                showAlert("Rút tiền thành công!", Alert.AlertType.INFORMATION);
            }
        } catch (NumberFormatException e) {
            showAlert("Vui lòng nhập số tiền hợp lệ!", Alert.AlertType.WARNING);
        } catch (SQLException e) {
            showAlert("Lỗi cập nhật số dư!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void handleGoBack(ActionEvent event) {
        // Quay về màn hình chính tùy theo role của user
        try {
            Parent root;
            root = FXMLLoader.load(getClass().getResource("/Home.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Không thể quay lại màn hình chính!", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Thông báo");
        alert.setContentText(content);
        alert.showAndWait();
    }
}