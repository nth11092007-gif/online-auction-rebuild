// utils/SessionManager.java

package com.auction.client.utils;

import com.auction.server.dao.UserDAO;
import com.auction.server.dao.UserDAOImpl;
import com.auction.common.model.User;

/** SessionManager - manages the currently logged-in user session in memory. */
public final class SessionManager {

  private static final UserDAO USER_DAO = new UserDAOImpl();

  private SessionManager() {
    // Hide utility class constructor
  }

  // lớp phụ dùng để lưu người dùng hiện tại đã đăng nhập vào RAM
  // có thể mở rộng để lưu thông tin khác như token,
  // thời gian đăng nhập, v.v.
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

  /**
   * Đồng bộ balance / frozen_balance từ DB
   * vào user đang đăng nhập.
   */
  public static boolean refreshCurrentUserFromDb() {
    if (currentUser == null) {
      return false;
    }
    User fresh = USER_DAO.getUserById(currentUser.getId());
    if (fresh == null) {
      return false;
    }
    currentUser = fresh;
    return true;
  }
}





