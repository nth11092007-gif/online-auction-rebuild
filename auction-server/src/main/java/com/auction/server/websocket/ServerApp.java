package com.auction.server.websocket;

import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Console app de chay Auction Server.
 * Chay ServerApp -> server start tren port 8887 -> nhap "stop" de tat.
 * Dung ngrok de expose: ngrok tcp 8887
 */
public final class ServerApp {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerApp.class);

  private ServerApp() {
    // Hide implicit public constructor
  }

  private static final int DEFAULT_PORT = 8887;

  /** Starts the auction server and waits for a stop command from stdin. */
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);

    System.out.println("=== Máy chủ Đấu giá ===");
    System.out.println("Cổng: " + DEFAULT_PORT);
    System.out.println("Đang khởi động...");

    AuctionServer server = new AuctionServer(DEFAULT_PORT);
    server.start();

    System.out.println(
        "Server đang chạy trên port " + DEFAULT_PORT);
    System.out.println(
        "Để expose qua ngrok: ngrok tcp " + DEFAULT_PORT);
    System.out.println("Nhập 'stop' để tắt.\n");

    while (true) {
      String cmd = scanner.nextLine().trim();
      if ("stop".equalsIgnoreCase(cmd)) {
        System.out.println("Đang tắt server...");
        try {
          server.stop(1000);
        } catch (InterruptedException e) {
          LOGGER.error(
              "Lỗi khi dừng server: {}", e.getMessage(), e);
          Thread.currentThread().interrupt();
        }
        System.out.println("Server đã tắt.");
        break;
      }
    }

    scanner.close();
  }
}
