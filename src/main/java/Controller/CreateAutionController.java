package Controller;

import dao.AuctionSessionDAO;
import dao.AuctionSessionDAOImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class CreateAutionController {
    AuctionSessionDAO auctionSessionDAO = new AuctionSessionDAOImpl();
    @FXML
    private Button btnClearForm;

    @FXML
    private Button btnCreateAution;

    @FXML
    private ComboBox<String> cbItemType;

    @FXML
    private TextArea txtDescription;

    @FXML
    private TextField txtIncrementStep;

    @FXML
    private TextField txtItemName;

    @FXML
    private TextField txtOpenDays;

    @FXML
    private TextField txtStartingPrice;

    @FXML
    void HandleClearForm(ActionEvent event) {
        txtItemName.clear();
        txtStartingPrice.clear();
        txtIncrementStep.clear();
        txtOpenDays.clear();
        txtDescription.clear();
        cbItemType.getSelectionModel().clearSelection();
    }

    @FXML
    void HandleCreatAution(ActionEvent event) {
        String itemName = txtItemName.getText();
        double startPrice = Double.parseDouble(txtStartingPrice.getText());
        double stepPrice = Double.parseDouble(txtIncrementStep.getText());
        int openDays = Integer.parseInt(txtOpenDays.getText());
        String description = txtDescription.getText();
    }
    @FXML
    public void initialize() {
        // Thêm các loại vật phẩm vào ComboBox
        cbItemType.getItems().addAll("Electronics", "Arts", "Vehicles");
    }

}
