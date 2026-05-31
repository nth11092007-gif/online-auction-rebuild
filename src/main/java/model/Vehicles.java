package model;

/**
 * Represents a vehicle item in the auction system.
 */
public class Vehicles extends Item {

  private final String brand;

  private final int mileage;

  private final String vehicleId;

  /**
   * Constructs a Vehicles item with all attributes.
   *
   * @param itemId the unique item identifier
   * @param itemName the display name
   * @param ownerName the owner's name
   * @param startingPrice the starting auction price
   * @param description the item description
   * @param brand the vehicle brand
   * @param mileage the vehicle mileage
   * @param vehicleId the license plate or vehicle ID
   */
  public Vehicles(int itemId, String itemName,
      String ownerName, double startingPrice,
      String description, String brand, int mileage,
      String vehicleId) {
    super(itemId, ownerName, startingPrice, description,
        itemName);
    this.brand = brand;
    this.mileage = mileage;
    this.vehicleId = vehicleId;
  }

  public String getBrand() {
    return brand;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public int getMileage() {
    return mileage;
  }

  @Override
  public String showInfo() {
    return "Current Item: \nType: Vehicle\n Owner: "
        + getOwnerName()
        + "\nBrand: " + getBrand()
        + "\nLicense Plate: " + getVehicleId()
        + "\nMileage: " + getMileage()
        + "\nDescription: " + getDescription()
        + "\nStarting Price: " + getStartingPrice();
  }
}
