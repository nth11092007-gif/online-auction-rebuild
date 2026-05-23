package Controller;

import dao.ItemDAO;
import dao.ItemDAOImpl;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.AuctionSession;
import model.Item;

import java.awt.image.BufferedImage;

public class ItemCardController {

    private ItemDAO itemDAO = new ItemDAOImpl();
    private AuctionSession currentSession;

    @FXML private Label lblDescribe;
    @FXML private Label lblName;
    @FXML private ImageView imgItem;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblItemID;
    @FXML private Label lblOwner;
    @FXML private Label lblType;
    @FXML private Button btnJoin;

    public void setItemData(Item item) {
        lblItemID.setText("ID: " + item.getItemID());
        lblName.setText("Tên sản phẩm: "+item.getItemName());
        lblCurrentPrice.setText("Giá hiện tại: " + item.getStartingPrice() + " VND");
        lblType.setText("Phân loại: " + item.getClass().getSimpleName());
        lblOwner.setText("Người sở hữu: " + item.getOwnerName());
        lblDescribe.setText("Mô tả: " + item.getDescription());
        if (item.getAvatar() != null) {
            Image image = SwingFXUtils.toFXImage(item.getAvatar(), null);
            if (image != null) {
                imgItem.setImage(image);
            } else {
                System.err.println("Không chuyển đổi được ảnh từ database.");
            }
        }
    }

    public void setAuctionData(AuctionSession session) {
        // FIX: Kiểm tra null ngay đầu — trước đây không có null check nên
        // nếu session null, dòng this.currentSession = session chạy rồi
        // session.getItem() ném NPE, exception thoát ra ngoài, card không được
        // add vào container mà không có thông báo lỗi rõ ràng.
        if (session == null) {
            System.err.println("setAuctionData nhận session null — bỏ qua.");
            return;
        }

        this.currentSession = session;
        System.out.println("setAuctionData OK, sessionId = " + session.getSessionID());

        try {
            Item item = session.getItem();
            if (item != null) {
                setItemData(item);
                lblCurrentPrice.setText("Giá hiện tại: " + session.getCurrentPrice() + " VND");
            } else {
                // item null — hiển thị thông tin cấp session
                lblItemID.setText("Session: " + session.getSessionID());
                lblCurrentPrice.setText("Giá hiện tại: " + session.getCurrentPrice() + " VND");
                lblType.setText("Loại: Đấu giá");
                lblOwner.setText("Người bán: "
                        + (session.getSeller() != null ? session.getSeller().getUsername() : "Unknown"));
                lblDescribe.setText(""); // không có item thì không có mô tả
                System.out.println("Cảnh báo: session " + session.getSessionID()
                        + " không có item — có thể Gson chưa map đúng field.");
                BufferedImage bImage = session.getItem().getAvatar();
                Image fxImage = SwingFXUtils.toFXImage(bImage, null);
                imgItem.setImage(fxImage);
                
            }
        } catch (Exception e) {
            System.err.println("Lỗi trong setAuctionData (session="
                    + session.getSessionID() + "): " + e.getMessage());
            e.printStackTrace();
            // currentSession đã được gán ở trên nên nút Tham gia vẫn hoạt động
        }
    }

    @FXML
    private void handleJoinAuction() {
        if (currentSession != null) {
            System.out.println("Joining auction: " + currentSession.getSessionID());
            MainApp.showAuctionDetail(currentSession);  // gọi static method
        } else {
            System.err.println("currentSession is null");
        }
    }
}