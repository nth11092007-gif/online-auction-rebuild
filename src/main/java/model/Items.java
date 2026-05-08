package model;
//Lớp trừu tượng đại diện cho một món hàng cơ bản trong hệ thống đấu giá.

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public abstract class Items {
    //Tích hợp tự động sinh UUID hoặc lấy ID từ Database sau.
    protected int itemID;
    protected String owner;
    protected double startingPrice;
    protected String description;
    protected BufferedImage avatar;
    private static Logger logger = LoggerFactory.getLogger(Items.class);

    public Items(int itemID, String owner, double startingPrice, String description) {
        this.itemID = itemID;
        this.owner = owner;
        this.startingPrice = startingPrice;
        this.description = description;
        try {
            this.avatar = ImageIO.read(new File("src\\main\\resources\\Images\\BaseItem.png"));
        } catch (IOException e) {
            logger.error("Không tìm thấy ảnh nguồn");
        }
    }

    public void setItemID(int itemID) { this.itemID = itemID; }
    public void setAvatar(BufferedImage avatar) {this.avatar = avatar;}
    public void setAvatar(String filePath) {
        try {
            BufferedImage newImage = ImageIO.read(new File(filePath));
        } catch (IOException e) {
            logger.error("Ảnh không hợp lệ!");
        }
    }
    public int getItemID() { return itemID; }
    public String getOwner() {
        return owner;
    }
    public double getStartingPrice() {
        return startingPrice;
    }
    public String getDescription(){
        return description;
    }
    public BufferedImage getAvatar() {
        return avatar;
    }

    public abstract String showInfo();
}
