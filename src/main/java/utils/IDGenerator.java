package utils;

import java.util.UUID;

public class IDGenerator {
    // Sinh mã UUID ngẫu nhiên dài 36 ký tự.
    public static String generateSessionId() {
        return UUID.randomUUID().toString();
    }
}
