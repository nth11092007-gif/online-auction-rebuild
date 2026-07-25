package Controller;

import com.google.gson.JsonObject;
import dao.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.AuctionWebSocketClient;
import service.ServiceFactory;
import utils.AlertUtils;
import utils.NavigationManager;
import utils.SessionManager;

/** LoginController - handles user login and WebSocket authentication synchronization. */
public class LoginController {

  private static final Logger logger =
      LoggerFactory.getLogger(LoginController.class);

  @FXML
  private TextField txtUser;
  @FXML
  private PasswordField txtPassword;

  private final UserDAO userDao =
      ServiceFactory.getInstance().getUserDao();

  @FXML
  void onhandleIn(ActionEvent event) {
    String username = txtUser.getText();
    String password = txtPassword.getText();

    if (username.isEmpty() || password.isEmpty()) {
      AlertUtils.showError("Lỗi", "Vui lòng nhập tên đăng nhập và mật khẩu.");
      return;
    }

    try {
      User user = userDao.login(username, password);
      if (user == null) {
        AlertUtils.showError("Đăng nhập thất bại", "Sai tên đăng nhập hoặc mật khẩu.");
        return;
      }
      if (user.isBanned()) {
        AlertUtils.showError("Đăng nhập thất bại", "Tài khoản của bạn đã bị khoá!");
        logger.warn("Tai khoan bi ban co gang dang nhap: {}", username);
        return;
      }

      SessionManager.setCurrentUser(user);
      logger.info("Đăng nhập thành công: {}", user.getUsername());

      syncWebSocketLogin(username, password);

      if (user.getRole() == User.Role.USER) {
        NavigationManager.navigateTo(event, "/Home.fxml");
      } else if (user.getRole() == User.Role.ADMIN) {
        NavigationManager.navigateTo(event, "/HomeAdmin.fxml");
      } else {
        AlertUtils.showError("Lỗi", "Vai trò người dùng không xác định.");
      }

    } catch (Exception e) {
      logger.error("Lỗi đăng nhập", e);
      AlertUtils.showError("Lỗi hệ thống", "Không thể đăng nhập: " + e.getMessage());
    }
  }

  @FXML
  void onhandleUp(ActionEvent event) {
    NavigationManager.navigateTo(event, "/Register.fxml");
  }

  private void syncWebSocketLogin(String username, String password) {
    try {
      AuctionWebSocketClient wsClient = MainApp.getWebSocketClient();
      if (wsClient != null && wsClient.isOpen()) {
        JsonObject loginData = new JsonObject();
        loginData.addProperty("username", username);
        loginData.addProperty("password", password);
        wsClient.sendCommand("LOGIN", loginData);
        logger.info("Đã gửi LOGIN qua WebSocket cho user: {}", username);
      } else {
        logger.warn("WebSocket chưa kết nối, bỏ qua đăng nhập WebSocket");
      }
    } catch (Exception e) {
      logger.error("Lỗi khi đăng nhập WebSocket", e);
    }
  }
}
