package dao;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.sql.DataSource;
import model.Arts;
import model.Electronics;
import model.Item;
import model.Vehicles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.DBConnection;

/**
 * Implementation of {@link ItemDAO} using JDBC.
 */
public class ItemDAOImpl implements ItemDAO {

  private static final Logger logger =
      LoggerFactory.getLogger(ItemDAOImpl.class);
  private final DataSource dataSource;

  public ItemDAOImpl() {
    this(DBConnection.getDataSource());
  }

  public ItemDAOImpl(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  // =========================================================================
  // 1. THEM SAN PHAM (NAP CHONG)
  // =========================================================================

  @Override
  public void addItem(Connection conn, Item item)
      throws SQLException {
    String sql = "INSERT INTO items"
        + " (item_type, owner, starting_price, description,"
        + " artist_name, release_date, warranty, brand,"
        + " mileage, vehicle_id_plate, avatar, item_name)"
        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (PreparedStatement ps = conn.prepareStatement(
        sql, Statement.RETURN_GENERATED_KEYS)) {

      ps.setString(2, item.getOwnerName());
      ps.setString(12, item.getItemName());
      ps.setDouble(3, item.getStartingPrice());
      ps.setString(4, item.getDescription());
      BufferedImage avatar = item.getAvatar();
      if (avatar != null) {
        ByteArrayOutputStream baos =
            new ByteArrayOutputStream();
        try {
          ImageIO.write(avatar, "png", baos);
        } catch (IOException e) {
          logger.error(
              "Lỗi khi chuyển ảnh thành bytes: {}",
              e.getMessage(), e);
          logger.error(
              "Lỗi khi đọc file ảnh: {}",
              e.getMessage(), e);
        }
        byte[] imageBytes = baos.toByteArray();
        ps.setBytes(11, imageBytes);
      } else {
        ps.setNull(11, Types.BLOB);
      }

      if (item instanceof Arts) {
        Arts art = (Arts) item;
        ps.setString(1, "Arts");
        ps.setString(5, art.getArtistName());

        LocalDate rd = art.getReleaseDate();
        if (rd != null) {
          ps.setDate(6, Date.valueOf(rd));
        } else {
          ps.setNull(6, Types.DATE);
        }

        ps.setNull(7, Types.INTEGER);
        ps.setNull(8, Types.VARCHAR);
        ps.setNull(9, Types.INTEGER);
        ps.setNull(10, Types.VARCHAR);

      } else if (item instanceof Electronics) {
        Electronics elec = (Electronics) item;
        ps.setString(1, "Electronics");
        ps.setNull(5, Types.VARCHAR);
        ps.setNull(6, Types.DATE);
        ps.setInt(7, elec.getWarranty());
        ps.setString(8, elec.getBrand());
        ps.setNull(9, Types.INTEGER);
        ps.setNull(10, Types.VARCHAR);

      } else if (item instanceof Vehicles) {
        Vehicles veh = (Vehicles) item;
        ps.setString(1, "Vehicles");
        ps.setNull(5, Types.VARCHAR);
        ps.setNull(6, Types.DATE);
        ps.setNull(7, Types.INTEGER);
        ps.setString(8, veh.getBrand());
        ps.setInt(9, veh.getMileage());
        ps.setString(10, veh.getVehicleId());
      }

      int affectedRows = ps.executeUpdate();
      if (affectedRows > 0) {
        try (ResultSet rs = ps.getGeneratedKeys()) {
          if (rs.next()) {
            item.setItemId(rs.getInt(1));
          }
        }
      }
    }
  }

  @Override
  public void addItem(Item item) {
    try (Connection conn = dataSource.getConnection()) {
      addItem(conn, item);
    } catch (SQLException e) {
      logger.error(
          "❌ Lỗi khi thêm item: {}",
          e.getMessage(), e);
    }
  }

  // =========================================================================
  // 2. LAY SAN PHAM THEO ID (NAP CHONG)
  // =========================================================================

  @Override
  public Item getItemById(Connection conn, int id)
      throws SQLException {
    String sql = "SELECT * FROM items WHERE item_id = ?";
    try (PreparedStatement pstmt =
        conn.prepareStatement(sql)) {
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
      logger.error(
          "Lỗi khi lấy item theo ID {}: {}",
          id, e.getMessage(), e);
      return null;
    }
  }

  @Override
  public void setAvatar(int itemId, File file) {
    String sql =
        "UPDATE items SET avatar = ? WHERE item_id = ?";

    try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        FileInputStream fis = new FileInputStream(file)) {

      ps.setBinaryStream(1, fis, (int) file.length());
      ps.setInt(2, itemId);

      int rowsUpdated = ps.executeUpdate();
      if (rowsUpdated > 0) {
        System.out.println(
            "Cập nhật ảnh đại "
            + "diện thành công cho Item ID: "
            + itemId);
      }
    } catch (SQLException | IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public List<Item> getAllItems() {
    try (Connection conn = DBConnection.getConnection()) {
      return getAllItems(conn);
    } catch (SQLException e) {
      logger.error(
          "Lỗi khi lấy tất cả items: {}",
          e.getMessage(), e);
      return new ArrayList<>();
    }
  }

  @Override
  public List<Item> getAllItems(Connection conn)
      throws SQLException {
    String sql = "SELECT * FROM items";
    try (Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {

      List<Item> itemList = new ArrayList<>();
      while (rs.next()) {
        itemList.add(extractItemFromResultSet(rs));
      }
      return itemList;
    } catch (SQLException e) {
      logger.error("Loi khi lay tat ca cac item!");
      return new ArrayList<>();
    }
  }

  // =========================================================================
  // 3. CAP NHAT CHU SO HUU MOI CHO SAN PHAM (NAP CHONG)
  // =========================================================================

  @Override
  public boolean updateItemOwner(
      Connection conn, int itemId, int newOwnerId)
      throws SQLException {
    String sql = "UPDATE items"
        + " SET owner ="
        + " (SELECT username FROM users WHERE id = ?)"
        + " WHERE item_id = ?";
    try (PreparedStatement pstmt =
        conn.prepareStatement(sql)) {
      pstmt.setInt(1, newOwnerId);
      pstmt.setInt(2, itemId);
      return pstmt.executeUpdate() > 0;
    }
  }

  @Override
  public boolean updateItemOwner(
      int itemId, int newOwnerId) {
    try (Connection conn = dataSource.getConnection()) {
      return updateItemOwner(conn, itemId, newOwnerId);
    } catch (SQLException e) {
      logger.error(
          "Lỗi khi cập nhật chủ sở hữu cho item {}: {}",
          itemId, e.getMessage(), e);
      return false;
    }
  }

  // =========================================================================
  // HAM TIEN ICH (Utility) DUNG NOI BO TRONG DAO
  // =========================================================================

  private Item extractItemFromResultSet(ResultSet rs)
      throws SQLException {
    int id = rs.getInt("item_id");
    String itemName = rs.getString("item_name");
    String type = rs.getString("item_type");
    String owner = rs.getString("owner");
    double startingPrice = rs.getDouble("starting_price");
    String desc = rs.getString("description");
    Item item = null;
    if ("Arts".equals(type)) {
      Date sqlDate = rs.getDate("release_date");
      item = new Arts(id, itemName, owner, startingPrice,
          desc, rs.getString("artist_name"),
          sqlDate != null ? sqlDate.toLocalDate() : null);

    } else if ("Electronics".equals(type)) {
      item = new Electronics(id, itemName, owner,
          startingPrice, desc, rs.getInt("warranty"),
          rs.getString("brand"));

    } else if ("Vehicles".equals(type)) {
      item = new Vehicles(id, itemName, owner,
          startingPrice, desc, rs.getString("brand"),
          rs.getInt("mileage"),
          rs.getString("vehicle_id_plate"));
    }
    // Xu ly kieu du lieu Blob (Avatar)
    if (item != null) {
      Blob blob = rs.getBlob("avatar");
      if (blob != null) {
        try (InputStream is = blob.getBinaryStream()) {
          BufferedImage bi = ImageIO.read(is);
          item.setAvatar(bi);
        } catch (IOException e) {
          logger.error(
              "Lỗi chuyển đổi ảnh từ database: "
              + e.getMessage());
        }
      }
    }
    return item;
  }
}
