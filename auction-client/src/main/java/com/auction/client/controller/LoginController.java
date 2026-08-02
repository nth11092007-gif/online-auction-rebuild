package com.auction.client.controller;

import com.auction.client.MainApp;
import com.google.gson.JsonObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.auction.common.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.auction.client.network.AuctionWebSocketClient;
import com.auction.client.utils.AlertUtils;
import com.auction.client.utils.NavigationManager;
import com.auction.client.utils.SessionManager;
import com.google.gson.Gson;

/** LoginController - handles user login and WebSocket authentication synchronization. */
public class LoginController {

  private static final Logger logger =
      LoggerFactory.getLogger(LoginController.class);

  @FXML
  private TextField txtUser;
  @FXML
  private PasswordField txtPassword;
  
  private final Gson gson = new Gson();

  @FXML
  void onhandleIn(ActionEvent event) {
    String username = txtUser.getText();
    String password = txtPassword.getText();

    if (username.isEmpty() || password.isEmpty()) {
      AlertUtils.showError("Lỗi", "Vui lòng nhập tên đăng nhập và mật khẩu.");
      return;
    }

    try {
      AuctionWebSocketClient wsClient = MainApp.getWebSocketClient();
      if (wsClient == null || !wsClient.isOpen()) {
        AlertUtils.showError("Lỗi kết nối", "Không thể kết nối đến máy chủ.");
        return;
      }
      
      wsClient.addHandler("LOGIN_RESULT", json -> {
        javafx.application.Platform.runLater(() -> {
        String status = json.has("status") ? json.get("status").getAsString() : "ERROR";
        if ("SUCCESS".equals(status)) {
          User user = gson.fromJson(json.get("user"), User.class);
          SessionManager.setCurrentUser(user);
          logger.info("Đăng nhập thành công qua WS: {}", user.getUsername());
          
          if (user.getRole() == User.Role.USER) {
            NavigationManager.navigateTo(event, "/Home.fxml");
          } else if (user.getRole() == User.Role.ADMIN) {
            NavigationManager.navigateTo(event, "/HomeAdmin.fxml");
          }
        } else {
          String msg = json.has("message") ? json.get("message").getAsString() : "Đăng nhập thất bại";
          AlertUtils.showError("Lỗi đăng nhập", msg);
        }
        });
        // Remove handler sau khi xong
        wsClient.removeHandler("LOGIN_RESULT");
      });
      
      JsonObject loginData = new JsonObject();
      loginData.addProperty("username", username);
      loginData.addProperty("password", password);
      wsClient.sendCommand("LOGIN", loginData);
      
    } catch (Exception e) {
      logger.error("Lỗi gửi yêu cầu đăng nhập", e);
      AlertUtils.showError("Lỗi hệ thống", "Không thể gửi yêu cầu: " + e.getMessage());
    }
  }

  @FXML
  void onhandleUp(ActionEvent event) {
    NavigationManager.navigateTo(event, "/Register.fxml");
  }


}


