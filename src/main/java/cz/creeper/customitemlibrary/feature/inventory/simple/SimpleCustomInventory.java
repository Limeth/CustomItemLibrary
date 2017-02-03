package cz.creeper.customitemlibrary.feature.inventory.simple;

import com.flowpowered.math.vector.Vector2i;
import com.google.common.base.Preconditions;
import cz.creeper.customitemlibrary.data.CustomInventoriesData;
import cz.creeper.customitemlibrary.data.CustomInventoryData;
import cz.creeper.customitemlibrary.feature.inventory.AbstractCustomInventory;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.event.item.inventory.AffectSlotEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@EqualsAndHashCode(callSuper = true)
@Getter
public class SimpleCustomInventory extends AbstractCustomInventory<SimpleCustomInventoryDefinition> implements Consumer<InteractInventoryEvent> {
    private final DataHolder dataHolder;
    // Initialized after construction
    private Inventory inventory;

    public SimpleCustomInventory(SimpleCustomInventoryDefinition definition, @NonNull DataHolder dataHolder) {
        super(definition);

        this.dataHolder = dataHolder;
    }

    void setInventory(@NonNull Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public DataHolder getDataHolder() {
        return dataHolder;
    }

    private CustomSlot getCustomSlot(CustomSlotDefinition customSlotDefinition) {
        Vector2i position = customSlotDefinition.getPosition();
        int slotIndex = SimpleCustomInventoryDefinition.getSlotIndex(position.getX(), position.getY());

        // TODO: Improve, once the API is expanded
        Iterator<Slot> slotIterator = inventory.<Slot>slots().iterator();

        for(int i = 0; i < slotIndex; i++)
            slotIterator.next();

        Slot slot = slotIterator.next();

        return new CustomSlot(this, customSlotDefinition, slot);
    }

    public CustomSlot getCustomSlot(int x, int y) {
        return getCustomSlot(getDefinition().getCustomSlotDefinition(x, y));
    }

    public CustomSlot getCustomSlot(Vector2i position) {
        return getCustomSlot(position.getX(), position.getY());
    }

    public CustomSlot getCustomSlot(String slotId) {
        CustomSlotDefinition customSlotDefinition = getDefinition().getCustomSlotDefinition(slotId)
                .orElseThrow(() -> new IllegalArgumentException("No slot found with id '" + slotId + "'."));

        return getCustomSlot(customSlotDefinition);
    }

    public CustomSlot getCustomSlot(Slot slot) {
        Preconditions.checkArgument(hasChild(slot), "The specified slot isn't a part of this inventory.");

        int slotIndex = temporaryGetSlotIndex(slot);
        Vector2i slotPosition = SimpleCustomInventoryDefinition.getSlotLocation(slotIndex);

        return getCustomSlot(slotPosition);
    }

    public Stream<CustomSlot> customSlots() {
        return StreamSupport.stream(inventory.<Slot>slots().spliterator(), false)
                .limit(getDefinition().getSize())
                .map(this::getCustomSlot);
    }

    @Override
    public void accept(InteractInventoryEvent event) {
        if(event instanceof AffectSlotEvent) {
            AffectSlotEvent affectSlotEvent = (AffectSlotEvent) event;

            affectSlotEvent.getTransactions().forEach(slotTransaction -> {
                Slot slot = slotTransaction.getSlot();

                if(!hasChild(slot))
                    return;

                CustomSlot customSlot = getCustomSlot(slot);

                customSlot.getDefinition().getAffectCustomSlotListener()
                        .onAffectCustomSlot(customSlot, slotTransaction, affectSlotEvent);
            });
        }
    }

    public boolean hasChild(Inventory child) {
        return hasChild(inventory, child);
    }

    public boolean hasParent(Inventory parent) {
        return hasChild(parent, inventory);
    }

    private static boolean hasChild(Inventory supposedParent, Inventory supposedChild) {
        Inventory parent = supposedChild;

        do
        {
            Inventory newParent = parent.parent();

            if(newParent == supposedParent)
                return true;

            if(newParent == parent)
                return false;

            parent = newParent;
        }
        while(true);
    }

    public CustomInventoryData getCustomInventoryData() {
        CustomInventoriesData customInventoriesData = getCustomInventoriesData();
        String id = getDefinition().getTypeId();

        return customInventoriesData.get(id)
                .orElseGet(() -> {
                    CustomInventoryData result = CustomInventoryData.of(this);

                    customInventoriesData.put(id, result);

                    return result;
                });
    }

    public void setCustomInventoryData(CustomInventoryData data) {
        CustomInventoriesData customInventoriesData = getCustomInventoriesData();
        String id = getDefinition().getTypeId();

        customInventoriesData.put(id, data);
    }

    private CustomInventoriesData getCustomInventoriesData() {
        return dataHolder.get(CustomInventoriesData.class)
                .orElseGet(() -> {
                        CustomInventoriesData result = new CustomInventoriesData();

                        dataHolder.offer(result);

                        return result;
                });
    }

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    private static int temporaryGetSlotIndex(Slot slot) {
        try {
            Field slotField = slot.getClass().getDeclaredField("slot");

            if(!slotField.isAccessible()) {
                slotField.setAccessible(true);
            }

            Object slotLensImpl = slotField.get(slot);
            Field[] fields = Class.forName("org.spongepowered.common.item.inventory.lens.impl.AbstractLens")
                    .getDeclaredFields();
            Field baseField = null;

            for(Field field : fields) {
                if(field.getName().equals("base")) {
                    baseField = field;
                    break;
                }
            }

            if(!baseField.isAccessible()) {
                baseField.setAccessible(true);
            }

            return (int) baseField.get(slotLensImpl);
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
