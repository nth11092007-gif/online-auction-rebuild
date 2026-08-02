package com.auction.server.service;

import com.auction.server.dao.UserDAO;
import com.auction.server.dao.UserDAOImpl;
import com.auction.common.model.User;

/**
 * UserService - Tầng xử lý nghiệp vụ liên quan đến người dùng.
 * Trung gian giữa Server và DAO.
 */
public class UserService {

  private final UserDAO userDao;

  public UserService(UserDAO userDao) {
    this.userDao = userDao;
  }

  public UserService() {
    this.userDao = new UserDAOImpl();
  }

  /**
   * Lấy thông tin chi tiết người dùng.
   */
  public User getUserById(int userId) {
    return userDao.getUserById(userId);
  }

  public User getUserByUsername(String username) {
    return userDao.getUserByUsername(username);
  }

  public UserDAO getUserDao() {
    return userDao;
  }
}

