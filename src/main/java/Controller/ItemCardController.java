package Controller;

import dao.ItemDAO;
import dao.ItemDAOImpl;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.Items;

public class ItemCardController{
    private ItemDAO itemDAO = new ItemDAOImpl();
    @FXML
    private Label lblDescribe;

    @FXML
    private ImageView imgItem;

    @FXML
    private Label lblCurrentPrice;

    @FXML
    private Label lblItemID;

    @FXML
    private Label lblOwner;

    @FXML
    private Label lblType;

    public void setItemData(Items item) {
        lblItemID.setText("ID: " + item.getItemID());
        lblCurrentPrice.setText("Giá hiện tại: " + item.getStartingPrice() + " VND");
        lblType.setText("Phân loại: " + item.getClass().getSimpleName());
        lblOwner.setText("Người sở hữu: " + item.getOwnerName());
        lblDescribe.setText("Mô tả: " + item.getDescription());
        if (item.getAvatar() != null) {
            Image image = SwingFXUtils.toFXImage(item.getAvatar(), null);
            if (image == null) {
                System.out.print("Oops something went wrong with database");
            }
            imgItem.setImage(image);
        }
    }
}
