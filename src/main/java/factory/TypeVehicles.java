package factory;

import model.Items;
import model.ItemsAttributes;
import model.Vehicles;

public class TypeVehicles implements ItemsFactory{
    @Override
    public Items createItems(ItemsAttributes request) {
        return new Vehicles(0, request.getOwnerName(), request.getStartingPrice(), request.getDescription(), request.getBrand(), request.getMileage(), request.getVehicleID());
    }
}