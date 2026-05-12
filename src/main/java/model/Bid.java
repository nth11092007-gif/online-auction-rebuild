package model;

import java.time.LocalDateTime;

public class Bid {
        final private Bidder bidder;
        final private double amount;
        private LocalDateTime time;

        public Bid(Bidder bidder, double amount) {
            this.bidder = bidder;
            this.amount = amount;
            this.time = LocalDateTime.now();
        }

        // Constructor đầy đủ cho DAO khi lấy từ Database
        public Bid(User bidder, double amount, LocalDateTime time) {
            this.bidder = bidder;
            this.amount = amount;
            this.time = time;
        }
        
        public double getAmount() { return amount; }
        public Bidder getBidder() { return bidder; }
        public LocalDateTime getTime() { return time; }
}