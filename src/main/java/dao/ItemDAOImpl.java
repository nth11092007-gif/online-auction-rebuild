package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.Arts;
import model.Electronics;
import model.Item;
import model.Vehicles;
import utils.DBConnection;

public class ItemDAOImpl implements ItemDAO {

    private static final Logger logger = LoggerFactory.getLogger(ItemDAOImpl.class);

    // =========================================================================
    // 1. THÊM SẢN PHẨM (NẠP CHỒNG)
    // =========================================================================

    @Override
    public void addItem(Connection conn, Item item) throws SQLException {
        String sql = "INSERT INTO items (item_type, owner, starting_price, description, " +
                "artist_name, release_date, warranty, brand, mileage, vehicle_id_plate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(2, item.getOwnerName());
            ps.setDouble(3, item.getStartingPrice());
            ps.setString(4, item.getDescription());

            if (item instanceof Arts) {
                Arts art = (Arts) item;
                ps.setString(1, "Arts");
                ps.setString(5, art.getArtistName());
                
                LocalDate rd = art.getReleaseDate();
                if (rd != null) {
                    ps.setDate(6, java.sql.Date.valueOf(rd));
                } else {
                    ps.setNull(6, java.sql.Types.DATE);
                }
                
                ps.setNull(7, java.sql.Types.INTEGER);
                ps.setNull(8, java.sql.Types.VARCHAR);
                ps.setNull(9, java.sql.Types.INTEGER);
                ps.setNull(10, java.sql.Types.VARCHAR);

            } else if (item instanceof Electronics) {
                Electronics elec = (Electronics) item;
                ps.setString(1, "Electronics");
                ps.setNull(5, java.sql.Types.VARCHAR);
                ps.setNull(6, java.sql.Types.DATE);
                ps.setInt(7, elec.getWarranty());
                ps.setString(8, elec.getBrand());
                ps.setNull(9, java.sql.Types.INTEGER);
                ps.setNull(10, java.sql.Types.VARCHAR);

            } else if (item instanceof Vehicles) {
                Vehicles veh = (Vehicles) item;
                ps.setString(1, "Vehicles");
                ps.setNull(5, java.sql.Types.VARCHAR);
                ps.setNull(6, java.sql.Types.DATE);
                ps.setNull(7, java.sql.Types.INTEGER);
                ps.setString(8, veh.getBrand());
                ps.setInt(9, veh.getMileage());
                ps.setString(10, veh.getVehicleID());
            }

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setItemID(rs.getInt(1));
                    }
                }
            }
        }
    }

    @Override
    public void addItem(Item item) {
        try (Connection conn = DBConnection.getConnection()) {
            addItem(conn, item);
        } catch (SQLException e) {
            logger.error("❌ Lỗi khi thêm item: {}", e.getMessage(), e);
        }
    }

    // =========================================================================
    // 2. LẤY SẢN PHẨM THEO ID (NẠP CHỒNG)
    // =========================================================================

    @Override
    public Item getItemById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM items WHERE item_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractItemFromResultSet(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Item getItemById(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            return getItemById(conn, id);
        } catch (SQLException e) {
            logger.error("Lỗi khi lấy item theo ID {}: {}", id, e.getMessage(), e);
            return null;
        }
    }

    // =========================================================================
    // 3. CẬP NHẬT CHỦ SỞ HỮU MỚI CHO SẢN PHẨM (NẠP CHỒNG)
    // =========================================================================

    @Override
    public boolean updateItemOwner(Connection conn, int itemId, int newOwnerId) throws SQLException {
        String sql = "UPDATE items SET owner = (SELECT username FROM users WHERE id = ?) WHERE item_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newOwnerId);
            pstmt.setInt(2, itemId);
            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateItemOwner(int itemId, int newOwnerId) {
        try (Connection conn = DBConnection.getConnection()) {
            return updateItemOwner(conn, itemId, newOwnerId);
        } catch (SQLException e) {
            logger.error("Lỗi khi cập nhật chủ sở hữu cho item {}: {}", itemId, e.getMessage(), e);
            return false;
        }
    }

    // =========================================================================
    // HÀM TIỆN ÍCH (Utility) DÙNG NỘI BỘ TRONG DAO
    // =========================================================================

    private Item extractItemFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("item_id");
        String type = rs.getString("item_type");
        String owner = rs.getString("owner");
        double startingPrice = rs.getDouble("starting_price");
        String desc = rs.getString("description");

        if ("Arts".equals(type)) {
            Date sqlDate = rs.getDate("release_date");
            return new Arts(id, owner, startingPrice, desc,
                    rs.getString("artist_name"),
                    sqlDate != null ? sqlDate.toLocalDate() : null);

        } else if ("Electronics".equals(type)) {
            return new Electronics(id, owner, startingPrice, desc,
                    rs.getInt("warranty"),
                    rs.getString("brand"));

        } else if ("Vehicles".equals(type)) {
            return new Vehicles(id, owner, startingPrice, desc,
                    rs.getString("brand"),
                    rs.getInt("mileage"),
                    rs.getString("vehicle_id_plate"));
        }
        
        return null;
    }
    @Override
    public List<Item> getAllItems() {
        try (Connection conn = DBConnection.getConnection()) {
            return getAllItems(conn);
        } catch (SQLException e) {
            logger.error("Lỗi khi lấy tất cả items: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    @Override
    public List<Item> getAllItems(Connection conn) throws SQLException {
        String sql = "SELECT * FROM items";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<Item> itemList = new ArrayList<>();
            while (rs.next()) {
                itemList.add(extractItemFromResultSet(rs));
            }
            return itemList;
        }
    }
}