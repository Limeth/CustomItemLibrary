package cz.creeper.customitemlibrary.feature.inventory.simple;

import com.flowpowered.math.vector.Vector2i;
import com.google.common.base.Preconditions;
import cz.creeper.customitemlibrary.data.mutable.CustomInventoryData;
import cz.creeper.customitemlibrary.feature.inventory.AbstractCustomInventory;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.event.item.inventory.AffectSlotEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@EqualsAndHashCode(callSuper = true)
@Getter
public class SimpleCustomInventory extends AbstractCustomInventory<SimpleCustomInventoryDefinition> implements Consumer<InteractInventoryEvent> {
    public SimpleCustomInventory(SimpleCustomInventoryDefinition definition, DataHolder dataHolder) {
        super(definition, dataHolder);
    }

    private CustomSlot getCustomSlot(CustomSlotDefinition customSlotDefinition) {
        Vector2i position = customSlotDefinition.getPosition();
        int slotIndex = SimpleCustomInventoryDefinition.getSlotIndex(position.getX(), position.getY());

        // TODO: Improve, once the API is expanded
        Iterator<Slot> slotIterator = getInventory().<Slot>slots().iterator();

        for(int i = 0; i < slotIndex; i++)
            slotIterator.next();

        Slot slot = slotIterator.next();

        return new CustomSlot(this, customSlotDefinition, slotIndex);
    }

    public CustomSlot getCustomSlot(int x, int y) {
        return getCustomSlot(getDefinition().getCustomSlotDefinition(x, y));
    }

    public CustomSlot getCustomSlot(Vector2i position) {
        return getCustomSlot(position.getX(), position.getY());
    }

    public Optional<CustomSlot> getCustomSlot(String slotId) {
        return getDefinition().getCustomSlotDefinition(slotId).map(this::getCustomSlot);
    }

    public CustomSlot getCustomSlot(Slot slot) {
        Preconditions.checkArgument(hasChild(slot), "The specified slot isn't a part of this inventory.");

        int slotIndex = temporaryGetSlotIndex(slot);
        Vector2i slotPosition = SimpleCustomInventoryDefinition.getSlotLocation(slotIndex);

        return getCustomSlot(slotPosition);
    }

    public Stream<CustomSlot> customSlots() {
        return StreamSupport.stream(getInventory().<Slot>slots().spliterator(), false)
                .limit(getDefinition().getSize())
                .map(this::getCustomSlot);
    }

    @Override
    public void accept(InteractInventoryEvent event) {
        if(event instanceof AffectSlotEvent) {
            AffectSlotEvent affectSlotEvent = (AffectSlotEvent) event;
            List<SlotTransaction> ownedTransactions = affectSlotEvent.getTransactions().stream()
                    .filter(slotTransaction -> hasChild(slotTransaction.getSlot()))
                    .collect(Collectors.toList());

            ownedTransactions.forEach(slotTransaction -> {
                CustomSlot customSlot = getCustomSlot(slotTransaction.getSlot());

                customSlot.getDefinition().getAffectCustomSlotListener()
                        .onAffectCustomSlot(customSlot, slotTransaction, affectSlotEvent);
            });

            if(!event.isCancelled()) {
                ownedTransactions.forEach(slotTransaction -> {
                    CustomSlot customSlot = getCustomSlot(slotTransaction.getSlot());

                    customSlot.setItemStack(slotTransaction.getFinal().createStack());
                });
            }
        }
    }

    public boolean hasChild(Inventory child) {
        return hasChild(getInventory(), child);
    }

    public boolean hasParent(Inventory parent) {
        return hasChild(parent, getInventory());
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
        return getDefinition().getCustomInventoryData(getDataHolder(), dataHolder -> CustomInventoryData.of(this));
    }

    public void setCustomInventoryData(CustomInventoryData data) {
        getDefinition().setCustomInventoryData(getDataHolder(), data);
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
