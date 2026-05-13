package test;

import dao.*;
import factory.*;
import model.*;

import java.io.File;
import java.time.LocalDate;

public class MainTest {
    public static void main(String[] args) {
        UserDAO testUserDAO = new UserDAOImpl();
        User user = testUserDAO.getUserByUsername("buyer_an");
        System.out.print(user.getUsername());
    }
}