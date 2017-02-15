package cz.creeper.customitemlibrary.feature.inventory.simple;

import cz.creeper.customitemlibrary.data.mutable.CustomInventoryData;
import lombok.NonNull;
import lombok.Value;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;

import java.util.Iterator;
import java.util.Optional;

@Value
public class CustomSlot {

    /**
     * @deprecated May be null
     */
    @Deprecated
    SimpleCustomInventory customInventory;

    SimpleCustomInventoryDefinition customInventoryDefinition;
    DataHolder dataHolder;
    CustomSlotDefinition definition;
    int slotIndex;

    public CustomSlot(@NonNull SimpleCustomInventory customInventory, @NonNull CustomSlotDefinition definition, int slotIndex) {
        this.customInventory = customInventory;
        this.customInventoryDefinition = customInventory.getDefinition();
        this.dataHolder = customInventory.getDataHolder();
        this.definition = definition;
        this.slotIndex = slotIndex;
    }

    public CustomSlot(@NonNull SimpleCustomInventoryDefinition customInventoryDefinition, @NonNull DataHolder dataHolder, @NonNull CustomSlotDefinition definition, int slotIndex) {
        this.customInventory = null;
        this.customInventoryDefinition = customInventoryDefinition;
        this.dataHolder = dataHolder;
        this.definition = definition;
        this.slotIndex = slotIndex;
    }

    public void setFeature(String featureId) {
        GUIFeature feature = definition.getFeature(featureId)
                .orElseThrow(() -> new IllegalArgumentException("No feature found with id '" + featureId + "' in this slot."));

        if(definition.isPersistent()) {
            CustomInventoryData data = customInventoryDefinition.getCustomInventoryData(dataHolder);

            data.getSlotIdToFeatureId().put(definition.getId().get(), featureId);
            customInventoryDefinition.setCustomInventoryData(dataHolder, data);
        }

        setItemStack(feature.createItemStack());
    }

    public void setItemStack(ItemStack itemStack) {
        CustomInventoryData data = customInventoryDefinition.getCustomInventoryData(dataHolder);

        if(itemStack != null) {
            if(definition.isPersistent()) {
                String definitionId = definition.getId()
                        .orElseThrow(() -> new IllegalStateException("Could not access the ID of a persistent CustomSlot."));
                data.getSlotIdToItemStack().put(definitionId, itemStack.createSnapshot());
                customInventoryDefinition.setCustomInventoryData(dataHolder, data);
            }

            getSlot().ifPresent(slot -> slot.set(itemStack));
        } else {
            if(definition.isPersistent()) {
                String definitionId = definition.getId()
                        .orElseThrow(() -> new IllegalStateException("Could not access the ID of a persistent CustomSlot."));
                data.getSlotIdToItemStack().put(definitionId, ItemStackSnapshot.NONE);
                customInventoryDefinition.setCustomInventoryData(dataHolder, data);
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
            CustomInventoryData data = customInventoryDefinition.getCustomInventoryData(dataHolder);
            String definitionId = definition.getId()
                    .orElseThrow(() -> new IllegalStateException("Could not access the ID of a persistent CustomSlot."));

            return Optional.ofNullable(data.getSlotIdToItemStack().get(definitionId))
                    .map(ItemStackSnapshot::createStack);
        }

        return Optional.ofNullable(definition.createDefaultItemStack());
    }

    public Optional<Slot> getSlot() {
        return Optional.ofNullable(customInventory)
                .map(SimpleCustomInventory::getInventory)
                .map(inventory -> {
                    Iterator<Slot> slotIterator = inventory.<Slot>slots().iterator();

                    for(int i = 0; i < slotIndex; i++)
                        slotIterator.next();

                    return slotIterator.next();
                });
    }
}
