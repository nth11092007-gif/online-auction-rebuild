package Controller;

import Exception.PasswordStrengthCheck;
import dao.UserDAO;
import java.io.IOException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ServiceFactory;
import utils.SessionManager;

/** RegisterController - handles user registration with password strength validation. */
public class RegisterController {

  private static final Logger logger =
      LoggerFactory.getLogger(RegisterController.class);
  UserDAO register =
      ServiceFactory.getInstance().getUserDao();

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

  /**
   * Evaluates the strength of a password based on length,
   * uppercase, lowercase, digits, and special characters.
   *
   * @param password the password string to evaluate
   * @return a strength label in Vietnamese
   */
  public static String checkPasswordStrength(
      String password) {
    if (password == null
        || password.trim().isEmpty()) {
      return "Trống";
    }
    int score = 0;
    if (password.length() >= 8) {
      score++;
    }
    if (password.matches(".*[A-Z].*")) {
      score++;
    }
    if (password.matches(".*[a-z].*")) {
      score++;
    }
    if (password.matches(".*\\d.*")) {
      score++;
    }
    if (password.matches(
        ".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
      score++;
    }
    switch (score) {
      case 0:
      case 1:
      case 2:
        return "Yếu";
      case 3:
        return "Trung bình";
      case 4:
        return "Mạnh";
      case 5:
        return "Rất mạnh";
      default:
        return "Không xác định";
    }
  }

  private void showAlert(
      String content, Alert.AlertType type) {
    Alert alert = new Alert(type);
    alert.setTitle("Thông báo");
    alert.setContentText(content);
    alert.showAndWait();
  }

  private void showAlert(
      String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private void switchToHome(ActionEvent event) {
    try {
      FXMLLoader loader = new FXMLLoader(
          getClass().getResource("/Home.fxml"));
      Parent root = loader.load();
      Stage stage =
          (Stage) ((Node) event.getSource())
              .getScene().getWindow();
      Scene scene = new Scene(root);
      stage.setScene(scene);
      stage.show();
    } catch (IOException e) {
      logger.error("Lỗi: {}", e.getMessage(), e);
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

      if (username.isEmpty() || password.isEmpty()
          || realname.isEmpty() || email.isEmpty()
          || phone.isEmpty()) {
        showAlert("Vui lòng điền đầy đủ thông tin",
            Alert.AlertType.ERROR);
        return;
      }

      String strength =
          checkPasswordStrength(password);
      if (strength.equals("Trống")
          || strength.equals("Yếu")) {
        throw new PasswordStrengthCheck();
      }

      User newUser = new User(
          realname, username, email, password, phone);
      boolean succes = register.register(newUser);

      if (succes) {
        SessionManager.setCurrentUser(newUser);
        showAlert("Đăng ký thành công",
            Alert.AlertType.INFORMATION);
        switchToHome(event);
      } else {
        showAlert("Tài khoản đã tồn tại",
            Alert.AlertType.ERROR);
      }

    } catch (PasswordStrengthCheck p) {
      showAlert(
          p.getMessage()
          + " (Độ mạnh hiện tại: Yếu)",
          Alert.AlertType.WARNING);
    }
  }

  @FXML
  void handleBackToLogin(ActionEvent event) {
    try {
      Parent root = FXMLLoader.load(
          getClass().getResource("/Login.fxml"));
      Stage stage =
          (Stage) ((Node) event.getSource())
              .getScene().getWindow();
      stage.setScene(new Scene(root));
      stage.show();
    } catch (IOException e) {
      logger.error("Lỗi: {}", e.getMessage(), e);
      showAlert("Lỗi",
          "Không thể quay lại màn hình đăng nhập.");
    }
  }

}
