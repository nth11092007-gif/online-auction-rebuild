package server;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dto.Message;
import server.command.Command;
import server.command.GetSessionsCommand;
import server.command.GetUserCommand;
import server.command.JoinSessionCommand;
import server.command.PlaceBidCommand;
import server.command.SettleSessionCommand;
import service.AuctionService;
import service.SettlementService;
import service.UserService;

public class AuctionServer extends WebSocketServer {
    private final Gson gson = new Gson();
    private final Map<String, Set<WebSocket>> sessionSubscribers = new ConcurrentHashMap<>();
    private AuctionFeedServer feedServer;
    private final Logger logger = LoggerFactory.getLogger(AuctionServer.class);
    // Các service
    private final AuctionService auctionService;
    private final UserService userService;
    private final SettlementService settlementService;

    // Command map thay cho switch-case
    private final Map<String, Command> commandMap = new HashMap<>();

    public AuctionServer(int port) {
        super(new InetSocketAddress(port));
        this.auctionService = new AuctionService();
        this.userService = new UserService();
        this.settlementService = new SettlementService();
    }

    @Override
    public void onStart() {
        feedServer = new AuctionFeedServer();
        System.out.println("🚀 Auction Server đã khởi động thành công trên port: " + getPort());

        // Khởi tạo command map (sau khi feedServer đã có)
        commandMap.put("GET_SESSIONS", new GetSessionsCommand(auctionService));
        commandMap.put("GET_USER", new GetUserCommand(userService));
        commandMap.put("JOIN", new JoinSessionCommand(sessionSubscribers, feedServer, auctionService, userService));
        commandMap.put("BID", new PlaceBidCommand(auctionService, userService)); // PlaceBidCommand cần inject cả userService
        commandMap.put("SETTLE", new SettleSessionCommand(settlementService, feedServer));
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println("🟢 Có người dùng mới kết nối: " + webSocket.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int requestCode, String reason, boolean remote) {
        System.out.println("🔴 Người dùng đã ngắt kết nối: " + conn.getRemoteSocketAddress());
        // Xóa khỏi tất cả các phiên đăng ký
        sessionSubscribers.values().forEach(subscribers -> subscribers.remove(conn));
    }

    @Override
    public void onMessage(WebSocket webSocket, String message) {
        System.out.println("📩 Nhận được tin nhắn từ client: " + message);
        try {
            JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
            if (!jsonObject.has("type")) return;
            String type = jsonObject.get("type").getAsString();

            Command command = commandMap.get(type);
            if (command != null) {
                command.execute(webSocket, jsonObject);
            } else {
                webSocket.send(gson.toJson(new Message("ERROR", "Command Unknown")));
            }
        } catch (Exception e) {
            logger.error("❌ Lỗi xử lý tin nhắn: " + e.getMessage());
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        if (webSocket != null) {
            logger.error("❌ Lỗi trên kết nối: " + webSocket.getRemoteSocketAddress(), e);
        }
    }
}