package factory;

import model.Arts;
import model.Items;
import model.ItemsAttributes;

public class TypeArts implements ItemsFactory{
    @Override
    public Items createItems(ItemsAttributes request) {
        return new Arts(0, request.getOwnerName(), request.getStartingPrice(), request.getDescription(), request.getArtistName(), request.getReleaseDate());
    }
}