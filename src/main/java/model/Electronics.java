package model;

/**
 * Represents an electronic item in the auction system.
 */
public class Electronics extends Item {

  private final int warranty;

  private final String brand;

  /**
   * Constructs an Electronics item with all attributes.
   *
   * @param itemId the unique item identifier
   * @param itemName the display name
   * @param ownerName the owner's name
   * @param startingPrice the starting auction price
   * @param description the item description
   * @param warranty the warranty period in months
   * @param brand the brand name
   */
  public Electronics(int itemId, String itemName,
      String ownerName, double startingPrice,
      String description, int warranty, String brand) {
    super(itemId, ownerName, startingPrice, description,
        itemName);
    this.warranty = warranty;
    this.brand = brand;
  }

  public int getWarranty() {
    return warranty;
  }

  public String getBrand() {
    return brand;
  }

  @Override
  public String showInfo() {
    return "Current Item: \nType: Electronic\n Owner: "
        + getOwnerName()
        + "\nBrand: " + getBrand()
        + "\nWarranty Period: " + getWarranty()
        + "\nDescription: " + getDescription()
        + "\nStarting Price: " + getStartingPrice();
  }
}
