package server.command;

import java.util.HashMap;
import java.util.Map;

import org.java_websocket.WebSocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dto.Message;
import model.User;
import service.UserService;

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
            conn.send(gson.toJson(new Message("ERROR", "User không tồn tại")));
            return;
        }

        // Chỉ gửi những trường không nhạy cảm
        Map<String, Object> safeUser = new HashMap<>();
        safeUser.put("id", user.getID());
        safeUser.put("username", user.getUsername());
        safeUser.put("balance", user.getBalance());
        safeUser.put("role", user.getRole().name()); // nếu Role là enum
        // Không có password!

        conn.send(gson.toJson(new Message("USER_INFO", safeUser)));
    }
}