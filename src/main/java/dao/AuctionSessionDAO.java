package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import model.AuctionSession;

public interface AuctionSessionDAO {
    // =========================================================================
    // 1. TẠO PHIÊN ĐẤU GIÁ MỚI
    // =========================================================================
    boolean createSession(Connection conn, AuctionSession session, int itemId) throws SQLException;
    boolean createSession(AuctionSession session, int itemId); // Bản gọi lẹ

    // =========================================================================
    // 2. LẤY THÔNG TIN PHIÊN ĐẤU GIÁ (Kèm người bán và sản phẩm)
    // =========================================================================
    AuctionSession getSessionById(Connection conn, String sessionId) throws SQLException;
    AuctionSession getSessionById(String sessionId); // Bản gọi lẹ dùng cho UI
    // LẤY THÔNG TIN MỌI PHIÊN ĐẤU GIÁ
    List<AuctionSession> getAllSessions(Connection conn) throws SQLException;
    List<AuctionSession> getAllSessions();

    List<AuctionSession> getSessionsStartBefore(Connection conn, LocalDateTime time, AuctionSession.Status status) throws SQLException;
    List<AuctionSession> getSessionsStartBefore(LocalDateTime time, AuctionSession.Status status);

    List<AuctionSession> getSessionsEndBefore(Connection conn, LocalDateTime time, AuctionSession.Status status) throws SQLException;
    List<AuctionSession> getSessionsEndBefore(LocalDateTime time, AuctionSession.Status status);
    // =========================================================================
    // 3. CẬP NHẬT TRẠNG THÁI PHIÊN (Ví dụ: Chuyển từ OPEN sang CLOSED)
    // =========================================================================
    boolean updateSessionStatusAtomic(Connection conn, String sessionId, AuctionSession.Status status) throws SQLException;
    boolean updateSessionStatusAtomic(String sessionId, AuctionSession.Status status); // Bản gọi lẹ

    // Thêm hàm này để lưu thời gian mới vào DB khi Anti-sniping kích hoạt
    boolean updateEndTime(Connection conn, String sessionId, Timestamp newEndTime) throws SQLException;
}