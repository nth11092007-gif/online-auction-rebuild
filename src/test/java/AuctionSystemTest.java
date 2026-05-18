import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dao.AuctionSessionDAO;
import dao.AuctionSessionDAOImpl;
import dao.BidDAO;
import dao.BidDAOImpl;
import dao.ItemDAO;
import dao.ItemDAOImpl;
import dao.UserDAO;
import dao.UserDAOImpl;
import factory.TypeArts;
import model.AuctionSession;
import model.Bid;
import model.Item;
import model.ItemsAttributes;
import model.User;
import utils.DBConnection;

public class AuctionSystemTest {

    // Khai báo các DAO ở cấp độ class để dùng chung
    final private UserDAO userDAO = new UserDAOImpl();
    final private ItemDAO itemDAO = new ItemDAOImpl();
    final private AuctionSessionDAO sessionDAO = new AuctionSessionDAOImpl();
    final private BidDAO bidDAO = new BidDAOImpl();
    @Test
    @DisplayName("Test luồng: Đăng nhập -> Tạo Hàng -> Mở Phiên -> Đặt giá")
    void testFullAuctionFlow() {
        // ==========================================
        // BƯỚC 1: ĐĂNG NHẬP NGƯỜI BÁN
        // ==========================================
        User seller = userDAO.login("admin01", "123456");
        
        // Thay thế if-else bằng Assertions. 
        // Nếu seller là null, Test sẽ đỏ ngay lập tức và in ra thông báo.
        assertNotNull(seller, "❌ Lỗi: Không tìm thấy tài khoản admin.");
        assertEquals("admin01", seller.getUsername(), "Tên đăng nhập không khớp.");


        ItemsAttributes artAttr = new ItemsAttributes.Builder(seller, 3000.0)
        // Nối chuỗi các thuộc tính tùy chọn
        .description("Bức tranh Đêm Đầy Sao (Bản sao siêu cấp)")
        .artistName("Vincent van Gogh")
        .releaseDate(LocalDate.of(1889, 6, 1))
        // Gọi lệnh build() ở cuối cùng để khóa (đóng gói) object lại
        .build();
        
        Item artItem = new TypeArts().createItems(artAttr);
        itemDAO.addItem(artItem);
        // Giả sử sau khi thêm vào DB, ID phải được tạo (khác null hoặc > 0)
        assertNotNull(artItem.getItemID(), "❌ Lỗi: ItemID của bức tranh chưa được tạo.");


        // ==========================================
        // BƯỚC 3: TẠO PHIÊN ĐẤU GIÁ CHO BỨC TRANH
        // ==========================================
        AuctionSession session = new AuctionSession(
                seller,
                artItem,
                artItem.getStartingPrice() // Giá khởi điểm 3000
        );
        
        boolean isSessionCreated = sessionDAO.createSession(session, artItem.getItemID());
        assertTrue(isSessionCreated, "Lỗi: Không thể tạo phiên đấu giá trong DB.");
        
        // Lấy đúng ID vừa được IDGenerator tự động sinh ra trong Constructor
        String actualSessionId = session.getSessionID();
        assertNotNull(actualSessionId, "Lỗi: SessionID chưa được sinh ra.");


        // ==========================================
        // BƯỚC 4: NGƯỜI MUA ĐẶT GIÁ (BID)
        // ==========================================
        User buyer = userDAO.login("buyer_an", "123456");
        assertNotNull(buyer, "Lỗi: Không tìm thấy tài khoản người mua.");

        // Người mua đặt giá 5000 (Bằng giá khởi điểm 3000)
        Bid newBid = new Bid(buyer, 5000.0);
        
        // Dùng actualSessionId của phiên vừa tạo, thay vì chuỗi tự gõ
        try {
            boolean isBidSuccess = bidDAO.addBid(DBConnection.getConnection(), actualSessionId, newBid);
            assertTrue(isBidSuccess, "Đặt giá phải thành công!");
            assertTrue(isBidSuccess, "❌ Lỗi: Người mua đặt giá thất bại.");
            assertEquals(5000.0, newBid.getAmount(), "Số tiền đặt giá không khớp.");
        } catch (SQLException e) {
    // Nếu có lỗi SQL, đoạn này sẽ bắt lại và in ra lỗi
        e.printStackTrace();
    fail("Lỗi SQL phát sinh: " + e.getMessage());
        }   
    }
}