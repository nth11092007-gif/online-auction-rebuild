package model;
//Lớp trừu tượng đại diện cho một món hàng cơ bản trong hệ thống đấu giá.

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public abstract class Item {
    //Tích hợp tự động sinh UUID hoặc lấy ID từ Database sau.
    protected int itemID;
    protected String ownerName;
    protected double startingPrice;
    protected String description;
    protected BufferedImage avatar;
    private static Logger logger = LoggerFactory.getLogger(Items.class);


    public Item(int itemID, String ownerName, double startingPrice, String description) {
        this.itemID = itemID;
        this.ownerName = ownerName;
        this.startingPrice = startingPrice;
        this.description = description;
        try {
            InputStream inputStream = getClass().getResourceAsStream("/Images/BaseItem.png");

            if (inputStream == null) {
                logger.error("Không tìm thấy ảnh tại đường dẫn chỉ định!");
            } else {
                BufferedImage image = ImageIO.read(inputStream);
                logger.info("Đọc ảnh thành công!");
                this.avatar = image;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setItemID(int itemID) { this.itemID = itemID; }
    public void setAvatar(BufferedImage avatar) {this.avatar = avatar;}
    public void setAvatar(String filePath) {
        try {
            InputStream inputStream = getClass().getResourceAsStream(filePath);

            if (inputStream == null) {
                logger.error("Không tìm thấy ảnh tại đường dẫn chỉ định!");
            } else {
                BufferedImage image = ImageIO.read(inputStream);
                logger.info("Đọc ảnh thành công!");
                this.avatar = image;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public int getItemID() { return itemID; }
    public String getOwnerName() {
        return ownerName;
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

    public String getItemType() {
        return this.getClass().getSimpleName();
    }
}