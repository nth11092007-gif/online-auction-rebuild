package com.auction.server.websocket.command;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.auction.server.dao.UserDAO;
import com.auction.common.model.User;
import org.java_websocket.WebSocket;

/** LoginCommand - authenticates a user via username and password. */
public class LoginCommand implements Command {

  private final UserDAO userDao;

  private final Gson gson = new Gson();

  public LoginCommand(UserDAO userDao) {
    this.userDao = userDao;
  }

  @Override
  public void execute(WebSocket conn, JsonObject jsonData) {
    if (!jsonData.has("username")
        || !jsonData.has("password")) {
      sendResult(conn, "ERROR", "Thiếu username hoặc password", null);
      return;
    }
    String username =
        jsonData.get("username").getAsString();
    String password =
        jsonData.get("password").getAsString();

    User user = userDao.login(username, password);
    if (user == null) {
      sendResult(conn, "ERROR", "Sai tên đăng nhập hoặc mật khẩu", null);
      return;
    }

    conn.setAttachment(username);
    sendResult(conn, "SUCCESS", "Đăng nhập thành công", user);
  }
  
  private void sendResult(WebSocket conn, String status, String message, User user) {
    JsonObject result = new JsonObject();
    result.addProperty("type", "LOGIN_RESULT");
    result.addProperty("status", status);
    result.addProperty("message", message);
    if (user != null) {
      result.add("user", gson.toJsonTree(user));
    }
    conn.send(result.toString());
  }
}
