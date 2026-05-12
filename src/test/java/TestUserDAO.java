import dao.UserDAO;
import dao.UserDAOImpl;
import model.User;

public class TestUserDAO {
    public static void main(String[] args) {
        // Khởi tạo đối tượng DAO
        UserDAO userDAO = new UserDAOImpl();

        System.out.println("=== BẮT ĐẦU TEST UserDAO ===");

        // ----------------------------------------------------
        // Kịch bản 1: Đăng nhập đúng tài khoản Admin
        // ----------------------------------------------------
        System.out.println("\n[Test 1]: Đăng nhập với tài khoản hợp lệ");
        User admin = userDAO.login("admin_super", "123456");
        
        if (admin != null) {
            System.out.println("-> Đăng nhập THÀNH CÔNG!");
            System.out.println("-> Tên hiển thị: " + admin.getUsername());
            System.out.println("-> Quyền hạn: " + admin.getRole());
            System.out.println("-> Số dư ví: " + admin.getBalance() + " VNĐ");
        } else {
            System.out.println("-> LỖI: Không tìm thấy tài khoản!");
        }

        // ----------------------------------------------------
        // Kịch bản 2: Đăng nhập sai mật khẩu (Bị chặn)
        // ----------------------------------------------------
        System.out.println("\n[Test 2]: Đăng nhập sai mật khẩu");
        User hacker = userDAO.login("admin_super", "wrongpass123");
        
        if (hacker == null) {
            System.out.println("-> Hợp lý: Hệ thống ĐÃ CHẶN đăng nhập!");
        } else {
            System.out.println("-> LỖI BẢO MẬT: Nhập sai pass vẫn vào được?");
        }
        System.out.println("\n[Test 3]: Nạp thêm tiền cho Admin");
        // Giả sử nạp thêm 500k vào ví, tiền cọc giữ nguyên
        try {
            boolean napTien = userDAO.updateBalance(admin.getID(), admin.getBalance() + 500000, admin.getFrozenBalance());
            if (napTien) {
                System.out.println("-> Cập nhật số dư THÀNH CÔNG!");
            } else {
                System.out.println("-> LỖI: Không thể cập nhật tiền.");
            }
        } catch (Exception e) {
            System.out.println("-> LỖI: Exception khi cập nhật tiền - " + e.getMessage());
        }
    }
}