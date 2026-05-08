package test;

import dao.*;
import factory.*;
import model.*;

import java.io.File;
import java.time.LocalDate;

public class MainTest {
    public static void main(String[] args) {
       Items testing = new Vehicles(123,"ô",0.0,"","",2,"1223");
       testing.setAvatar("src/main/resources/Images/logo.png");
       System.out.print(testing.getAvatar());
    }
}