// utils/SessionManager.java
package utils;
import model.User;

public class SessionManager {
    // lớp phụ dùng để lưu người dùng hiện tại đã đăng nhập vào RAM
    // có thể mở rộng để lưu thông tin khác như token, thời gian đăng nhập, v.v.
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void logout() {
        currentUser = null;
    }
}