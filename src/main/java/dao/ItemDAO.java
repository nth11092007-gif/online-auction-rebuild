package dao;

import model.Items;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface ItemDAO {
    void addItem(Items item);

    Items getItemById(int id);
    void setAvatar(int id, File file);
    List<Items> getAllItems() throws SQLException;
    List<Items> getAllItems(Connection conn) throws SQLException;

}