package model;

public class ProxyBid {
    private int id;
    private int userId;
    private String sessionId;  // dùng String giống các chỗ khác
    private double maxAmount;
    private boolean active;

    public ProxyBid() {}

    public ProxyBid(int userId, String sessionId, double maxAmount) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.maxAmount = maxAmount;
        this.active = true;
    }

    // getters & setters...
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public double getMaxAmount() { return maxAmount; }
    public void setMaxAmount(double maxAmount) { this.maxAmount = maxAmount; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}