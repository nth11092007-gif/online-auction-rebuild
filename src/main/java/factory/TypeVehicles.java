package factory;

import model.Item;
import model.ItemsAttributes;
import model.Vehicles;

public class TypeVehicles implements ItemsFactory{
    @Override
    public Item createItems(ItemsAttributes request) {
        return new Vehicles(0, request.getOwnerName(), request.getStartingPrice(), request.getDescription(), request.getBrand(), request.getMileage(), request.getVehicleID());
    }
}