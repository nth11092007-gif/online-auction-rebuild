package model;

import java.time.LocalDate;

/**
 * Represents an art item in the auction system.
 */
public class Arts extends Item {

  private final String artistName;

  private final LocalDate releaseDate;

  /**
   * Constructs an Arts item with all attributes.
   *
   * @param itemId the unique item identifier
   * @param itemName the display name
   * @param ownerName the owner's name
   * @param startingPrice the starting auction price
   * @param description the item description
   * @param artistName the artist's name
   * @param releaseDate the release date
   */
  public Arts(int itemId, String itemName, String ownerName,
      double startingPrice, String description,
      String artistName, LocalDate releaseDate) {
    super(itemId, ownerName, startingPrice, description,
        itemName);
    this.artistName = artistName;
    this.releaseDate = releaseDate;
  }

  public String getArtistName() {
    return artistName;
  }

  public LocalDate getReleaseDate() {
    return releaseDate;
  }

  @Override
  public String showInfo() {
    return "Current Item: \nType: Art\n Owner: "
        + getOwnerName()
        + "\n Artist Name: " + getArtistName()
        + "\nRelease Date: " + getReleaseDate()
        + "\nDescription: " + getDescription()
        + "\nStarting Price: " + getStartingPrice();
  }
}
