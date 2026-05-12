package server.command;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.WebSocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dto.Message;
import server.AuctionFeedServer;
import server.WebSocketObserver;

public class JoinSessionCommand implements Command {
    private final Map<String, Set<WebSocket>> sessionSubscribers;
    private final AuctionFeedServer feedServer;
    private final Gson gson = new Gson();

    public JoinSessionCommand(Map<String, Set<WebSocket>> sessionSubscribers, AuctionFeedServer feedServer) {
        this.sessionSubscribers = sessionSubscribers;
        this.feedServer = feedServer;
    }

    @Override
    public void execute(WebSocket conn, JsonObject jsonData) {
        String sessionId = jsonData.get("sessionId").getAsString();
        sessionSubscribers.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet()).add(conn);

        if (feedServer != null) {
            feedServer.subscribe(sessionId, new WebSocketObserver(conn));
        }

        System.out.println("👤 Người dùng " + conn.getRemoteSocketAddress() + " đã bắt đầu xem phiên: " + sessionId);
        // Phản hồi cho client biết đã join thành công (tuỳ chọn)
        conn.send(gson.toJson(new Message("JOIN_SUCCESS", "Đã tham gia phiên " + sessionId)));
    }
}