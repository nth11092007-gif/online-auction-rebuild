package com.auction.client.controller;

import com.auction.client.utils.SessionManager;
import com.auction.common.exception.PasswordStrengthCheck;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.auction.common.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.auction.client.utils.AlertUtils;
import com.auction.client.utils.NavigationManager;
import com.auction.client.MainApp;
import com.auction.client.network.AuctionWebSocketClient;
import com.google.gson.JsonObject;
import com.google.gson.Gson;

/** RegisterController - handles user registration with password strength validation. */
public class RegisterController {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(RegisterController.class);
      
  private final Gson gson = new Gson();

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

      AuctionWebSocketClient wsClient = MainApp.getWebSocketClient();
      if (wsClient == null || !wsClient.isOpen()) {
        AlertUtils.showError("Lỗi", "Không thể kết nối đến máy chủ.");
        return;
      }
      
      wsClient.addHandler("REGISTER_RESULT", json -> {
        javafx.application.Platform.runLater(() -> {
        String status = json.has("status") ? json.get("status").getAsString() : "ERROR";
        if ("SUCCESS".equals(status)) {
          User returnedUser = gson.fromJson(json.get("user"), User.class);
          SessionManager.setCurrentUser(returnedUser);
          AlertUtils.showInfo("Đăng ký thành công");
          NavigationManager.navigateTo(event, "/Home.fxml");
        } else {
          String msg = json.has("message") ? json.get("message").getAsString() : "Tài khoản đã tồn tại";
          AlertUtils.showError(msg);
        }
        });
        wsClient.removeHandler("REGISTER_RESULT");
      });
      
      JsonObject registerData = new JsonObject();
      registerData.addProperty("username", username);
      registerData.addProperty("password", password);
      registerData.addProperty("email", email);
      registerData.addProperty("phone", phone);
      registerData.addProperty("realname", realname);
      
      wsClient.sendCommand("REGISTER", registerData);

    } catch (PasswordStrengthCheck p) {
      AlertUtils.showWarning(p.getMessage() + " (Độ mạnh hiện tại: Yếu)");
    } catch (Exception e) {
      LOGGER.error("Lỗi đăng ký", e);
      AlertUtils.showError("Đã có lỗi xảy ra");
    }
  }

  @FXML
  void handleBackToLogin(ActionEvent event) {
    NavigationManager.navigateTo(event, "/Login.fxml");
  }
}


