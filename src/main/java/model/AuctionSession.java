package model;

import java.time.LocalDateTime;
import java.util.ArrayList;

import model.state.AuctionState;
import model.state.AuctionStateFactory;
import utils.IDGenerator;
public class AuctionSession {
    private Seller seller;
    private Item item;
    final private String sessionID;
    final private double startingPrice;
    final private double incrementStep;
    private double currentPrice;
    private Bidder highestBidder = null;
    final private ArrayList<Bid> bidHistory = new ArrayList<>();
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    public enum Status { PENDING, OPEN, CLOSED, SETTLED }; // một nhóm các hằng số
    private Status status;
    private AuctionState state;
    public AuctionSession(Seller seller, Item item, double startingPrice, double incrementStep, LocalDateTime startTime){
        this.seller = seller;
        this.item = item;
        this.sessionID = IDGenerator.generateSessionId(); // sinh UUID duy nhất cho mỗi phiên đấu giá
        this.startingPrice = startingPrice;
        this.incrementStep = incrementStep;
        this.startTime = startTime;
        this.status = Status.PENDING;
        this.state = AuctionStateFactory.fromStatus(this.status);
        if (this.seller != null) {
            this.seller.addCreatedAuctionSession(this); // thêm phiên đấu giá vào lịch sử của người bán
        }
    }
    public AuctionSession(Seller seller, Item item, double startingPrice){
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
    public Item getItem() { return item; }
    public double getStartingPrice() { return startingPrice; }
    public double getIncrementStep() { return incrementStep; }
    public double getCurrentPrice() { return currentPrice; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public Bidder getHighestBidder() { return highestBidder; }
    public ArrayList<Bid> getBidHistory() { return bidHistory; }
    public String getSessionID(){
         return this.sessionID;
    }
    public Status getStatus(){
        return this.status;
    }
    public AuctionState getState(){
        return this.state;
    }
    public void setState(AuctionState state) {
        this.state = state;
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
    if (highestBidder == null) {
        if (newBid.getAmount() < startingPrice) {
            System.err.println("Giá đặt phải ít nhất bằng giá khởi điểm");
            return false;
        }
    } 
    else {
    if (newBid.getBidder().getID() == highestBidder.getID()) {
        System.err.println("Bạn đã là người đặt giá cao nhất, không thể đặt giá tiếp");
        return false;
    }
    if (newBid.getAmount() <= currentPrice + incrementStep) {
        System.err.println("Giá đặt phải cao hơn giá hiện tại ít nhất một bước giá (" + incrementStep + ")");
        return false;
    }
    }
    // Thêm bid vào lịch sử
    bidHistory.add(newBid);
    // Cập nhật giá hiện tại
    this.currentPrice = newBid.getAmount();
    // Cập nhật người đặt giá cao nhất
    this.highestBidder = newBid.getBidder();
    System.out.println("Bid mới: " + newBid.getBidder().getUsername()   + " đặt " + newBid.getAmount() + " cho phiên " + sessionID);
    return true;
    }
}