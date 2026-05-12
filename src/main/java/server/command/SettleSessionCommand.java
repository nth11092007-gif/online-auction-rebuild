package server.command;

import java.util.HashMap;
import java.util.Map;

import org.java_websocket.WebSocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dto.Message;
import server.AuctionFeedServer;
import service.SettlementService;

public class SettleSessionCommand implements Command {
    private final SettlementService settlementService;
    private final AuctionFeedServer feedServer;
    private final Gson gson = new Gson();

    public SettleSessionCommand(SettlementService settlementService, AuctionFeedServer feedServer) {
        this.settlementService = settlementService;
        this.feedServer = feedServer;
    }

    @Override
    public void execute(WebSocket conn, JsonObject jsonData) {
        String sessionId = jsonData.get("sessionId").getAsString();
        boolean success = settlementService.settleAuction(sessionId);

        if (success) {
            // Gửi phản hồi thành công cho người ra lệnh
            conn.send(gson.toJson(new Message("SETTLE_RESULT", "Phiên đấu giá đã được kết thúc")));
            // Broadcast cho toàn bộ người đang xem phiên
            Map<String, Object> closedMsg = new HashMap<>();
            closedMsg.put("sessionId", sessionId);
            closedMsg.put("message", "Phiên đấu giá đã kết thúc. Cảm ơn bạn đã tham gia!");
            Message broadcast = new Message("SESSION_CLOSED", closedMsg);
            String jsonBroadcast = gson.toJson(broadcast);
            if (feedServer != null) {
                feedServer.notifyObservers(sessionId, jsonBroadcast);
            }
        } else {
            conn.send(gson.toJson(new Message("ERROR", "Không thể kết thúc phiên đấu giá")));
        }
    }
}