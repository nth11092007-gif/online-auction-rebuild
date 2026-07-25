package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dto.Message;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.command.Command;
import server.command.GetSessionsCommand;
import server.command.GetUserCommand;
import server.command.JoinSessionCommand;
import server.command.LoginCommand;
import server.command.PlaceBidCommand;
import server.command.SettleSessionCommand;
import service.AuctionService;
import service.SettlementService;
import service.UserService;

/** AuctionServer - WebSocket server that handles auction commands and real-time events. */
public class AuctionServer extends WebSocketServer {

  private final Gson gson = new Gson();

  private final Map<String, Set<WebSocket>> sessionSubscribers =
      new ConcurrentHashMap<>();

  private final Map<WebSocket, Map<String, WebSocketObserver>>
      connectionObservers = new ConcurrentHashMap<>();

  private final AuctionFeedServer feedServer;

  private final Logger logger =
      LoggerFactory.getLogger(AuctionServer.class);

  private final AuctionService auctionService;

  private final UserService userService;

  private final SettlementService settlementService;

  private final ExecutorService commandExecutor =
      Executors.newFixedThreadPool(10);

  private final Map<String, Command> commandMap = new HashMap<>();

  /**
   * Constructs an AuctionServer on the given port with default services.
   *
   * @param port the port number to listen on
   */
  public AuctionServer(int port) {
      this(port,
              service.ServiceFactory.getInstance().getAuctionService(),
              service.ServiceFactory.getInstance().getSettlementService(),
              service.ServiceFactory.getInstance().getUserService(),
              AuctionFeedServer.getInstance());
  }

  /**
   * Constructs an AuctionServer with custom service dependencies.
   *
   * @param port the port number to listen on
   * @param auctionService the auction service for bid and session operations
   * @param settlementService the settlement service for closing sessions
   * @param userService the user service for authentication and lookup
   * @param feedServer the feed server for broadcasting events
   */
  public AuctionServer(int port, AuctionService auctionService,
      SettlementService settlementService,
      UserService userService,
      AuctionFeedServer feedServer) {
    super(new InetSocketAddress(port));

    this.auctionService = auctionService;
    this.userService = userService;
    this.settlementService = settlementService;
    this.feedServer = feedServer;

    initCommands();
  }

  private void initCommands() {
    commandMap.clear();
    commandMap.put("LOGIN",
        new LoginCommand(userService.getUserDao()));

    commandMap.put("GET_SESSIONS",
        new GetSessionsCommand(auctionService));
    commandMap.put("GET_USER",
        new GetUserCommand(userService));
    commandMap.put("JOIN",
        new JoinSessionCommand(sessionSubscribers,
            connectionObservers, feedServer,
            auctionService, userService));
    commandMap.put("BID",
        new PlaceBidCommand(auctionService, userService));
    commandMap.put("SETTLE",
        new SettleSessionCommand(
            settlementService, feedServer));
  }

  @Override
  public void onStart() {
    auctionService.setEventPublisher(
        feedServer != null
            ? feedServer::notifyObservers : null);
    initCommands();
    logger.info(
        "Auction Server đã khởi động thành công trên port: {}",
        getPort());
  }

  @Override
  public void onOpen(WebSocket webSocket,
      ClientHandshake clientHandshake) {
    logger.info("Có người dùng mới kết nối: {}",
        webSocket.getRemoteSocketAddress());
  }

  @Override
  public void onClose(WebSocket conn, int requestCode,
      String reason, boolean remote) {
    logger.info("Người dùng đã ngắt kết nối: {}",
        conn.getRemoteSocketAddress());
    sessionSubscribers.values()
        .forEach(subscribers -> subscribers.remove(conn));

    Map<String, WebSocketObserver> observers =
        connectionObservers.remove(conn);
    if (observers != null && feedServer != null) {
      observers.forEach(
          (sessionId, observer) ->
              feedServer.unsubscribe(sessionId, observer));
    }
  }

  @Override
  public void onMessage(WebSocket webSocket, String message) {
    logger.info("Nhận được tin nhắn từ client: {}", message);
    try {
      JsonObject jsonObject =
          JsonParser.parseString(message).getAsJsonObject();
      if (!jsonObject.has("type")) {
        return;
      }
      String type = jsonObject.get("type").getAsString();

      Command command = commandMap.get(type);
      if (command != null) {
        commandExecutor.submit(() -> {
          try {
            command.execute(webSocket, jsonObject);
          } catch (Exception e) {
            logger.error("Lỗi thực thi command {}: {}",
                type, e.getMessage(), e);
          }
        });
      } else {
        webSocket.send(gson.toJson(
            new Message("ERROR", "Lệnh không xác định")));
      }
    } catch (Exception e) {
      logger.error(
          "Lỗi xử lý tin nhắn: " + e.getMessage());
      try {
        JsonObject errorResponse = new JsonObject();
        errorResponse.addProperty(
            "type", "PLACE_BID_RESULT");
        errorResponse.addProperty("status", "FAILURE");
        errorResponse.addProperty("message",
            "Lỗi xử lý: " + e.getMessage());
        webSocket.send(errorResponse.toString());
      } catch (Exception ex) {
        logger.error(
            "Không thể gửi lỗi về client: "
                + ex.getMessage());
      }
    }
  }

  @Override
  public void onError(WebSocket webSocket, Exception e) {
    if (webSocket != null) {
      logger.error("Lỗi trên kết nối: "
          + webSocket.getRemoteSocketAddress(), e);
    }
  }

  /**
   * Dong thread pool xu ly command. Goi khi server dung.
   */
  public void shutdown() {
    commandExecutor.shutdownNow();
    logger.info("Command executor đã đóng.");
  }
}
