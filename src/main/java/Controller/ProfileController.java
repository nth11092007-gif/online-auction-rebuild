package Controller;

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

import java.io.IOException;
import java.sql.SQLException;

public class ProfileController {
    private int currentUserID;
    private UserDAO userDAO = new UserDAOImpl();
    private User currentUser;
    @FXML
    private Button HandleBack;

    @FXML
    private Button HandleWithdrawAmount;

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

    // Hàm này phải được gọi sau khi load FXML để truyền ID người dùng
    public void setUserId(int id) {
        this.currentUserID = id;
        loadUserData(); // Tải dữ liệu khi có ID
    }

    @FXML
    void handleGoBack(ActionEvent event) {
        try {
            //Parent root = FXMLLoader.load(getClass().getResource("Home.fxml"));
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
    private void loadUserData() {
        this.currentUser = userDAO.getUserById(currentUserID);
        if (currentUser != null) {
            lblRealName.setText(currentUser.getRealName());
            lblEmail.setText(currentUser.getEmail());
            lblPhoneNumber.setText(currentUser.getPhoneNumber());
            updateBalanceUI();
        }
    }
    private void updateBalanceUI() {
        lblBalance.setText(String.format("%,.2f VNĐ", currentUser.getBalance()));
        lblFrozenBalance.setText(String.format("%,.2f VNĐ", currentUser.getFrozenBalance()));
    }
    private void showAlert(String content, Alert.AlertType type){
        Alert alert = new Alert(type);
        alert.setTitle("Thông báo");
        alert.setContentText(content);
        alert.showAndWait();
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
            if (userDAO.updateBalance(currentUserID, newBalance, currentUser.getFrozenBalance())) {
                currentUser.deposit(amount); // Cập nhật đối tượng Java
                updateBalanceUI(); // Cập nhật UI
                txtDepositAmount.clear();
                showAlert("Nạp tiền thành công!", Alert.AlertType.INFORMATION);
            }
        } catch (NumberFormatException e) {
            showAlert("Vui lòng nhập số tiền hợp lệ!", Alert.AlertType.WARNING);
        } catch (SQLException e) {
            showAlert("lỗi lấy thông tin số dư", Alert.AlertType.ERROR);
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
                showAlert("Số dư khả dụng không đủ để thực hiện giao dịch!", Alert.AlertType.ERROR);
                return;
            }
            double newBalance = currentUser.getBalance() - amount;
            if (userDAO.updateBalance(currentUserID, newBalance, currentUser.getFrozenBalance())) {
                currentUser.withdraw(amount);
                updateBalanceUI();
                txtWithdrawAmount.clear();

                showAlert("Rút tiền thành công!", Alert.AlertType.INFORMATION);
            }
        } catch (NumberFormatException e) {
            showAlert("Vui lòng nhập số tiền hợp lệ!", Alert.AlertType.WARNING);
        } catch (SQLException e) {
            showAlert("lỗi lấy thông tin số dư", Alert.AlertType.ERROR);
        }
    }
}
