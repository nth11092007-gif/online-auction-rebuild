package model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ItemsAttributesTest {

    private final User owner = new User("Real", "user", "e@mail", "pass", "0123");

    @Test
    void build_WithAllFields_HasCorrectValues() {
        ItemsAttributes attr = new ItemsAttributes.Builder(owner, 500.0,"Bức tranh mặt nươc")
                .description("desc")
                .artistName("Van Gogh")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .brand("Samsung")
                .warranty(12)
                .mileage(10000)
                .vehicleId("ABC-123")
                .build();

        assertEquals(owner.getUsername(), attr.getOwnerName());
        assertEquals(500.0, attr.getStartingPrice());
        assertEquals("desc", attr.getDescription());
        assertEquals("Van Gogh", attr.getArtistName());
        assertEquals(LocalDate.of(2020, 1, 1), attr.getReleaseDate());
        assertEquals("Samsung", attr.getBrand());
        assertEquals(12, attr.getWarranty());
        assertEquals(10000, attr.getMileage());
        assertEquals("ABC-123", attr.getVehicleId());
    }

    @Test
    void build_MinimalAttributes_HasDefaults() {
        ItemsAttributes attr = new ItemsAttributes.Builder(owner, 100.0, "test").build();
        assertEquals("", attr.getDescription());
        assertNull(attr.getArtistName());
        assertEquals(0, attr.getWarranty());
        assertEquals(0, attr.getMileage());
    }
}