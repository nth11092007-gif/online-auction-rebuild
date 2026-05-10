// import java.nio.file.Files;
// import java.nio.file.Paths;
// import java.sql.Connection;
// import java.sql.Statement;

// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.junit.jupiter.api.Assertions.fail;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;

// import service.AuctionService;
// import utils.DBConnection;

// public class AuctionServiceIntegrationTest {

//     private AuctionService auctionService;

//     @BeforeEach
//     public void setUp() {
//         auctionService = new AuctionService();

//         // Làm sạch và setup lại Database trước MỖI bài test
//         try (Connection conn = DBConnection.getConnection();
//              Statement stmt = conn.createStatement()) {
             
//             // 1. Đọc và chạy toàn bộ file quan_ly_dau_gia.sql để reset bảng và data gốc
//             String sqlPath = "src/test/resources/quan_ly_dau_gia.sql";
//             String sqlContent = new String(Files.readAllBytes(Paths.get(sqlPath)));
//             stmt.execute(sqlContent);

//             // 2. Vì file SQL chưa có dữ liệu phiên đấu giá, ta phải tự tạo 1 phiên OPEN để test
//             // Lấy item_id = 1 (Tranh sơn dầu, giá khởi điểm 1500) của seller_minh (id=2)
//             String insertTestSession = "INSERT INTO auction_sessions " +
//                     "(session_id, owner_id, item_id, starting_price, step_price, status) " +
//                     "VALUES (1, 2, 1, 1500, 100, 'OPEN')";
//             stmt.executeUpdate(insertTestSession);
            
//             System.out.println("🔧 Đã reset Database và tạo Phiên đấu giá mẫu thành công!");

//         } catch (Exception e) {
//             fail("Lỗi Setup Database: Không thể đọc file SQL hoặc kết nối DB. " + e.getMessage());
//         }
//     }

//     @Test
//     @DisplayName("Test đặt giá thành công với Database thật")
//     public void testPlaceBidWithRealDatabase() {
//         System.out.println("Bắt đầu test đặt giá...");

//         // Dữ liệu lấy chuẩn theo bảng users trong quan_ly_dau_gia.sql
//         int realBidderId = 3;              // ID 3 là buyer_an, đang có 5000 tiền
//         String realSessionId = "1"; // ID phiên ta vừa tạo ở setup()
//         double bidAmount = 2000.0;         // Giá đặt (lớn hơn giá khởi điểm 1500)

//         try {
//             // Chạy thẳng vào hệ thống thật
//             boolean isSuccess = auctionService.placeBid(realBidderId, realSessionId, bidAmount);
            
//             // Dùng JUnit để chốt kết quả: Kỳ vọng là TRUE
//             assertTrue(isSuccess, "Lỗi: Đặt giá thất bại. Code Service đang có vấn đề!");

//             System.out.println("✅ Đặt giá THÀNH CÔNG! Nếu mở MySQL kiểm tra, bạn sẽ thấy:");
//             System.out.println(" - Bảng 'users': Tiền của buyer_an (ID 3) đã bị trừ/đóng băng.");
//             System.out.println(" - Bảng 'bids': Đã có dòng lịch sử đặt giá 2000.0 cho SS_TEST_01.");
            
//         } catch (Exception e) {
//             e.printStackTrace();
//             fail("Test bị sập do Exception: " + e.getMessage());
//         }
//     }
// }