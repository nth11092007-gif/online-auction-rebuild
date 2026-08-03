package com.auction.common.model;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class representing a basic item in the auction system.
 * Subclasses provide specific item types such as Art, Electronics, and Vehicles.
 */
public abstract class Item {

  private int itemId;

  private String itemName;

  private String ownerName;

  private double startingPrice;

  private String description;

  private BufferedImage avatar;

  private static final Logger LOGGER =
      LoggerFactory.getLogger(Item.class);

  /**
   * Constructs an Item with the specified attributes.
   *
   * @param itemId the unique item identifier
   * @param ownerName the name of the item owner
   * @param startingPrice the starting auction price
   * @param description the item description
   * @param itemName the display name of the item
   */
  public Item(int itemId, String ownerName, double startingPrice,
      String description, String itemName) {
    this.itemId = itemId;
    this.itemName = itemName;
    this.ownerName = ownerName;
    this.startingPrice = startingPrice;
    this.description = description;
    try {
      InputStream inputStream =
          getClass().getResourceAsStream("/Images/BaseItem.png");
      if (inputStream == null) {
        LOGGER.error(
            "Không tìm thấy ảnh tại "
            + "ường dẫn chỉ định!");
      } else {
        BufferedImage image = ImageIO.read(inputStream);
        LOGGER.info("Đọc ảnh thành công!");
        this.avatar = image;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void setItemId(int itemId) {
    this.itemId = itemId;
  }

  public void setAvatar(BufferedImage avatar) {
    this.avatar = avatar;
  }

  /**
   * Sets the item avatar image from a resource file path.
   *
   * @param filePath the classpath resource path to the image file
   */
  public void setAvatar(String filePath) {
    try {
      InputStream inputStream =
          getClass().getResourceAsStream(filePath);
      if (inputStream == null) {
        LOGGER.error(
            "Không tìm thấy ảnh tại "
            + "ường dẫn chỉ định!");
      } else {
        BufferedImage image = ImageIO.read(inputStream);
        LOGGER.info("Đọc ảnh thành công!");
        this.avatar = image;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public int getItemId() {
    return itemId;
  }

  public String getItemName() {
    return itemName;
  }

  public String getOwnerName() {
    return ownerName;
  }

  public double getStartingPrice() {
    return startingPrice;
  }

  public String getDescription() {
    return description;
  }

  public BufferedImage getAvatar() {
    return avatar;
  }

  /** Returns a formatted string with item details. */
  public abstract String showInfo();

  public String getItemType() {
    return this.getClass().getSimpleName();
  }
}
