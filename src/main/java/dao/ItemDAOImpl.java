package dao;

import model.*;
import utils.DBConnection;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;

public class ItemDAOImpl implements ItemDAO {

    @Override
    public void addItem(Items item) {
        String sql = "INSERT INTO items (item_type, owner, starting_price, description, " +
                "artist_name, release_date, warranty, brand, mileage, vehicle_id_plate, avatar) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(2, item.getOwner());
            ps.setDouble(3, item.getStartingPrice());
            ps.setString(4, item.getDescription());
            BufferedImage avatar = item.getAvatar();
            if (avatar != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                javax.imageio.ImageIO.write(avatar, "png", baos);
                byte[] imageBytes = baos.toByteArray();
                ps.setBytes(11, imageBytes);
            } else {
                ps.setNull(11, java.sql.Types.BLOB);
            }

            if (item instanceof Arts) {
                Arts art = (Arts) item;
                ps.setString(1, "Arts");
                ps.setString(5, art.getArtistName());
                ps.setDate(6, java.sql.Date.valueOf(art.getReleaseDate()));
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
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public Items getItemById(int id) {
        String sql = "SELECT * FROM items WHERE item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String type = rs.getString("item_type");
                    String owner = rs.getString("owner");
                    double startingPrice = rs.getDouble("starting_price");
                    String desc = rs.getString("description");

                    Items item = null;

                    // 1. Khởi tạo đối tượng theo Type
                    if ("Arts".equals(type)) {
                        java.sql.Date sqlDate = rs.getDate("release_date");
                        item = new Arts(id, owner, startingPrice, desc,
                                rs.getString("artist_name"),
                                sqlDate != null ? sqlDate.toLocalDate() : null);
                    }
                    else if ("Electronics".equals(type)) {
                        item = new Electronics(id, owner, startingPrice, desc,
                                rs.getInt("warranty"),
                                rs.getString("brand"));
                    }
                    else if ("Vehicles".equals(type)) {
                        item = new Vehicles(id, owner, startingPrice, desc,
                                rs.getString("brand"),
                                rs.getInt("mileage"),
                                rs.getString("vehicle_id_plate"));
                    }

                    // 2. Xử lý kiểu dữ liệu Blob (Avatar)
                    if (item != null) {
                        Blob blob = rs.getBlob("avatar");
                        if (blob != null) {
                            try (java.io.InputStream is = blob.getBinaryStream()) {
                                BufferedImage bi = javax.imageio.ImageIO.read(is);
                                item.setAvatar(bi); // Sử dụng setter đã thêm ở bước 1
                            } catch (java.io.IOException e) {
                                System.err.println("Lỗi chuyển đổi ảnh từ database: " + e.getMessage());
                            }
                        }
                    }

                    return item;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setAvatar(int itemId, File file) {
        String sql = "UPDATE items SET avatar = ? WHERE item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             FileInputStream fis = new FileInputStream(file)) {

            // Chuyển đổi File thành luồng nhị phân để lưu vào BLOB
            ps.setBinaryStream(1, fis, (int) file.length());
            ps.setInt(2, itemId);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Cập nhật ảnh đại diện thành công cho Item ID: " + itemId);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}