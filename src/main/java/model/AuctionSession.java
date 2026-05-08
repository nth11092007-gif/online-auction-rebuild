package model;

import java.time.LocalDateTime;
import java.util.ArrayList;

import utils.IDGenerator;

public class AuctionSession {
    private User seller;
    private Items item;
    final private String sessionID;
    final private double startingPrice;
    final private double incrementStep;
    private double currentPrice;
    private User highestBidder = null;
    final private ArrayList<Bid> bidHistory = new ArrayList<>();
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    public enum Status { PENDING, OPEN, CLOSED, CANCELLED }; // một nhóm các hằng số
    public Status status;
    public AuctionSession(User seller, Items item, double startingPrice, double incrementStep, LocalDateTime startTime){
        this.seller = seller;
        this.item = item;
        this.sessionID = IDGenerator.generateSessionId(); // sinh UUID duy nhất cho mỗi phiên đấu giá
        this.startingPrice = startingPrice;
        this.incrementStep = incrementStep;
        this.startTime = startTime;
        this.status = Status.PENDING;
        if (this.seller != null) {
            this.seller.addCreatedSessions(this); // thêm phiên đấu giá vào lịch sử của người bán
        }
    }
    public AuctionSession(User seller, Items item, double startingPrice){
        this(seller, item, startingPrice, 0.1, LocalDateTime.now());
    }
    // Bổ sung Setter
    public void setCurrentPrice(double price) { this.currentPrice = price; }
    public void setHighestBidder(User user) { this.highestBidder = user; }
    public void setStartTime(LocalDateTime time) { this.startTime = time; }
    public void setEndTime(LocalDateTime time) { this.endTime = time; }

    // Bổ sung các Getter
    public User getSeller() { return seller; }
    public Items getItem() { return item; }
    public double getStartingPrice() { return startingPrice; }
    public double getIncrementStep() { return incrementStep; }
    public double getCurrentPrice() { return currentPrice; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getSessionID(){
         return this.sessionID;
    }
    public Status getStatus(){
        return this.status;
    }
    public void startSession(int openDays) {
        this.status = Status.OPEN;
        this.currentPrice = startingPrice; 
        this.startTime = LocalDateTime.now(); // Lấy giờ bấm nút
        this.endTime = this.startTime.plusDays(openDays); // Tính giờ đóng cửa
        System.out.println("Phiên đấu giá đã CHÍNH THỨC BẮT ĐẦU! Giá khởi điểm: " + startingPrice);
        System.out.println("Kết thúc vào: " + this.endTime);
    }
    public void endSession() {
        this.status = Status.CLOSED;
        this.endTime = LocalDateTime.now();

        System.out.println("\n=== PHIÊN ĐẤU GIÁ KẾT THÚC ===");
        if (highestBidder != null) {
            System.out.println("Người chiến thắng: " + highestBidder.getUsername() + " với giá " + currentPrice);
            // Gợi ý cho sau này: Tại đây bạn có thể gọi Transaction để trừ tiền người thắng
            // và cộng tiền cho seller.
        } else {
            System.out.println("Không có ai tham gia trả giá. Vật phẩm chưa được bán!");
        }
    }
}