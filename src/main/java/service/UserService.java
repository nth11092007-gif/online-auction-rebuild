package service;

import dao.UserDAOImpl;
import model.User;

/**
 * UserService - Tầng xử lý nghiệp vụ liên quan đến người dùng.
 * Trung gian giữa Server và DAO.
 */
public class UserService {
    private final UserDAOImpl userDao;

    public UserService() {
        this.userDao = new UserDAOImpl();
    }

    /**
     * Lấy thông tin chi tiết người dùng
     */
    public User getUserById(int userId) {
        return userDao.getUserById(userId);
    }
}