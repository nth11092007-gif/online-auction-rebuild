package utils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;

class IDGeneratorTest {
    @Test
    void generateSessionId_NoDuplicates() {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            String id = IDGenerator.generateSessionId();
            assertTrue(ids.add(id), "Duplicate ID found: " + id);
        }
    }
}