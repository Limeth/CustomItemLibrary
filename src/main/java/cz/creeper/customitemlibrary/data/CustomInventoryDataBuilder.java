package cz.creeper.customitemlibrary.data;

import static cz.creeper.customitemlibrary.data.CustomItemLibraryKeys.*;

import lombok.val;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.Map;
import java.util.Optional;

public class CustomInventoryDataBuilder extends AbstractDataBuilder<CustomInventoryData> {
    public CustomInventoryDataBuilder() {
        super(CustomInventoryData.class, 0);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Optional<CustomInventoryData> buildContent(DataView container) throws InvalidDataException {
        if(!container.contains(CUSTOM_INVENTORY_ID, CUSTOM_INVENTORY_SLOT_ID_TO_ITEMSTACK,
                CUSTOM_INVENTORY_SLOT_ID_TO_FEATURE_ID)) {
            return Optional.empty();
        }

        Optional<String> id = container.getString(CUSTOM_INVENTORY_ID.getQuery());

        if(!id.isPresent())
            throw new InvalidDataException("Could not access the ID of the inventory data.");

        val slotIdToItemStack = (Map<String, ItemStackSnapshot>) container.getMap(
                CUSTOM_INVENTORY_SLOT_ID_TO_ITEMSTACK.getQuery()
        ).orElse(null);

        val slotIdToFeatureId = (Map<String, String>) container.getMap(
                CUSTOM_INVENTORY_SLOT_ID_TO_FEATURE_ID.getQuery()
        ).orElse(null);

        return Optional.of(new CustomInventoryData(id.get(), slotIdToItemStack, slotIdToFeatureId));
    }
}
