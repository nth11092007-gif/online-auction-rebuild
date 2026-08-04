package com.auction.server.websocket;

/** AuctionServerLauncher - standalone launcher for the Auction WebSocket server. */
public final class AuctionServerLauncher {

  private static final int PORT = 8887;

  private AuctionServerLauncher() {
    // Hide utility class constructor
  }

  /** Starts the AuctionServer on the default port. */
  public static void main(String[] args) {
    AuctionServer server = new AuctionServer(PORT);
    server.start();
    System.out.println(
        "AuctionServer đã chạy trên cổng " + PORT);
  }
}
