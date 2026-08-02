package com.auction.server.websocket.command;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.auction.server.dao.UserDAO;
import com.auction.common.model.User;
import org.java_websocket.WebSocket;

public class RegisterCommand implements Command {

  private final UserDAO userDao;
  private final Gson gson = new Gson();

  public RegisterCommand(UserDAO userDao) {
    this.userDao = userDao;
  }

  @Override
  public void execute(WebSocket conn, JsonObject jsonData) {
    if (!jsonData.has("username") || !jsonData.has("password") ||
        !jsonData.has("email") || !jsonData.has("phone") || !jsonData.has("realname")) {
      sendResult(conn, "ERROR", "Vui lòng cung cấp đủ thông tin", null);
      return;
    }
    
    String username = jsonData.get("username").getAsString();
    String password = jsonData.get("password").getAsString();
    String email = jsonData.get("email").getAsString();
    String phone = jsonData.get("phone").getAsString();
    String realname = jsonData.get("realname").getAsString();

    User newUser = new User(realname, username, email, password, phone);
    boolean success = userDao.register(newUser);

    if (success) {
      // Set attachment logic? Maybe don't login automatically or do it
      conn.setAttachment(username);
      
      // Fetch user again to get generated ID if needed, or if register sets it
      User registeredUser = userDao.login(username, password);
      sendResult(conn, "SUCCESS", "Đăng ký thành công", registeredUser != null ? registeredUser : newUser);
    } else {
      sendResult(conn, "ERROR", "Tài khoản đã tồn tại", null);
    }
  }
  
  private void sendResult(WebSocket conn, String status, String message, User user) {
    JsonObject result = new JsonObject();
    result.addProperty("type", "REGISTER_RESULT");
    result.addProperty("status", status);
    result.addProperty("message", message);
    if (user != null) {
      result.add("user", gson.toJsonTree(user));
    }
    conn.send(result.toString());
  }
}
