package factory;

import model.Electronics;
import model.Items;
import model.ItemsAttributes;

public class TypeElectronics implements ItemsFactory{
    @Override
    public Items createItems(ItemsAttributes request) {
        return new Electronics(0, request.getOwnerName(), request.getStartingPrice(), request.getDescription(), request.getWarranty(), request.getBrand());
    }
}