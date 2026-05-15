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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import Exception.*;
import java.io.IOException;

public class RegisterController {
    UserDAO register = new UserDAOImpl();
    @FXML
    private Button btnRegister;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private TextField txtPhoneNumber;

    @FXML
    private TextField txtRealName;

    @FXML
    private TextField txtUser;

    public static String checkPasswordStrength(String password) {
        if (password == null || password.trim().isEmpty()) {
            return "Trống";
        }
        int score = 0;
        // 1. Kiểm tra độ dài (ít nhất 8 ký tự)
        if (password.length() >= 8) {
            score++;
        }
        // 2. Kiểm tra có chứa ít nhất một chữ cái viết hoa (A-Z)
        if (password.matches(".*[A-Z].*")) {
            score++;
        }
        // 3. Kiểm tra có chứa ít nhất một chữ cái viết thường (a-z)
        if (password.matches(".*[a-z].*")) {
            score++;
        }
        // 4. Kiểm tra có chứa ít nhất một chữ số (0-9)
        if (password.matches(".*\\d.*")) {
            score++;
        }
        // 5. Kiểm tra có chứa ít nhất một ký tự đặc biệt
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            score++;
        }
        // Đánh giá độ khó dựa trên tổng điểm (tối đa 5 điểm)
        switch (score) {
            case 0:
            case 1:
            case 2:
                return "Yếu"; // Chỉ đạt 1-2 tiêu chí (thường là quá ngắn hoặc chỉ có chữ/số)
            case 3:
                return "Trung bình"; // Đạt 3 tiêu chí
            case 4:
                return "Mạnh"; // Đạt 4 tiêu chí
            case 5:
                return "Rất mạnh"; // Đạt đủ 5 tiêu chí
            default:
                return "Không xác định";
        }
    }

    private void showAlert(String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Thông báo");
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void switchToHome(ActionEvent event) {
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
    void onhandleRegister(ActionEvent event) {
        try {
            String username = txtUser.getText().trim();
            String password = txtPassword.getText().trim();
            String realname = txtRealName.getText().trim();
            String email = txtEmail.getText().trim();
            String phone = txtPhoneNumber.getText().trim();

            //Ktra ko dc bỏ trống
            if (username.isEmpty() || password.isEmpty() || realname.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                showAlert("vui lòng điền đầy đủ thông tin", Alert.AlertType.ERROR);
                return;
            }

            User newUser = new User(realname, username, email, password, phone);
            boolean succes = register.register(newUser);

            //Thông báo thành công
            if (succes) {
                showAlert("Đăng ký thành công", Alert.AlertType.INFORMATION);
                switchToHome(event);
            } else {
                showAlert("Tài khoản đã tồn tại", Alert.AlertType.ERROR);
            }
        } catch (PasswordStrengthCheck p) {
            p.getMessage();
        }
    }
}
