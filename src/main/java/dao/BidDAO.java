package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import model.Bid;

public interface BidDAO {
    // Lưu lượt đặt giá mới vào database
    boolean addBid(String sessionId, Bid bid);
    boolean addBid(Connection conn, String sessionId, Bid bid) throws SQLException;

    // Lấy danh sách lịch sử đặt giá của một phiên cụ thể, sắp xếp từ cao xuống thấp
    List<Bid> getBidsBySession(String sessionId);
    List<Bid> getBidsBySession(Connection conn, String sessionId) throws SQLException;

    Bid getHighestBid(Connection conn, String sessionId) throws SQLException;
    Bid getHighestBid(String sessionId);
}