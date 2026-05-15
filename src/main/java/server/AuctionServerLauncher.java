package server;

public class AuctionServerLauncher {
    public static void main(String[] args) {
        int port = 8887; // cổng WebSocket
        AuctionServer server = new AuctionServer(port);
        server.start();
        System.out.println("AuctionServer đã chạy trên cổng " + port);
        // Server sẽ chạy nền, không cần vòng lặp
    }
}