package com.auction.server.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import com.auction.common.model.Item;

/**
 * Data Access Object interface for item operations.
 */
public interface ItemDAO {
  void addItem(Item item);

  void addItem(Connection conn, Item item) throws SQLException;

  Item getItemById(int id);

  Item getItemById(Connection conn, int id) throws SQLException;

  boolean updateItemOwner(
      Connection conn, int itemId, int newOwnerId)
      throws SQLException;

  boolean updateItemOwner(int itemId, int newOwnerId);

  List<Item> getAllItems();

  List<Item> getAllItems(Connection conn) throws SQLException;

  void setAvatar(int id, File file);
}
