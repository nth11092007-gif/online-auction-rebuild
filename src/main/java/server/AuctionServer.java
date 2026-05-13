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

import dao.AuctionSessionDAO;
import dao.AuctionSessionDAOImpl;
import dao.BidDAO;
import dao.BidDAOImpl;
import dao.ItemDAO;
import dao.ItemDAOImpl;
import dao.UserDAO;
import dao.UserDAOImpl;
import dto.Message;
import server.command.Command;
import server.command.GetSessionsCommand;
import server.command.GetUserCommand;
import server.command.JoinSessionCommand;
import server.command.PlaceBidCommand;
import server.command.PlaceProxyBidCommand;
import server.command.SettleSessionCommand;
import service.AuctionService;
import service.ProxyBiddingService;
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
    private ProxyBiddingService proxyBiddingService;

    // Command map thay cho switch-case
    private final Map<String, Command> commandMap = new HashMap<>();

    public AuctionServer(int port) {
        this(port, new AuctionService(), new SettlementService(), new UserService(), new AuctionFeedServer());
    }

    public AuctionServer(int port, AuctionService auctionService,
                         SettlementService settlementService,
                         UserService userService,
                         AuctionFeedServer feedServer) {
        super(new InetSocketAddress(port));
        // Tạo DAO implementations
        UserDAO userDAO = new UserDAOImpl();
        BidDAO bidDAO = new BidDAOImpl();
        AuctionSessionDAO sessionDAO = new AuctionSessionDAOImpl();
        ItemDAO itemDAO = new ItemDAOImpl(); // nếu cần

        // Inject vào service
        this.auctionService = new AuctionService(userDAO, bidDAO, sessionDAO);
        this.userService = new UserService(userDAO);
        this.settlementService = new SettlementService(sessionDAO, bidDAO, userDAO, itemDAO);
        this. proxyBiddingService = new ProxyBiddingService(auctionService);
    }

    @Override
    public void onStart() {
        this.feedServer = new AuctionFeedServer();
        System.out.println("🚀 Auction Server đã khởi động thành công trên port: " + getPort());
        this.proxyBiddingService = new ProxyBiddingService(auctionService);
        // Khởi tạo command map (sau khi feedServer đã có)
        commandMap.put("GET_SESSIONS", new GetSessionsCommand(auctionService));
        commandMap.put("GET_USER", new GetUserCommand(userService));
        commandMap.put("JOIN", new JoinSessionCommand(sessionSubscribers, feedServer, auctionService, userService));
        commandMap.put("BID", new PlaceBidCommand(auctionService, userService)); // PlaceBidCommand cần inject cả userService
        commandMap.put("SETTLE", new SettleSessionCommand(settlementService, feedServer));
        commandMap.put("PLACE_PROXY_BID", new PlaceProxyBidCommand(proxyBiddingService, userService));
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
    /**
     * onMessage: Khi server nhận được một tin nhắn từ client, phương thức này sẽ được gọi.
     * @param webSocket: Đối tượng WebSocket đại diện cho kết nối gửi tin nhắn.
     * @param message: Nội dung tin nhắn nhận được, thường là một chuỗi JSON chứa thông tin về hành động mà client muốn thực hiện (ví dụ: đặt giá, tham gia phiên đấu giá, v.v.).
     * Phương thức này sẽ phân tích nội dung tin nhắn, xác định loại hành động và gọi các hàm xử lý logic tương ứng trong service của bạn để thực hiện hành động đó. 
     * Sau khi xử lý, server có thể gửi phản hồi hoặc thông báo đến các client khác nếu cần thiết.
     * (như vòng main của server để liên tục lắng nghe và xử lý các yêu cầu từ client)
     */
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
<<<<<<<<< Temporary merge branch 1

    @Override
    public void onStart() {
        this.feedServer = new AuctionFeedServer();
        this.auctionService.setFeedServer(this.feedServer);
        System.out.println("🚀 Auction Server đã khởi động thành công trên port: " + getPort());
    }

}
=========
}
>>>>>>>>> Temporary merge branch 2
