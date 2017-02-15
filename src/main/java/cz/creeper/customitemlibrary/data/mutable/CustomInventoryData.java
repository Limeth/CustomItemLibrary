package cz.creeper.customitemlibrary.data.mutable;

// Don't let this expand to individual static imports, as it breaks Lombok for some reason
import static cz.creeper.customitemlibrary.data.CustomItemLibraryKeys.*;

import com.google.common.collect.Maps;
import cz.creeper.customitemlibrary.feature.inventory.simple
        .SimpleCustomInventory;
import cz.creeper.customitemlibrary.feature.inventory.simple
        .SimpleCustomInventoryDefinition;
import lombok.Value;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;

import java.util.Map;
import java.util.stream.Collectors;

@Value
public class CustomInventoryData implements DataSerializable {
    public static final String ID_UNINITIALIZED = "__UNINITIALIZED__";

    String id;
    Map<String, ItemStackSnapshot> slotIdToItemStack = Maps.newHashMap();
    Map<String, String> slotIdToFeatureId = Maps.newHashMap();

    public CustomInventoryData(String id,
                               Map<String, ItemStackSnapshot> slotIdToItemStack,
                               Map<String, String> slotIdToFeatureId) {
        this.id = id;

        if(slotIdToItemStack != null)
            this.slotIdToItemStack.putAll(slotIdToItemStack);

        if(slotIdToFeatureId != null)
            this.slotIdToFeatureId.putAll(slotIdToFeatureId);
    }

    public static CustomInventoryData empty(SimpleCustomInventoryDefinition definition) {
        return new CustomInventoryData(definition.getTypeId(), null, null);
    }

    public static CustomInventoryData of(SimpleCustomInventory customInventory) {
        Map<String, ItemStackSnapshot> slotIdToItemStack = customInventory.customSlots()
                .filter(customSlot -> customSlot.getDefinition().isPersistent())
                .collect(Collectors.toMap(
                        customSlot -> customSlot.getDefinition().getId().get(),
                        customSlot -> customSlot.getSlot()
                                .flatMap(Slot::peek)
                                .map(ItemStack::createSnapshot)
                                .orElse(ItemStackSnapshot.NONE)
                ));

        return new CustomInventoryData(customInventory.getDefinition().getTypeId(), slotIdToItemStack, null);
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(CUSTOM_INVENTORY_ID, id)
                .set(CUSTOM_INVENTORY_SLOT_ID_TO_ITEMSTACK, slotIdToItemStack)
                .set(CUSTOM_INVENTORY_SLOT_ID_TO_FEATURE_ID, slotIdToFeatureId);
    }
}
