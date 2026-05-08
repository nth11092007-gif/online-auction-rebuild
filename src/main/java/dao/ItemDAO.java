package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import model.Items;

public interface ItemDAO {
    void addItem(Items item);
    void addItem(Connection conn, Items item) throws SQLException;

    Items getItemById(int id);
    Items getItemById(Connection conn, int id) throws SQLException;

    boolean updateItemOwner(Connection conn, int itemId, int newOwnerId) throws SQLException;
    boolean updateItemOwner(int itemId, int newOwnerId);

    List<Items> getAllItems();
    List<Items> getAllItems(Connection conn) throws SQLException;
}