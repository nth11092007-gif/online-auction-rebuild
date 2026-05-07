package Controller;

import dao.UserDAO;
import dao.UserDAOImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;

import java.io.IOException;

public class RegisterController {
    UserDAO register = new UserDAOImpl();
    @FXML
    private Button btnRegister;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private TextField txtPhoneNumber;

    @FXML
    private TextField txtRealName;

    @FXML
    private TextField txtUser;

    private void showAlert(String content, Alert.AlertType type){
        Alert alert = new Alert(type);
        alert.setTitle("Thông báo");
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void switchToHome (ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Home.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onhandleRegister(ActionEvent event) {
        String username = txtUser.getText().trim();
        String password = txtPassword.getText().trim();
        String realname = txtRealName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhoneNumber.getText().trim();

        //Ktra ko dc bỏ trống
        if (username.isEmpty() || password.isEmpty() || realname.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showAlert("vui lòng điền đầy đủ thông tin", Alert.AlertType.ERROR);
            return;
        }

        User newUser = new User(realname, username, email, password, phone);
        boolean succes = register.register(newUser);

        //Thông báo thành công
        if (succes) {
            showAlert("Đăng ký thành công", Alert.AlertType.INFORMATION);
            switchToHome(event);
        } else {
            showAlert("Tài khoản đã tồn tại", Alert.AlertType.ERROR);
        }
    }
}
