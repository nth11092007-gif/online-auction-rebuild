package server;

/** AuctionServerLauncher - standalone launcher for the Auction WebSocket server. */
public class AuctionServerLauncher {

  /** Starts the AuctionServer on the default port. */
  public static void main(String[] args) {
    int port = 8887;
    AuctionServer server = new AuctionServer(port);
    server.start();
    System.out.println(
        "AuctionServer đã chạy trên cổng " + port);
  }
}
