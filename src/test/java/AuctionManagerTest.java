// import java.time.LocalDate;
// import java.util.List;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertNull;
// import static org.junit.jupiter.api.Assertions.assertSame;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;

// import model.Arts;
// import model.AuctionManager;
// import model.AuctionSession;
// import model.Items;
// import model.User;
// public class AuctionManagerTest {

//     private AuctionManager manager;
//     private User dummySeller;
//     private Items dummyItem;

//     @BeforeEach
//     public void setUp() {
//         manager = AuctionManager.getInstance();
//         dummySeller = new User("Dummy Seller", "dummy", "dummy@mail.com", "pass", "123");
//         dummyItem = new Arts(1, dummySeller.getUsername(), 50000, "A beautiful painting", "Dummy Artist", LocalDate.now());
//     }


//     @Test
//     public void testSingletonInstance() {
//         AuctionManager anotherManager = AuctionManager.getInstance();
//         assertSame(manager, anotherManager, "Both instances should be exactly the same object (Singleton)");
//     }

//     @Test
//     public void testAddAndFindSession() {
//         AuctionSession session = new AuctionSession(dummySeller, dummyItem, 50000, 10000, LocalDate.now());
//         manager.addSession(session);

//         AuctionSession foundSession = manager.findSessionByID(session.getSessionID());
        
//         assertNotNull(foundSession, "Should find the session that was just added");
//         assertEquals(session.getSessionID(), foundSession.getSessionID(), "The IDs should match exactly");
//     }

//     @Test
//     public void testFindSessionNotFound() {
//         AuctionSession notFound = manager.findSessionByID("INVALID_ID_999");
//         assertNull(notFound, "Should return null for non-existent session IDs");
//     }

//     @Test
//     public void testGetSessionsByStatus() {
//         // 1. Create a PENDING session
//         AuctionSession pendingSession = new AuctionSession(dummySeller, dummyItem, 10000, 1000, LocalDate.now());
        
//         // 2. Create an OPEN session
//         AuctionSession openSession = new AuctionSession(dummySeller, dummyItem, 20000, 2000, LocalDate.now());
//         openSession.startSession(1); // Changes status to OPEN

//         manager.addSession(pendingSession);
//         manager.addSession(openSession);

//         // 3. Fetch filtered lists
//         List<AuctionSession> pendingList = manager.getSessionsByStatus(AuctionSession.Status.PENDING);
//         List<AuctionSession> openList = manager.getSessionsByStatus(AuctionSession.Status.OPEN);

//         // 4. Assertions
//         assertTrue(pendingList.contains(pendingSession), "Pending list should contain the pending session");
//         assertFalse(pendingList.contains(openSession), "Pending list should NOT contain the open session");

//         assertTrue(openList.contains(openSession), "Open list should contain the open session");
//         assertFalse(openList.contains(pendingSession), "Open list should NOT contain the pending session");
//     }
// }