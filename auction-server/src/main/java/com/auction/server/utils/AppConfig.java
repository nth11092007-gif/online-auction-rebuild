package com.auction.server.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AppConfig - reads application configuration from config.properties
 * on the classpath. Falls back to defaults if the file is missing.
 */
public final class AppConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);

  private static final Properties PROPS = new Properties();

  static {
    try (InputStream is = AppConfig.class.getClassLoader()
        .getResourceAsStream("config.properties")) {
      if (is != null) {
        PROPS.load(is);
        LOGGER.info("Đã tải config.properties");
      } else {
        LOGGER.warn(
            "Không tìm thấy config.properties,"
                + " dùng giá trị mặc định");
      }
    } catch (IOException e) {
      LOGGER.error("Lỗi đọc config.properties: {}",
          e.getMessage(), e);
    }
  }

  /** Returns the WebSocket server URI. */
  public static String getWsUri() {
    return PROPS.getProperty(
        "ws.uri", "ws://localhost:8887");
  }

  /** Returns the Database JDBC URL. */
  public static String getDbUrl() {
    return PROPS.getProperty(
        "db.url",
        "jdbc:mysql://localhost:3306/quan_ly_dau_gia"
            + "?useSSL=false&serverTimezone=UTC"
            + "&connectionTimeZone=UTC"
            + "&forceConnectionTimeZoneToSession=true");
  }

  /** Returns the Database Username. */
  public static String getDbUser() {
    return PROPS.getProperty("db.username", "root");
  }

  /** Returns the Database Password. */
  public static String getDbPassword() {
    return PROPS.getProperty("db.password", "");
  }

  private AppConfig() {
  }

}
