package server.command;

import java.util.List;

import org.java_websocket.WebSocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dto.Message;
import model.AuctionSession;
import service.AuctionService;

public class GetSessionsCommand implements Command {
    private final AuctionService auctionService;
    private final Gson gson = new Gson();

    public GetSessionsCommand(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @Override
    public void execute(WebSocket conn, JsonObject jsonData) {
        List<AuctionSession> sessions = auctionService.getAllSessions();
        // Dùng Message DTO để bọc dữ liệu, không nối chuỗi thủ công
        Message msg = new Message("SESSION_LIST", sessions);
        conn.send(gson.toJson(msg));
    }
}