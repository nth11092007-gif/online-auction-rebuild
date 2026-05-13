package server.command;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.WebSocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dto.Message;
import model.AuctionSession;
import model.Bidder;
import server.AuctionFeedServer;
import server.WebSocketObserver;
import service.AuctionService;
import service.UserService;

public class JoinSessionCommand implements Command {
    private final Map<String, Set<WebSocket>> sessionSubscribers;
    private final AuctionFeedServer feedServer;
    private final AuctionService auctionService;
    private final UserService userService;
    private final Gson gson = new Gson();

    public JoinSessionCommand(Map<String, Set<WebSocket>> sessionSubscribers,
                              AuctionFeedServer feedServer,
                              AuctionService auctionService,
                              UserService userService) {
        this.sessionSubscribers = sessionSubscribers;
        this.feedServer = feedServer;
        this.auctionService = auctionService;
        this.userService = userService;
    }

    @Override
    public void execute(WebSocket conn, JsonObject jsonData) {
        String sessionId = jsonData.get("sessionId").getAsString();
        sessionSubscribers.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet()).add(conn);
        if (feedServer != null) {
            feedServer.subscribe(sessionId, new WebSocketObserver(conn));
        }

        String username = (String) conn.getAttachment();
        if (username != null) {
            Bidder bidder = userService.getUserByUsername(username);
            if (bidder != null) {
                AuctionSession session = auctionService.getSessionById(sessionId);
                if (session != null) {
                    bidder.addJoinedAuctionSession(session);
                }
            }
        }
        conn.send(gson.toJson(new Message("JOIN_SUCCESS", "Đã tham gia phiên " + sessionId)));
    }
}