package dto;

public class BidRequest {
    private int userId;
    private String sessionId;
    private double bidAmount;

    // Các hàm Getter (Gson có thể dùng Reflection để gán giá trị, nhưng có getter sẽ tiện cho mình dùng)
    public int getUserId() { return userId; }
    public String getSessionId() { return sessionId; }
    public double getBidAmount() { return bidAmount; }
}
