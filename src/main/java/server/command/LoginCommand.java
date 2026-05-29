package server.command;

import org.java_websocket.WebSocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dao.UserDAO;
import dto.Message;
import model.User;

public class LoginCommand implements Command {
    private final UserDAO userDAO;
    private final Gson gson = new Gson();

    public LoginCommand(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public void execute(WebSocket conn, JsonObject jsonData) {
        if (!jsonData.has("username") || !jsonData.has("password")) {
            conn.send(gson.toJson(new Message("ERROR", "Thiếu username hoặc password")));
            return;
        }
        String username = jsonData.get("username").getAsString();
        String password = jsonData.get("password").getAsString();

        User user = userDAO.login(username, password);
        if (user == null) {
            conn.send(gson.toJson(new Message("ERROR", "Sai tên đăng nhập hoặc mật khẩu")));
            return;
        }

        conn.setAttachment(username);
        conn.send(gson.toJson(new Message("LOGIN_SUCCESS", "Đăng nhập thành công")));
    }
}
