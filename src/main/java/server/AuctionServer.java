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



import dao.UserDAOImpl;

import dto.Message;

import server.command.Command;

import server.command.GetSessionsCommand;

import server.command.GetUserCommand;

import server.command.JoinSessionCommand;

import server.command.LoginCommand;

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

    private final Map<WebSocket, Map<String, WebSocketObserver>> connectionObservers = new ConcurrentHashMap<>();

    private final AuctionFeedServer feedServer;

    private final Logger logger = LoggerFactory.getLogger(AuctionServer.class);

    private final AuctionService auctionService;

    private final UserService userService;

    private final SettlementService settlementService;

    private ProxyBiddingService proxyBiddingService;



    private final Map<String, Command> commandMap = new HashMap<>();



    public AuctionServer(int port) {

        this(port, new AuctionService(), new SettlementService(), new UserService(new UserDAOImpl()), AuctionFeedServer.getInstance());

    }



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

        if (proxyBiddingService == null) {

            proxyBiddingService = new ProxyBiddingService(auctionService);

        }

        commandMap.clear();

        commandMap.put("LOGIN", new LoginCommand(new UserDAOImpl()));

        commandMap.put("GET_SESSIONS", new GetSessionsCommand(auctionService));

        commandMap.put("GET_USER", new GetUserCommand(userService));

        commandMap.put("JOIN", new JoinSessionCommand(sessionSubscribers, connectionObservers, feedServer, auctionService, userService));

        commandMap.put("BID", new PlaceBidCommand(auctionService, userService, proxyBiddingService));

        commandMap.put("SETTLE", new SettleSessionCommand(settlementService, feedServer));

        commandMap.put("PLACE_PROXY_BID", new PlaceProxyBidCommand(proxyBiddingService, userService));

    }



    @Override

    public void onStart() {

        auctionService.setEventPublisher(feedServer != null ? feedServer::notifyObservers : null);

        proxyBiddingService = new ProxyBiddingService(auctionService);

        initCommands();

        System.out.println("Auction Server đã khởi động thành công trên port: " + getPort());

    }



    @Override

    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {

        System.out.println("🟢 Có người dùng mới kết nối: " + webSocket.getRemoteSocketAddress());

    }



    @Override

    public void onClose(WebSocket conn, int requestCode, String reason, boolean remote) {

        System.out.println("🔴 Người dùng đã ngắt kết nối: " + conn.getRemoteSocketAddress());

        sessionSubscribers.values().forEach(subscribers -> subscribers.remove(conn));



        Map<String, WebSocketObserver> observers = connectionObservers.remove(conn);

        if (observers != null && feedServer != null) {

            observers.forEach((sessionId, observer) -> feedServer.unsubscribe(sessionId, observer));

        }

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


