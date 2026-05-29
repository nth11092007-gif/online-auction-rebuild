package server.command;

import org.java_websocket.WebSocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dto.Message;
import model.Bidder;
import service.AuctionService;
import service.ProxyBiddingService;
import service.UserService;

public class PlaceBidCommand implements Command {
    private final AuctionService auctionService;
    private final UserService userService;
    private final ProxyBiddingService proxyBiddingService;
    private final Gson gson = new Gson();

    public PlaceBidCommand(AuctionService auctionService, UserService userService) {
        this(auctionService, userService, null);
    }

    public PlaceBidCommand(AuctionService auctionService, UserService userService,
                           ProxyBiddingService proxyBiddingService) {
        this.auctionService = auctionService;
        this.userService = userService;
        this.proxyBiddingService = proxyBiddingService;
    }

    @Override
    public void execute(WebSocket conn, JsonObject jsonData) {
        String auctionID = jsonData.get("auctionId").getAsString();
        double amount = jsonData.get("amount").getAsDouble();
        Object attachment = conn.getAttachment();
        if (!(attachment instanceof String)) {
            conn.send(gson.toJson(new Message("ERROR", "Bạn chưa đăng nhập")));
            return;
        }
        String username = (String) attachment;
        Bidder bidder = userService.getUserByUsername(username);
        if (bidder == null) {
            conn.send(gson.toJson(new Message("ERROR", "Người dùng không tồn tại")));
            return;
        }
        int userId = bidder.getID();

        boolean isSuccessful = auctionService.placeBid(userId, auctionID, amount);
        if (isSuccessful && proxyBiddingService != null) {
            proxyBiddingService.processProxyBids(auctionID);
        }
        conn.send(gson.toJson(new Message("PLACE_BID_RESULT", isSuccessful ? "Đặt giá thành công!" : "Đặt giá thất bại!")));
    }
}