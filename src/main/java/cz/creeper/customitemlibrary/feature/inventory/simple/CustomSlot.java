package cz.creeper.customitemlibrary.feature.inventory.simple;

import cz.creeper.customitemlibrary.data.mutable.CustomInventoryData;
import lombok.Value;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;

@Value
public class CustomSlot {
    SimpleCustomInventory customInventory;
    CustomSlotDefinition definition;
    Slot slot;

    public void setFeature(String featureId) {
        GUIFeature feature = definition.getFeature(featureId)
                .orElseThrow(() -> new IllegalArgumentException("No feature found with id '" + featureId + "' in this slot."));

        if(definition.isPersistent()) {
            CustomInventoryData data = customInventory.getCustomInventoryData();

            data.getSlotIdToFeatureId().put(definition.getId().get(), featureId);
            customInventory.setCustomInventoryData(data);
        }

        setItemStack(feature.createItemStack());
    }

    public void setItemStack(ItemStack itemStack) {
        CustomInventoryData data = customInventory.getCustomInventoryData();

        if(itemStack != null) {
            if(definition.isPersistent()) {
                data.getSlotIdToItemStack().put(definition.getId().get(), itemStack.createSnapshot());
                customInventory.setCustomInventoryData(data);
            }

            slot.set(itemStack);
        } else {
            if(definition.isPersistent()) {
                data.getSlotIdToItemStack().put(definition.getId().get(), ItemStackSnapshot.NONE);
                customInventory.setCustomInventoryData(data);
            }

            slot.clear();
        }
    }
}
