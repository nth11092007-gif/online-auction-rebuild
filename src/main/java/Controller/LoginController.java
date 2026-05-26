package Controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dao.UserDAOImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import utils.SessionManager;

public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML
    private TextField txtUser;
    @FXML
    private PasswordField txtPassword;

    private final UserDAOImpl userDAO = new UserDAOImpl();

    @FXML
    void onhandleIn(ActionEvent event) {
        String username = txtUser.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập tên đăng nhập và mật khẩu.");
            return;
        }

        try {
            // Gọi phương thức login từ DAO (trả về User nếu đúng, null nếu sai)
            User user = userDAO.login(username, password);
            if (user == null) {
                showAlert("Đăng nhập thất bại", "Sai tên đăng nhập hoặc mật khẩu.");
                return;
            }
            if (user.isBanned()) {
                showAlert("Đăng nhập thất bại", "Tk của bạn đã bị khoá!");
                logger.warn("Tai khoan bi ban co gang dang nhap: {}", username);
                return;
            }

            // Lưu user vào SessionManager (quan trọng để các controller khác dùng)
            SessionManager.setCurrentUser(user);
            logger.info("Đăng nhập thành công: {}", user.getUsername());
            
            // Điều hướng theo role
            if (user.getRole() == User.Role.USER) {
                navigateTo(event, "/Home.fxml");
            } else if (user.getRole() == User.Role.ADMIN) {
                navigateTo(event, "/HomeAdmin.fxml");
            } else {
                showAlert("Lỗi", "Vai trò người dùng không xác định.");
            }

        } catch (Exception e) {
            logger.error("Lỗi đăng nhập", e);
            showAlert("Lỗi hệ thống", "Không thể đăng nhập: " + e.getMessage());
        }
    }

    @FXML
    void onhandleUp(ActionEvent event) {
        navigateTo(event, "/Register.fxml");
    }

    private void navigateTo(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            logger.error("Không thể chuyển đến {}", fxmlPath, e);
            showAlert("Lỗi", "Không thể mở màn hình.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}