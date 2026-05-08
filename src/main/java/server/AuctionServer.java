package server;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import model.AuctionSession;
import model.User;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dto.BidRequest;
import service.AuctionService;
import service.SettlementService;
import service.UserService;

public class AuctionServer extends WebSocketServer {
    private static final Logger logger = LoggerFactory.getLogger(AuctionServer.class);
    // Khởi tạo service của bạn để gọi các hàm xử lý logic
    private final SettlementService settlementService;
    private final Gson gson = new Gson();
    private final AuctionService auctionService;
    private final UserService userService;
    private final Map<String, Set<WebSocket>> sessionSubscribers = new ConcurrentHashMap<>();
    private AuctionFeedServer feedServer;

    public AuctionServer(int port) {
        super(new InetSocketAddress(port));
        this.settlementService = new SettlementService();
        this.auctionService = new AuctionService();
        this.userService = new UserService();
    }
    /**
    * onOpen: Khi có một kết nối WebSocket mới được mở, phương thức này sẽ được gọi.
    * @param webSocket: Đối tượng WebSocket đại diện cho kết nối mới.
    * @param clientHandshake: Chứa thông tin về handshake của kết nối, có thể dùng để xác thực hoặc lấy thông tin client nếu cần.
    * (handshake: quá trình thiết lập kết nối WebSocket, bao gồm việc trao đổi thông tin giữa client và server để xác nhận kết nối) 
    */
    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println("🟢 Có người dùng mới kết nối: " + webSocket.getRemoteSocketAddress());
    }
    /**
     * onClose:
     * @param conn: Kết nối WebSocket bị đóng
     * @param requestCode: Mã yêu cầu đóng kết nối (nếu có)
     * @param reason: Lý do đóng kết nối
     * @param remote: true nếu kết nối bị đóng từ phía client, false nếu từ phía server
     */
    @Override
    public void onClose(WebSocket conn, int requestCode, String reason, boolean remote) {
        System.out.println("🔴 Người dùng đã ngắt kết nối: " + conn.getRemoteSocketAddress());
        sessionSubscribers.values().forEach(subscribers -> subscribers.remove(conn));
    }
    /**
     * Xử lý logic đặt giá khi nhận được yêu cầu từ client.
     * @param conn: Kết nối WebSocket của client gửi yêu cầu đặt giá
     * @param request: Đối tượng chứa thông tin về yêu cầu đặt giá, bao gồm userId, sessionId và bidAmount
     */
    private void processBid(WebSocket conn, BidRequest request) {
        // 1. Gọi service xử lý logic đấu giá
        boolean isSuccess = auctionService.placeBid(
                request.getUserId(),
                request.getSessionId(),
                request.getBidAmount()
        );

        if (isSuccess) {
            System.out.println("✅ Đặt giá thành công!");

            // 2. Tạo JSON thông báo giá mới
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("type", "NEW_BID");
            responseData.put("sessionId", request.getSessionId());
            responseData.put("newPrice", request.getBidAmount());
            String jsonResponse = gson.toJson(responseData);

            // 3. Lọc và gửi thông báo CHỈ cho những người đang xem phiên này
            feedServer.notifyObservers(request.getSessionId(), jsonResponse);

        } else {
            // Gửi lỗi cho riêng người vừa đặt giá
            conn.send("{\"type\": \"ERROR\", \"message\": \"Đặt giá thất bại! Số dư không đủ hoặc giá không hợp lệ.\"}");
        }
    }
    /**
     * Xử lý logic kết thúc phiên đấu giá khi nhận được yêu cầu từ client (tự động).
     * @param conn: Kết nối WebSocket của client gửi yêu cầu kết thúc phiên
     * @param sessionId: ID của phiên đấu giá cần kết thúc
     */
    private void processSettlement(WebSocket conn, String sessionId) {
        // 1. Gọi service xử lý logic kết thúc phiên
        boolean isSuccess = settlementService.settleAuction(sessionId);

        if (isSuccess) {
            System.out.println("✅ Đã chốt phiên đấu giá thành công: " + sessionId);

            // 2. Tạo JSON thông báo phiên đã đóng
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("type", "SESSION_CLOSED");
            responseData.put("sessionId", sessionId);
            responseData.put("message", "Phiên đấu giá đã kết thúc. Cảm ơn bạn đã tham gia!");

            String jsonResponse = gson.toJson(responseData);

            // 3. Phát thanh thông báo cho những người đang xem phiên này biết
            if (feedServer != null) {
                feedServer.notifyObservers(sessionId, jsonResponse);
            }

        } else {
            // Gửi lỗi cho người vừa ra lệnh đóng phiên (có thể là admin)
            conn.send("{\"type\": \"ERROR\", \"message\": \"Lỗi: Không thể kết thúc phiên đấu giá!\"}");
        }
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
            // 1. Phân tích chuỗi JSON thành JsonObject tổng quát
            JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();

            // 2. Lấy trường "type"
            if (!jsonObject.has("type")) return;
            String type = jsonObject.get("type").getAsString();

            // 3. Rẽ nhánh logic
            switch (type) {
                case "GET_SESSIONS":
                    /**
                     * Hợp nhất từ HTTP: Lấy danh sách tất cả phiên đấu giá
                     */
                    List<AuctionSession> sessions = auctionService.getAllSessions();
                    String sessionListJson = gson.toJson(sessions);
                    webSocket.send("{\"type\": \"SESSION_LIST\", \"data\": " + sessionListJson + "}");
                    break;

                case "GET_USER":
                    /**
                     * Hợp nhất từ HTTP: Lấy thông tin người dùng (số dư, tên...)
                     */
                    int userId = jsonObject.get("userId").getAsInt();
                    User user = userService.getUserById(userId);
                    if (user != null) {
                        webSocket.send("{\"type\": \"USER_INFO\", \"data\": " + gson.toJson(user) + "}");
                    } else {
                        webSocket.send("{\"type\": \"ERROR\", \"message\": \"User không tồn tại!\"}");
                    }
                    break;
                case "JOIN":
                    /**
                     * Đăng ký người dùng vào một phiên để nhận tin nhắn real-time
                     */
                    String joinSessionId = jsonObject.get("sessionId").getAsString();
                    sessionSubscribers.computeIfAbsent(joinSessionId, k -> ConcurrentHashMap.newKeySet()).add(webSocket);

                    feedServer.subscribe(joinSessionId, new WebSocketObserver(webSocket));

                    System.out.println("👤 Người dùng " + webSocket.getRemoteSocketAddress() + " đã bắt đầu xem phiên: " + joinSessionId);
                    break;

                case "BID":
                    // Chuyển phần còn lại thành BidRequest như cũ
                    BidRequest request = gson.fromJson(jsonObject, BidRequest.class);
                    processBid(webSocket, request); // Chuyển logic đặt giá vào hàm riêng cho gọn
                    break;
                case "SETTLE" :
                    String settleSessionId = jsonObject.get("sessionId").getAsString();
                    processSettlement(webSocket, settleSessionId);
                    break;
                default:
                    webSocket.send("{\"type\": \"ERROR\", \"message\": \"Command Unknown\"}");
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

    @Override
    public void onStart() {
        this.feedServer = new AuctionFeedServer();
        System.out.println("🚀 Auction Server đã khởi động thành công trên port: " + getPort());
    }

}
