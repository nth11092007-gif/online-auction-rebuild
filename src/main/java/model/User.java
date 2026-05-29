package model;

import java.util.ArrayList;
import java.util.List;

public class User implements Bidder, Seller {
    private String realName;
    private String username;
    private int id;
    private String email;
    private Role role;
    private String password;
    private String phoneNumber;
    private double balance = 0;
    private double frozenBalance = 0;
    private List<AuctionSession> myCreatedAuctions;
    private List<AuctionSession> myJoinedAuctions;

    public enum Role { USER, ADMIN }

    public User() {}

    public User(String realName, String username, String email, String password, String phoneNumber) {
        this.role = Role.USER;
        this.realName = realName;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.myCreatedAuctions = new ArrayList<>();
        this.myJoinedAuctions = new ArrayList<>();
    }

    // Constructor đầy đủ cho DAO khi lấy từ Database
    public User(int id, String username, String password, String realName, String email,
                String phoneNumber, Role role, double balance, double frozenBalance) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.balance = balance;
        this.frozenBalance = frozenBalance;
    }

    // ---------------- Các getter cơ bản (không thuộc interface) ----------------
    public String getRealName() { return realName; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public Role getRole() { return role; }

    // ---------------- Các phương thức của Buyer ----------------
    @Override
    public int getID() { return id; }

    @Override
    public String getUsername() { return username; }

    @Override
    public double getBalance() { return balance; }

    @Override
    public double getFrozenBalance() { return frozenBalance; }

    @Override
    public List<AuctionSession> getJoinedAuctionSessions() {
        if (myJoinedAuctions == null) {
            myJoinedAuctions = new ArrayList<>();
        }
        return myJoinedAuctions;
    }

    @Override
    public void addJoinedAuctionSession(AuctionSession session) {
        if (myJoinedAuctions == null) {
            myJoinedAuctions = new ArrayList<>();
        }
        myJoinedAuctions.add(session);
    }

    // ---------------- Các phương thức của Seller ----------------
    @Override
    public List<AuctionSession> getCreatedAuctionSessions() {
        if (myCreatedAuctions == null) {
            myCreatedAuctions = new ArrayList<>();
        }
        return myCreatedAuctions;
    }

    @Override
    public void addCreatedAuctionSession(AuctionSession session) {
        if (myCreatedAuctions == null) {
            myCreatedAuctions = new ArrayList<>();
        }
        myCreatedAuctions.add(session);
    }

    // ---------------- Các phương thức chỉ có ở User ----------------
    public void deposit(double amount) {
        balance += amount;
    }

    public void withdraw(double amount) {
        balance -= amount;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setFrozenBalance(double frozenBalance) {
        this.frozenBalance = frozenBalance;
    }

    public void setID(int id) {
        this.id = id;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}