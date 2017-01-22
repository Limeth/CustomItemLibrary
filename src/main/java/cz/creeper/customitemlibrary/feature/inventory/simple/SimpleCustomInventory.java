package cz.creeper.customitemlibrary.feature.inventory.simple;

import com.flowpowered.math.vector.Vector2i;
import com.google.common.collect.Lists;
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
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

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

    private void setFeature(CustomSlot customSlot, String featureId) {
        Vector2i position = customSlot.getPosition();
        GUIFeature feature = customSlot.getFeature(featureId)
                .orElseThrow(() -> new IllegalArgumentException("No feature found with id '" + featureId + "' in this slot."));
        int slotIndex = SimpleCustomInventoryDefinition.getSlotIndex(position.getX(), position.getY());
        Iterator<Slot> slotIterator = inventory.<Slot>slots().iterator();

        for(int i = 0; i < slotIndex; i++)
            slotIterator.next();

        Slot slot = slotIterator.next();

        slot.set(feature.createItemStack());
    }

    public void setFeature(int x, int y, String featureId) {
        setFeature(getDefinition().getCustomSlot(x, y), featureId);
    }

    public void setFeature(Vector2i position, String featureId) {
        setFeature(position.getX(), position.getY(), featureId);
    }

    public void setFeature(String slotId, String featureId) {
        CustomSlot customSlot = getDefinition().getCustomSlot(slotId)
                .orElseThrow(() -> new IllegalArgumentException("No slot found with id '" + slotId + "'."));

        setFeature(customSlot, featureId);
    }

    static Random random = new Random();

    @Override
    public void accept(InteractInventoryEvent event) {
        if(event instanceof AffectSlotEvent) {
            AffectSlotEvent affectSlotEvent = (AffectSlotEvent) event;

            affectSlotEvent.getTransactions().forEach(slotTransaction -> {
                Slot slot = slotTransaction.getSlot();
                int slotIndex = temporaryGetSlotIndex(slot);
                Vector2i slotPosition = SimpleCustomInventoryDefinition.getSlotLocation(slotIndex);
                CustomSlot customSlot = getDefinition().getCustomSlot(slotPosition.getX(), slotPosition.getY());
                List<String> featureIds = Lists.newArrayList(customSlot.getFeatures().keySet());
                String randomFeature = featureIds.get(random.nextInt(featureIds.size()));

                setFeature(customSlot, randomFeature);
            });
        }
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
