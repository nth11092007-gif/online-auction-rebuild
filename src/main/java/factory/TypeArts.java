package factory;

import model.Arts;
import model.Item;
import model.ItemsAttributes;

/** TypeArts - factory implementation that creates Art items. */
public class TypeArts implements ItemsFactory {

  @Override
  public Item createItems(ItemsAttributes request) {
    return new Arts(0, request.getItemName(),
        request.getOwnerName(), request.getStartingPrice(),
        request.getDescription(), request.getArtistName(),
        request.getReleaseDate());
  }
}
