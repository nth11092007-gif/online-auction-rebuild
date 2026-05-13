package factory;

import model.Item;
import model.ItemsAttributes;

public interface ItemsFactory{
    Item createItems(ItemsAttributes request);
}
