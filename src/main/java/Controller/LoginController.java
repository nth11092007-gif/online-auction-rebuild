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

    public static String checkPasswordStrength(String password) {
        // Kiểm tra đầu vào rỗng hoặc null
        if (password == null || password.trim().isEmpty()) {
            return "Trống";
        }

        int score = 0;

        // 1. Kiểm tra độ dài (ít nhất 8 ký tự)
        if (password.length() >= 8) {
            score++;
        }

        // 2. Kiểm tra có chứa ít nhất một chữ cái viết hoa (A-Z)
        if (password.matches(".*[A-Z].*")) {
            score++;
        }

        // 3. Kiểm tra có chứa ít nhất một chữ cái viết thường (a-z)
        if (password.matches(".*[a-z].*")) {
            score++;
        }

        // 4. Kiểm tra có chứa ít nhất một chữ số (0-9)
        if (password.matches(".*\\d.*")) {
            score++;
        }

        // 5. Kiểm tra có chứa ít nhất một ký tự đặc biệt
        // Bạn có thể thêm hoặc bớt các ký tự đặc biệt trong biểu thức chính quy này
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            score++;
        }

        // Đánh giá độ khó dựa trên tổng điểm (tối đa 5 điểm)
        switch (score) {
            case 0:
            case 1:
            case 2:
                return "Yếu"; // Chỉ đạt 1-2 tiêu chí (thường là quá ngắn hoặc chỉ có chữ/số)
            case 3:
                return "Trung bình"; // Đạt 3 tiêu chí
            case 4:
                return "Mạnh"; // Đạt 4 tiêu chí
            case 5:
                return "Rất mạnh"; // Đạt đủ 5 tiêu chí
            default:
                return "Không xác định";
        }
    }

    @FXML
    void onhandleIn(ActionEvent event) throws UserExisted, PasswordStrengthCheck {
        String UserName = txtUser.getText();
        String Pass = txtPassword.getText();
        System.out.print(UserName+"/n"+Pass);
        try {
            if (login.getUserByUsername(UserName) != null) {
                throw new  UserExisted();
            }
            switch (checkPasswordStrength(Pass)) {
                case "Yếu":
                    throw new PasswordStrengthCheck();
                case "Trung bình":
                    System.out.println("mk trung bình");
                case  "Rất mạnh":
                    System.out.println("ok");
            }
            User user = login.login(UserName,Pass);
            if (user.getRole() == User.Role.USER ) {
                try{
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
                    Parent root = loader.load();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch ( IOException e){
            e.printStackTrace();
        }
    }

}
