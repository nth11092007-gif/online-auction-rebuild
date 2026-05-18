package model;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Logger logger = LoggerFactory.getLogger(AuctionSession.class);
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
    public AuctionSession(Seller seller, Item item, double startingPrice, double incrementStep, LocalDateTime startTime, String sessionID){
        this.seller = seller;
        this.item = item;
        this.sessionID = sessionID;
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
        this.setOpen();
        this.currentPrice = startingPrice; 
        this.startTime = LocalDateTime.now(); // Lấy giờ bấm nút
        this.endTime = this.startTime.plusDays(openDays); // Tính giờ đóng cửa
        logger.info("Phiên đấu giá {} bắt đầu lúc {} và sẽ kết thúc lúc {}", sessionID, startTime, endTime);
        logger.info("Giá khởi điểm: {}, Bước giá: {}", startingPrice, incrementStep);
    }
    public void endSession() {
        this.setClose();
        this.endTime = LocalDateTime.now();

        logger.info("Phiên đấu giá {} đã kết thúc lúc {}", sessionID, endTime);
        if (highestBidder != null) {
            logger.info("Người chiến thắng: {} với giá {}", highestBidder.getUsername(), currentPrice);
            // Gợi ý cho sau này: Tại đây bạn có thể gọi Transaction để trừ tiền người thắng
            // và cộng tiền cho seller.
        } else {
            logger.info("Không có ai tham gia trả giá. Vật phẩm chưa được bán!");
        }
    }
    public boolean addBid(Bid newBid) {
        return this.state.addBid(this, newBid);
    }
    public boolean joinable() {
        return this.state.canJoin();
    }
    public boolean setOpen() {
        return this.state.open(this);
    }
    public boolean setClose() {
        return this.state.close(this);
    }
    public boolean settle() {
        return this.state.settle(this);
    }
}