package server.command;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dto.Message;
import java.util.List;
import model.AuctionSession;
import org.java_websocket.WebSocket;
import service.AuctionService;

/** GetSessionsCommand - retrieves and returns all auction sessions. */
public class GetSessionsCommand implements Command {

  private final AuctionService auctionService;

  private final Gson gson = new Gson();

  public GetSessionsCommand(AuctionService auctionService) {
    this.auctionService = auctionService;
  }

  @Override
  public void execute(WebSocket conn, JsonObject jsonData) {
    List<AuctionSession> sessions =
        auctionService.getAllSessions();
    Message msg = new Message("SESSION_LIST", sessions);
    conn.send(gson.toJson(msg));
  }
}
