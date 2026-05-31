package server.command;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dto.Message;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import model.AuctionSession;
import model.Bidder;
import org.java_websocket.WebSocket;
import server.AuctionFeedServer;
import server.WebSocketObserver;
import service.AuctionService;
import service.UserService;

/** JoinSessionCommand - subscribes a WebSocket client to an auction session. */
public class JoinSessionCommand implements Command {

  private final Map<String, Set<WebSocket>> sessionSubscribers;

  private final Map<WebSocket, Map<String, WebSocketObserver>>
      connectionObservers;

  private final AuctionFeedServer feedServer;

  private final AuctionService auctionService;

  private final UserService userService;

  private final Gson gson = new Gson();

  /**
   * Constructs a JoinSessionCommand with the given dependencies.
   *
   * @param sessionSubscribers map of session IDs to subscribed WebSocket clients
   * @param connectionObservers map of connections to their session observers
   * @param feedServer the auction feed server for pub/sub notifications
   * @param auctionService service for auction session operations
   * @param userService service for user lookup operations
   */
  public JoinSessionCommand(
      Map<String, Set<WebSocket>> sessionSubscribers,
      Map<WebSocket, Map<String, WebSocketObserver>>
          connectionObservers,
      AuctionFeedServer feedServer,
      AuctionService auctionService,
      UserService userService) {
    this.sessionSubscribers = sessionSubscribers;
    this.connectionObservers = connectionObservers;
    this.feedServer = feedServer;
    this.auctionService = auctionService;
    this.userService = userService;
  }

  @Override
  public void execute(WebSocket conn, JsonObject jsonData) {
    // 1. Validate sessionId
    if (!jsonData.has("sessionId")
        || jsonData.get("sessionId").isJsonNull()) {
      conn.send(gson.toJson(
          new Message("JOIN_FAILURE",
              "Thiếu thông tin phiên đấu giá")));
      return;
    }
    String sessionId =
        jsonData.get("sessionId").getAsString();
    if (sessionId.isEmpty()) {
      conn.send(gson.toJson(
          new Message("JOIN_FAILURE",
              "Thiếu thông tin phiên đấu giá")));
      return;
    }

    // 2. Validate user đã đăng nhập WebSocket
    String username = (String) conn.getAttachment();
    if (username == null) {
      conn.send(gson.toJson(
          new Message("JOIN_FAILURE",
              "Bạn chưa đăng nhập. "
                  + "Vui lòng đăng nhập lại!")));
      return;
    }

    // 3. Validate session tồn tại và joinable
    AuctionSession session =
        auctionService.getSessionById(sessionId);
    if (session == null) {
      conn.send(gson.toJson(
          new Message("JOIN_FAILURE",
              "Không tìm thấy phiên đấu giá")));
      return;
    }
    if (!session.joinable()) {
      conn.send(gson.toJson(
          new Message("JOIN_FAILURE",
              "Phiên đấu giá không còn "
                  + "nhận tham gia")));
      return;
    }

    // 4. Thêm vào subscribers
    sessionSubscribers
        .computeIfAbsent(
            sessionId, k -> ConcurrentHashMap.newKeySet())
        .add(conn);
    if (feedServer != null) {
      WebSocketObserver observer =
          new WebSocketObserver(conn);
      feedServer.subscribe(sessionId, observer);
      connectionObservers
          .computeIfAbsent(
              conn, k -> new ConcurrentHashMap<>())
          .put(sessionId, observer);
    }

    // 5. Ghi nhận bidder đã join
    Bidder bidder =
        userService.getUserByUsername(username);
    if (bidder != null) {
      bidder.addJoinedAuctionSession(session);
    }

    conn.send(gson.toJson(
        new Message("JOIN_SUCCESS",
            "Đã tham gia phiên " + sessionId)));
  }
}
