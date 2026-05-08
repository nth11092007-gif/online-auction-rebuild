package dao;

import model.Items;

import java.io.File;
import java.sql.Connection;
import java.util.List;

public interface ItemDAO {
    void addItem(Items item);

    Items getItemById(int id);
    void setAvatar(int id, File file);
    List<Items> getAllItems();
    List<Items> getAllItems(Connection conn);

}