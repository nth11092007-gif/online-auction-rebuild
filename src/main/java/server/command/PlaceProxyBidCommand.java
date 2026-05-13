// server/command/PlaceProxyBidCommand.java
package server.command;

import org.java_websocket.WebSocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dto.Message;
import service.ProxyBiddingService;
import service.UserService;

public class PlaceProxyBidCommand implements Command {
    private final ProxyBiddingService proxyBiddingService;
    private final UserService userService;
    private final Gson gson = new Gson();

    public PlaceProxyBidCommand(ProxyBiddingService proxyBiddingService, UserService userService) {
        this.proxyBiddingService = proxyBiddingService;
        this.userService = userService;
    }

    @Override
    public void execute(WebSocket conn, JsonObject jsonData) {
        String username = (String) conn.getAttachment();
        if (username == null) {
            conn.send(gson.toJson(new Message("ERROR", "Bạn chưa đăng nhập")));
            return;
        }
        // Lấy userId từ username (qua service)
        int userId = userService.getUserByUsername(username).getID(); // đảm bảo có method này hoặc tương tự
        String sessionId = jsonData.get("sessionId").getAsString();
        double maxAmount = jsonData.get("maxAmount").getAsDouble();

        try {
            proxyBiddingService.placeProxyBid(userId, sessionId, maxAmount);
            conn.send(gson.toJson(new Message("PROXY_SUCCESS", "Đặt proxy bid thành công")));
        } catch (IllegalArgumentException e) {
            conn.send(gson.toJson(new Message("ERROR", e.getMessage())));
        } catch (Exception e) {
            conn.send(gson.toJson(new Message("ERROR", "Lỗi hệ thống khi đặt proxy bid")));
            e.printStackTrace();
        }
    }
}