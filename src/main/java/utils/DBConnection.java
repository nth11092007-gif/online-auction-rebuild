package utils;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBConnection {
    private static final Logger logger = LoggerFactory.getLogger(DBConnection.class);
    private static HikariDataSource dataSource;

    static {
        try {
            // Cấu hình HikariCP
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://auction-db-01-auctionsystem1.f.aivencloud.com:26764/defaultdb?sslMode=REQUIRED");
            config.setUsername("avnadmin");
            config.setPassword("AVNS_ULXrtrBGv69ar_pPpPZ");

            // Tối ưu pool
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.setConnectionTestQuery("SELECT 1");
            config.setPoolName("HikariPool-DauGia");
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");

            dataSource = new HikariDataSource(config);
            logger.info("✅ HikariCP DataSource khởi tạo thành công!");
        } catch (Exception e) {
            logger.error("❌ Lỗi khởi tạo HikariCP: {}", e.getMessage(), e);
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("HikariDataSource chưa được khởi tạo");
        }
        return dataSource.getConnection();
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }

    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("🔌 HikariCP connection pool đã đóng.");
        }
    }
}