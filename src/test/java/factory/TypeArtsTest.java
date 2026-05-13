package factory;

import model.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class TypeArtsTest {

    @Test
    void createItems_ReturnsArtsWithCorrectFields() {
        ItemsAttributes attr = new ItemsAttributes.Builder(new User("Real","u","e","p","0"), 1000.0)
                .artistName("Picasso")
                .releaseDate(LocalDate.of(1907, 7, 1))
                .description("Les Demoiselles d'Avignon")
                .build();

        ItemsFactory factory = new TypeArts();
        Items item = factory.createItems(attr);

        assertInstanceOf(Arts.class, item);
        Arts art = (Arts) item;
        assertEquals("Picasso", art.getArtistName());
        assertEquals(LocalDate.of(1907, 7, 1), art.getReleaseDate());
        assertEquals(1000.0, art.getStartingPrice());
        assertEquals("u", art.getOwnerName());   // ownerName từ User
        assertEquals("Les Demoiselles d'Avignon", art.getDescription());
    }
}