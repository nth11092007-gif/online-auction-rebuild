package model;

import java.time.LocalDateTime;
import java.util.ArrayList;

import utils.IDGenerator;

public class AuctionSession {
    private Seller seller;
    private Items item;
    private String sessionID = null;
    final private double startingPrice;
    final private double incrementStep;
    private double currentPrice;
    private Bidder highestBidder = null;
    final private ArrayList<Bid> bidHistory = new ArrayList<>();
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    public enum Status { PENDING, OPEN, CLOSED, CANCELLED }; // một nhóm các hằng số
    private Status status;
    public AuctionSession(Seller seller, Items item, double startingPrice, double incrementStep, LocalDateTime startTime){
        this.seller = seller;
        this.item = item;
        this.sessionID = IDGenerator.generateSessionId(); // sinh UUID duy nhất cho mỗi phiên đấu giá
        this.startingPrice = startingPrice;
        this.incrementStep = incrementStep;
        this.startTime = startTime;
        this.status = Status.PENDING;
        if (this.seller != null) {
            this.seller.addCreatedAuctionSession(this); // thêm phiên đấu giá vào lịch sử của người bán
        }
    }
    public AuctionSession(Seller seller, Items item, double startingPrice, double incrementStep, LocalDateTime startTime, String sessionID){
        this.seller = seller;
        this.item = item;
        this.sessionID = sessionID;
        this.startingPrice = startingPrice;
        this.incrementStep = incrementStep;
        this.startTime = startTime;
        this.status = Status.PENDING;
        if (this.seller != null) {
            this.seller.addCreatedAuctionSession(this); // thêm phiên đấu giá vào lịch sử của người bán
        }
    } 
    public AuctionSession(Seller seller, Items item, double startingPrice){
        this(seller, item, startingPrice, 0.1, LocalDateTime.now());
    }
    // Bổ sung Setter
    public void setCurrentPrice(double price) { this.currentPrice = price; }
    public void setHighestBidder(Bidder bidder) { this.highestBidder = bidder; }
    public void setStartTime(LocalDateTime time) { this.startTime = time; }
    public void setEndTime(LocalDateTime time) { this.endTime = time; }
    public void setStatus(Status status) { this.status = status; }

    // Bổ sung các Getter
    public Seller getSeller() { return seller; }
    public Items getItem() { return item; }
    public double getStartingPrice() { return startingPrice; }
    public double getIncrementStep() { return incrementStep; }
    public double getCurrentPrice() { return currentPrice; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public Bidder getHighestBidder() { return highestBidder;}
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
    public boolean addBid(Bid newBid) {
    if (newBid == null) {
        return false;
    }
    if (status != Status.OPEN) {
        System.err.println("Phiên đấu giá không còn hoạt động, không thể đặt giá");
        return false;
    }
    if (newBid.getAmount() <= currentPrice + incrementStep) {
        System.err.println("Giá đặt phải cao hơn giá hiện tại");
        return false;
    }
    // Thêm bid vào lịch sử
    bidHistory.add(newBid);
    // Cập nhật giá hiện tại
    this.currentPrice = newBid.getAmount();
    return true;
}
}