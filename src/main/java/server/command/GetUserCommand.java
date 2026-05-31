package server.command;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dto.Message;
import java.util.HashMap;
import java.util.Map;
import model.User;
import org.java_websocket.WebSocket;
import service.UserService;

/** GetUserCommand - retrieves user information by user ID. */
public class GetUserCommand implements Command {

  private final UserService userService;

  private final Gson gson = new Gson();

  public GetUserCommand(UserService userService) {
    this.userService = userService;
  }

  @Override
  public void execute(WebSocket conn, JsonObject jsonData) {
    int userId = jsonData.get("userId").getAsInt();
    User user = userService.getUserById(userId);

    if (user == null) {
      conn.send(gson.toJson(
          new Message("ERROR", "User không tồn tại")));
      return;
    }

    // Chi gui nhung truong nhay cam
    Map<String, Object> safeUser = new HashMap<>();
    safeUser.put("id", user.getId());
    safeUser.put("username", user.getUsername());
    safeUser.put("balance", user.getBalance());
    safeUser.put("role", user.getRole().name());

    conn.send(gson.toJson(
        new Message("USER_INFO", safeUser)));
  }
}
