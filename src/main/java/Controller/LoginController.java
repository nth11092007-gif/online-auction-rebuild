package Controller;

import dao.UserDAO;
import dao.UserDAOImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import Exception.*;
import javafx.stage.Stage;
import model.User;
import org.slf4j.*;

import java.io.IOException;

public class LoginController {

    UserDAO login = new UserDAOImpl();
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @FXML
    private Button btnIn;

    @FXML
    private Button btnUp;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private TextField txtUser;
    @FXML
    void onhandleIn(ActionEvent event) throws UserExisted{
        String UserName = txtUser.getText();
        String Pass = txtPassword.getText();
        System.out.print(UserName+"/n"+Pass);
        try {
            if (login.getUserByUsername(UserName) != null) {
                throw new  UserExisted();
            }
            User user = login.login(UserName,Pass);
            if (user.getRole() == User.Role.USER ) {
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/HomeSeller.fxml"));
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();
                } catch ( IOException e){
                    e.printStackTrace();
                }
            } else if (user.getRole() == User.Role.ADMIN ) {
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/HomeAdmin.fxml"));
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();
                } catch ( IOException e){
                    e.printStackTrace();
                }
            }

        } catch (UserExisted u) {
            u.getMessage();
        }
    }

    @FXML
    void onhandleUp(ActionEvent event) {
        try{
            Parent root = FXMLLoader.load(getClass().getResource("/Register.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch ( IOException e){
            e.printStackTrace();
        }
    }

}
