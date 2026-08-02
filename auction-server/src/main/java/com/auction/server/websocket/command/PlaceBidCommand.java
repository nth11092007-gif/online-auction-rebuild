package com.auction.server.websocket.command;

import com.google.gson.JsonObject;
import com.auction.common.model.Bidder;
import org.java_websocket.WebSocket;
import com.auction.server.service.AuctionService;
import com.auction.server.service.UserService;

/** PlaceBidCommand - handles a bid placement request from a WebSocket client. */
public class PlaceBidCommand implements Command {

  private final AuctionService auctionService;

  private final UserService userService;


  public PlaceBidCommand(AuctionService auctionService,
      UserService userService) {
    this.auctionService = auctionService;
    this.userService = userService;
  }

  @Override
  public void execute(WebSocket conn, JsonObject jsonData) {
    try {
      String auctionId =
          jsonData.get("auctionId").getAsString();
      double amount =
          jsonData.get("amount").getAsDouble();
      Object attachment = conn.getAttachment();
      if (!(attachment instanceof String)) {
        sendResult(conn, "FAILURE",
            "Bạn chưa đăng nhập. "
                + "Vui lòng đăng nhập lại!");
        return;
      }
      String username = (String) attachment;
      Bidder bidder =
          userService.getUserByUsername(username);
      if (bidder == null) {
        sendResult(conn, "FAILURE",
            "Người dùng không tồn tại");
        return;
      }
      int userId = bidder.getId();

      boolean isSuccessful =
          auctionService.placeBid(
              userId, auctionId, amount);

      sendResult(conn,
          isSuccessful ? "SUCCESS" : "FAILURE",
          isSuccessful
              ? "Đặt giá thành công!"
              : "Đặt giá thất bại!");
    } catch (Exception e) {
      sendResult(conn, "FAILURE",
          "Lỗi server: " + e.getMessage());
    }
  }

  private void sendResult(WebSocket conn, String status,
      String message) {
    JsonObject result = new JsonObject();
    result.addProperty("type", "PLACE_BID_RESULT");
    result.addProperty("status", status);
    result.addProperty("message", message);
    conn.send(result.toString());
  }
}
