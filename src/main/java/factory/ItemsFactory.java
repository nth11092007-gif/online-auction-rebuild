package factory;

import model.Item;
import model.ItemsAttributes;

/** ItemsFactory - factory interface for creating auction items by type. */
public interface ItemsFactory {

  Item createItems(ItemsAttributes request);
}
