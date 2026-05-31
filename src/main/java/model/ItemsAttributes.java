package model;

import java.time.LocalDate;

/**
 * Contains common and type-specific attributes for auction items.
 * Uses the Builder Pattern to create item attribute objects flexibly
 * and allow easy extension for new item types or attributes.
 */
public class ItemsAttributes {

  // Common attributes (Immutable)
  private final User owner;

  private final String itemName;

  private final double startingPrice;

  private final String description;

  // Arts-specific attributes
  private final String artistName;

  private final LocalDate releaseDate;

  // Electronics / Vehicles attributes
  private final int warranty;

  private final String brand;

  private final int mileage;

  private final String vehicleId;

  /** Private constructor; only Builder may call. */
  private ItemsAttributes(Builder builder) {
    this.owner = builder.owner;
    this.itemName = builder.itemName;
    this.startingPrice = builder.startingPrice;
    this.description = builder.description;
    this.artistName = builder.artistName;
    this.releaseDate = builder.releaseDate;
    this.warranty = builder.warranty;
    this.brand = builder.brand;
    this.mileage = builder.mileage;
    this.vehicleId = builder.vehicleId;
  }

  public User getOwner() {
    return owner;
  }

  public String getItemName() {
    return itemName;
  }

  public String getOwnerName() {
    return owner != null ? owner.getUsername() : "Unknown";
  }

  public double getStartingPrice() {
    return startingPrice;
  }

  public String getDescription() {
    return description;
  }

  public String getArtistName() {
    return artistName;
  }

  public LocalDate getReleaseDate() {
    return releaseDate;
  }

  public int getWarranty() {
    return warranty;
  }

  public String getBrand() {
    return brand;
  }

  public int getMileage() {
    return mileage;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  /**
   * Builder for constructing ItemsAttributes instances.
   */
  public static class Builder {

    // Required parameters
    private final User owner;

    private final String itemName;

    private final double startingPrice;

    // Optional parameters with defaults
    private String description = "";

    private String artistName = null;

    private LocalDate releaseDate = null;

    private int warranty = 0;

    private String brand = null;

    private int mileage = 0;

    private String vehicleId = null;

    /**
     * Constructs a Builder with the required parameters.
     *
     * @param owner the item owner
     * @param startingPrice the starting auction price
     * @param itemName the item display name
     */
    public Builder(User owner, double startingPrice,
        String itemName) {
      this.owner = owner;
      this.startingPrice = startingPrice;
      this.itemName = itemName;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder artistName(String artistName) {
      this.artistName = artistName;
      return this;
    }

    public Builder releaseDate(LocalDate releaseDate) {
      this.releaseDate = releaseDate;
      return this;
    }

    public Builder warranty(int warranty) {
      this.warranty = warranty;
      return this;
    }

    public Builder brand(String brand) {
      this.brand = brand;
      return this;
    }

    public Builder mileage(int mileage) {
      this.mileage = mileage;
      return this;
    }

    public Builder vehicleId(String vehicleId) {
      this.vehicleId = vehicleId;
      return this;
    }

    /** Builds and returns the ItemsAttributes instance. */
    public ItemsAttributes build() {
      return new ItemsAttributes(this);
    }
  }
}
