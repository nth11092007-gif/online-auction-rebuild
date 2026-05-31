import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import service.AuctionService;
import utils.DBConnection;

/** Integration tests for AuctionService with real database. */
public class AuctionServiceIntegrationTest {

  private AuctionService auctionService;

  @BeforeEach
  public void setUp() {
    auctionService = new AuctionService();

    try (Connection conn = DBConnection.getConnection();
        Statement stmt = conn.createStatement()) {

      // Reset database with seed data
      String sqlPath = "src/test/resources/quan_ly_dau_gia.sql";
      String sqlContent =
          new String(Files.readAllBytes(Paths.get(sqlPath)));
      stmt.execute(sqlContent);

      // Create an OPEN session for testing
      String insertTestSession =
          "INSERT INTO auction_sessions "
          + "(session_id, owner_id, item_id,"
          + " starting_price, step_price, status) "
          + "VALUES (1, 2, 1, 1500, 100, 'OPEN')";
      stmt.executeUpdate(insertTestSession);

      System.out.println(
          "Database reset and test session created!");

    } catch (Exception e) {
      fail("Setup failed: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("Place bid successfully with real database")
  public void testPlaceBidWithRealDatabase() {
    System.out.println("Starting bid test...");

    int realBidderId = 3;
    String realSessionId = "1";
    double bidAmount = 2000.0;

    try {
      boolean isSuccess =
          auctionService.placeBid(
              realBidderId, realSessionId, bidAmount);

      assertTrue(isSuccess,
          "Bid failed - service logic has issues!");

      System.out.println("Bid SUCCESS! Check MySQL:");
      System.out.println(
          " - users: buyer_an (ID 3) balance frozen");
      System.out.println(
          " - bids: 2000.0 for session 1");

    } catch (Exception e) {
      e.printStackTrace();
      fail("Test crashed: " + e.getMessage());
    }
  }
}
