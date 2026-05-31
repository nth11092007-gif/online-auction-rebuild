package factory;

import model.Item;
import model.ItemsAttributes;
import model.Vehicles;

/** TypeVehicles - factory implementation that creates Vehicle items. */
public class TypeVehicles implements ItemsFactory {

  @Override
  public Item createItems(ItemsAttributes request) {
    return new Vehicles(0, request.getItemName(),
        request.getOwnerName(), request.getStartingPrice(),
        request.getDescription(), request.getBrand(),
        request.getMileage(), request.getVehicleId());
  }
}
