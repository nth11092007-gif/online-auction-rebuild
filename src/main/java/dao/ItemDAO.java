package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import model.Items;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface ItemDAO {
    void addItem(Items item);
    void addItem(Connection conn, Items item) throws SQLException;

    Items getItemById(int id);
    Items getItemById(Connection conn,int id) throws SQLException;
    boolean updateItemOwner(int itemId, int newOwnerId);
    boolean updateItemOwner(Connection conn, int itemId, int newOwnerId) throws SQLException;    void setAvatar(int id, File file);
    List<Items> getAllItems() throws SQLException;
    List<Items> getAllItems(Connection conn) throws SQLException;
}