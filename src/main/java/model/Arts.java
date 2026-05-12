package model;
import java.time.LocalDate;

public class Arts extends Items{
    private final String artistName;
    private final LocalDate releaseDate;

    public Arts(int itemID, String ownerName, double startingPrice, String description, String artistName, LocalDate releaseDate) {
        super(itemID, ownerName, startingPrice, description);
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
        return "Current Item: \nType: Art\n Owner: " + getOwnerName() + "\n Artist Name: " + getArtistName() + "\nRelease Date: " + getReleaseDate() + "\nDescription: " + getDescription() + "\nStarting Price: " + getStartingPrice();
    }
}