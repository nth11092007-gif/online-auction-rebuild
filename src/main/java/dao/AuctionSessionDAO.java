package dao;

import java.sql.Connection;
import java.sql.SQLException;
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

    // 3. Cập nhật trạng thái phiên
    boolean updateSessionStatus(String sessionId, AuctionSession.Status status);
}
