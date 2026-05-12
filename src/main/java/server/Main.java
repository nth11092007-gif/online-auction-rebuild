package server;

public class Main {
    public static void main(String[] args) {
        // Khởi chạy server ở port 8080
        int port = 8080;
        AuctionServer server = new AuctionServer(port);
        server.start();
    }
}
