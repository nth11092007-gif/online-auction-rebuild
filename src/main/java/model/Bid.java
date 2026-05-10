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
        
        public double getAmount() { return amount; }
        public Bidder getBidder() { return bidder; }
        public LocalDateTime getTime() { return time; }

        // Thêm setTime để gán thời gian từ DB
        public void setTime(LocalDateTime time) {
            this.time = time;
        }
}