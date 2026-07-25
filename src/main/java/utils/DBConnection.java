package utils;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/** DBConnection - manages the HikariCP database connection pool. */
public class DBConnection {
  private static final Logger logger =
      LoggerFactory.getLogger(DBConnection.class);
  private static HikariDataSource dataSource;

  static {
    try {
      // Cấu hình HikariCP từ AppConfig
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(AppConfig.getDbUrl());
      config.setUsername(AppConfig.getDbUser());
      config.setPassword(AppConfig.getDbPassword());


      // Tối ưu pool
      config.setMaximumPoolSize(20);
      config.setMinimumIdle(20);
      config.setConnectionTimeout(10000);
      config.setIdleTimeout(300000);
      config.setMaxLifetime(1800000);
      config.setPoolName("HikariPool-DauGia");
      // Bỏ connectionTestQuery — MySQL Connector/J 8.x
      // dùng isValid() tự động, nhẹ hơn "SELECT 1"
      config.setDriverClassName("com.mysql.cj.jdbc.Driver");

      dataSource = new HikariDataSource(config);
      logger.info("✅ HikariCP DataSource khởi tạo thành công!");
    } catch (Exception e) {
      logger.error(
          "❌ Lỗi khởi tạo HikariCP: {}", e.getMessage(), e);
      throw new ExceptionInInitializerError(e);
    }
  }

  /**
   * Returns a connection from the HikariCP connection pool.
   *
   * @return a database connection
   * @throws SQLException if the pool is not initialized or a connection cannot be obtained
   */
  public static Connection getConnection() throws SQLException {
    if (dataSource == null) {
      throw new SQLException(
          "HikariDataSource chưa được khởi tạo");
    }
    return dataSource.getConnection();
  }

  public static HikariDataSource getDataSource() {
    return dataSource;
  }

  /** Closes the HikariCP connection pool and releases all resources. */
  public static void closePool() {
    if (dataSource != null && !dataSource.isClosed()) {
      dataSource.close();
      logger.info("🔌 HikariCP connection pool đã đóng.");
    }
  }
}