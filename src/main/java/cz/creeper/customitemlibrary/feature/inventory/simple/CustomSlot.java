package cz.creeper.customitemlibrary.feature.inventory.simple;

import cz.creeper.customitemlibrary.data.mutable.CustomInventoryData;
import lombok.Value;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;

import java.util.Iterator;
import java.util.Optional;

@Value
public class CustomSlot {
    SimpleCustomInventory customInventory;
    CustomSlotDefinition definition;
    int slotIndex;

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
                String definitionId = definition.getId()
                        .orElseThrow(() -> new IllegalStateException("Could not access the ID of a persistent CustomSlot."));
                data.getSlotIdToItemStack().put(definitionId, itemStack.createSnapshot());
                customInventory.setCustomInventoryData(data);
            }

            getSlot().ifPresent(slot -> slot.set(itemStack));
        } else {
            if(definition.isPersistent()) {
                String definitionId = definition.getId()
                        .orElseThrow(() -> new IllegalStateException("Could not access the ID of a persistent CustomSlot."));
                data.getSlotIdToItemStack().put(definitionId, ItemStackSnapshot.NONE);
                customInventory.setCustomInventoryData(data);
            }

            getSlot().ifPresent(Inventory::clear);
        }
    }

    public Optional<ItemStack> getItemStack() {
        Optional<Slot> slot = getSlot();

        if(slot.isPresent()) {
            return slot.get().peek();
        }

        if(definition.isPersistent()) {
            CustomInventoryData data = customInventory.getCustomInventoryData();
            String definitionId = definition.getId()
                    .orElseThrow(() -> new IllegalStateException("Could not access the ID of a persistent CustomSlot."));

            return Optional.ofNullable(data.getSlotIdToItemStack().get(definitionId))
                    .map(ItemStackSnapshot::createStack);
        }

        return Optional.ofNullable(definition.createDefaultItemStack());
    }

    public Optional<Slot> getSlot() {
        return customInventory.getContainer().map(container -> {
            Iterator<Slot> slotIterator = container.<Slot>slots().iterator();

            for(int i = 0; i < slotIndex; i++)
                slotIterator.next();

            return slotIterator.next();
        });
    }
}
