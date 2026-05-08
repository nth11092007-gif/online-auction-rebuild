package dao;

import model.Items;

import java.io.File;

public interface ItemDAO {
    void addItem(Items item);

    Items getItemById(int id);
    void setAvatar(int id, File file);
}