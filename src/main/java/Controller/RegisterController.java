package Controller;

import Exception.PasswordStrengthCheck;
import dao.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ServiceFactory;
import utils.AlertUtils;
import utils.NavigationManager;
import utils.SessionManager;

/** RegisterController - handles user registration with password strength validation. */
public class RegisterController {

  private static final Logger logger =
      LoggerFactory.getLogger(RegisterController.class);
  private final UserDAO register =
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
  public static String checkPasswordStrength(String password) {
    if (password == null || password.trim().isEmpty()) {
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
    if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
      score++;
    }
    return switch (score) {
      case 0, 1, 2 -> "Yếu";
      case 3 -> "Trung bình";
      case 4 -> "Mạnh";
      case 5 -> "Rất mạnh";
      default -> "Không xác định";
    };
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
        AlertUtils.showError("Vui lòng điền đầy đủ thông tin");
        return;
      }

      String strength = checkPasswordStrength(password);
      if ("Trống".equals(strength) || "Yếu".equals(strength)) {
        throw new PasswordStrengthCheck();
      }

      User newUser = new User(realname, username, email, password, phone);
      boolean success = register.register(newUser);

      if (success) {
        SessionManager.setCurrentUser(newUser);
        AlertUtils.showInfo("Đăng ký thành công");
        NavigationManager.navigateTo(event, "/Home.fxml");
      } else {
        AlertUtils.showError("Tài khoản đã tồn tại");
      }

    } catch (PasswordStrengthCheck p) {
      AlertUtils.showWarning(p.getMessage() + " (Độ mạnh hiện tại: Yếu)");
    }
  }

  @FXML
  void handleBackToLogin(ActionEvent event) {
    NavigationManager.navigateTo(event, "/Login.fxml");
  }
}
