package com.auction.common.utils;

import java.util.UUID;

/** IDGenerator - generates unique identifiers for auction sessions and entities. */
public final class IDGenerator {
  private IDGenerator() {
    // Hide implicit public constructor
  }

  // Sinh mã UUID ngẫu nhiên dài 36 ký tự.
  public static String generateSessionId() {
    return UUID.randomUUID().toString();
  }
}
