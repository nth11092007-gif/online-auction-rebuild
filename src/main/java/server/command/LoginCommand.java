package server.command;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dao.UserDAO;
import dto.Message;
import model.User;
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
      conn.send(gson.toJson(
          new Message("ERROR",
              "Thiếu username hoặc password")));
      return;
    }
    String username =
        jsonData.get("username").getAsString();
    String password =
        jsonData.get("password").getAsString();

    User user = userDao.login(username, password);
    if (user == null) {
      conn.send(gson.toJson(
          new Message("ERROR",
              "Sai tên đăng nhập hoặc mật khẩu")));
      return;
    }

    conn.setAttachment(username);
    conn.send(gson.toJson(
        new Message("LOGIN_SUCCESS",
            "Đăng nhập thành công")));
  }
}
