package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class AuctionDetailController {

    @FXML
    private VBox Container;

    @FXML
    private Spinner<Integer> bidSpinner;

    @FXML
    private Label txtCurrentPrice;

    @FXML
    private Label txtDescription;

    @FXML
    private Label txtItemID;

    @FXML
    private Label txtItemName;
    private int currentBidValue = 25000000; //cần lấy gtri
    private int stepValue = 200000;
    @FXML
    public void initialize() {
        // Khởi tạo giới hạn cho Spinner: Min, Max, Giá trị khởi tạo, Bước nhảy
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        currentBidValue + stepValue,
                        Integer.MAX_VALUE,
                        currentBidValue + stepValue,
                        stepValue
                );
        bidSpinner.setValueFactory(valueFactory);
        //Cần thêm phương thức nhận gtri từ màn hình khác
    }
    @FXML
    void handleQuickBid(ActionEvent event) {
        Button btn = (Button) event.getSource();
        int amountToAdd = 0;

        switch (btn.getText()) {
            case "+0.5M": amountToAdd = 500000; break;
            case "+1.0M": amountToAdd = 1000000; break;
            case "+2.5M": amountToAdd = 2500000; break;
        }

        // Cập nhật giá trị lên Spinner
        int newBid = bidSpinner.getValue() + amountToAdd;
        bidSpinner.getValueFactory().setValue(newBid);
    }

    @FXML
    void HandleBid(ActionEvent event) {

    }

    @FXML
    void HandleGoBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HomeSeller.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
