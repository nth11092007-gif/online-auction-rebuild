package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AppConfig - reads application configuration from config.properties
 * on the classpath. Falls back to defaults if the file is missing.
 */
public class AppConfig {

  private static final Logger logger =
      LoggerFactory.getLogger(AppConfig.class);

  private static final Properties props = new Properties();

  static {
    try (InputStream is = AppConfig.class.getClassLoader()
        .getResourceAsStream("config.properties")) {
      if (is != null) {
        props.load(is);
        logger.info("Đã tải config.properties");
      } else {
        logger.warn(
            "Không tìm thấy config.properties,"
                + " dùng giá trị mặc định");
      }
    } catch (IOException e) {
      logger.error("Lỗi đọc config.properties: {}",
          e.getMessage(), e);
    }
  }

  /** Returns the WebSocket server URI. */
  public static String getWsUri() {
    return props.getProperty(
        "ws.uri", "ws://localhost:8887");
  }

  private AppConfig() { }
}
