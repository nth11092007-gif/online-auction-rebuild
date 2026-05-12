package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import model.User;

public interface UserDAO {

    // =========================================================================
    // NHÓM 1: CÁC HÀM ĐỌC/GHI ĐỘC LẬP (Không bao giờ cần Transaction phức tạp)
    // =========================================================================
    boolean register(User user);
    User login(String username, String password);
    List<User> searchUsers(String keyword);
    List<User> getAllUsers(); // admin only
    int countUsersByStatus(int status);


    // =========================================================================
    // NHÓM 2: CÁC HÀM TRUY XUẤT CƠ BẢN (Cần cả 2 phiên bản)
    // =========================================================================
    // Dùng khi chỉ muốn xem thông tin User bình thường ở UI
    User getUserById(int id); 
    
    // Dùng khi đang ở giữa một Transaction (ví dụ: Service đang trừ tiền thì cần check lại User)
    User getUserById(Connection conn, int id) throws SQLException; 

    User getUserByUsername(String username);

    User getUserByUsername(Connection conn, String username) throws SQLException;

    User getUserForUpdate(Connection conn, int userId) throws SQLException; // Lấy User kèm khoá (dùng khi đang ở giữa Transaction cần đảm bảo dữ liệu không bị thay đổi bởi luồng khác)
    // =========================================================================
    // NHÓM 3: CÁC HÀM CẬP NHẬT TRẠNG THÁI / THÔNG TIN (Cần cả 2 phiên bản)
    // =========================================================================
    // Cập nhật độc lập
    boolean updateStatus(int userId, String status) throws SQLException;
    boolean updateBalance(int userId, double newBalance, double newFrozenBalance) throws SQLException;

    // Cập nhật nằm trong một chuỗi Transaction (Ví dụ: Vừa trừ tiền xong thì khóa luôn mồm user nếu phát hiện gian lận)
    boolean updateStatus(Connection conn, int userId, String status) throws SQLException;
    boolean updateBalance(Connection conn, int userId, double newBalance, double newFrozenBalance) throws SQLException;


    // =========================================================================
    // NHÓM 4: CÁC HÀM GIAO DỊCH TIỀN BẠC (Atomic)
    // (Đã có bản nhận 'conn' cho Transaction, bổ sung bản độc lập để dùng khi nạp rút tiền lẻ)
    // =========================================================================
    
    // 4.1 Đóng băng tiền (Dùng khi đặt giá)
    boolean freezeMoneyAtomic(Connection conn, int userId, double amount) throws SQLException;
    boolean freezeMoneyAtomic(int userId, double amount) throws SQLException; // Bản gọi lẹ

    // 4.2 Hoàn tiền đang đóng băng về số dư (Dùng khi có người trả giá cao hơn)
    boolean refundMoneyAtomic(Connection conn, int userId, double amount) throws SQLException;
    boolean refundMoneyAtomic(int userId, double amount) throws SQLException; // Bản gọi lẹ

    // 4.3 Trừ hẳn tiền đóng băng (Dùng khi chốt đơn thành công ở SettlementService)
    boolean deductFrozenMoneyAtomic(Connection conn, int userId, double amount) throws SQLException;
    boolean deductFrozenMoneyAtomic(int userId, double amount) throws SQLException; // Bản gọi lẹ

    // 4.4 Cộng tiền vào tài khoản (Dùng khi nạp tiền hoặc người bán nhận được tiền thanh toán)
    boolean addMoneyAtomic(Connection conn, int userId, double amount) throws SQLException;
    boolean addMoneyAtomic(int userId, double amount) throws SQLException; // Bản gọi lẹ

}
