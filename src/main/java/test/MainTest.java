package test;

import dao.UserDAO;
import dao.UserDAOImpl;
import java.io.File;
import java.time.LocalDate;
import model.User;

/**
 * MainTest - simple test harness for DAO operations.
 */
public class MainTest {
  /** Entry point for manual DAO testing. */
  public static void main(String[] args) {
    UserDAO testUserDao = new UserDAOImpl();
    User user = testUserDao.getUserByUsername("buyer_an");
    System.out.print(user.getUsername());
  }
}
