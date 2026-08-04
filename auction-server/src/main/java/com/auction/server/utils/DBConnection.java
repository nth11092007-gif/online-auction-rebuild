package com.auction.server.utils;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/** DBConnection - manages the HikariCP database connection pool. */
public final class DBConnection {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(DBConnection.class);
  private static HikariDataSource dataSource;

  private static final int MAX_POOL_SIZE = 20;
  private static final int CONNECTION_TIMEOUT = 10000;
  private static final int IDLE_TIMEOUT = 300000;
  private static final int MAX_LIFETIME = 1800000;

  private DBConnection() {
    // Hide utility class constructor
  }

  static {
    try {
      // Cấu hình HikariCP từ AppConfig
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(AppConfig.getDbUrl());
      config.setUsername(AppConfig.getDbUser());
      config.setPassword(AppConfig.getDbPassword());


      // Tối ưu pool
      config.setMaximumPoolSize(MAX_POOL_SIZE);
      config.setMinimumIdle(MAX_POOL_SIZE);
      config.setConnectionTimeout(CONNECTION_TIMEOUT);
      config.setIdleTimeout(IDLE_TIMEOUT);
      config.setMaxLifetime(MAX_LIFETIME);
      config.setPoolName("HikariPool-DauGia");
      // Bỏ connectionTestQuery — MySQL Connector/J 8.x
      // dùng isValid() tự động, nhẹ hơn "SELECT 1"
      config.setDriverClassName("com.mysql.cj.jdbc.Driver");

      dataSource = new HikariDataSource(config);
      LOGGER.info("✅ HikariCP DataSource khởi tạo thành công!");
    } catch (Exception e) {
      LOGGER.error(
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
      LOGGER.info("🔌 HikariCP connection pool đã đóng.");
    }
  }
}
