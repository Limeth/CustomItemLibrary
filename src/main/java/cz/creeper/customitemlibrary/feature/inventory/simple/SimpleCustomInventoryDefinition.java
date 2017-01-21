package cz.creeper.customitemlibrary.feature.inventory.simple;

import com.flowpowered.math.vector.Vector2i;
import com.google.common.base.Preconditions;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.feature.inventory.AbstractCustomInventoryDefinition;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

@EqualsAndHashCode(callSuper = true)
@Getter
public class SimpleCustomInventoryDefinition extends AbstractCustomInventoryDefinition<SimpleCustomInventory> {
    public static final int INVENTORY_SLOTS_WIDTH = 9;
    public static final int INVENTORY_TEXTURE_PADDING_TOP = 18;
    public static final int INVENTORY_TEXTURE_PADDING_LEFT = 8;
    public static final int INVENTORY_TEXTURE_SLOT_SIZE = 16;
    public static final int INVENTORY_TEXTURE_SLOT_GAP = 2;
    private final CustomSlot[][] slots;

    public SimpleCustomInventoryDefinition(PluginContainer pluginContainer, String typeId, CustomSlot[][] slots) {
        super(pluginContainer, typeId);
        Preconditions.checkNotNull(slots, "slots");
        Preconditions.checkArgument(slots.length > 0, "The slots array have a positive height.");

        Arrays.stream(slots).forEach(row -> {
            Preconditions.checkNotNull(row, "slots");
            Preconditions.checkArgument(row.length == INVENTORY_SLOTS_WIDTH, "The slots array must be " + INVENTORY_SLOTS_WIDTH + " items wide.");
        });

        this.slots = slots;
    }

    @Override
    public SimpleCustomInventory open(Player player, Cause cause) {
        SimpleCustomInventory result = new SimpleCustomInventory(this);
        String typeId = getTypeId();
        InventoryArchetype archetype = InventoryArchetype.builder()
                .with(InventoryArchetypes.DOUBLE_CHEST)
                .property(new InventoryDimension(INVENTORY_SLOTS_WIDTH, getHeight()))
                .build(typeId, typeId);
        Inventory inventory = Inventory.builder()
                .of(archetype)
                .listener(InteractInventoryEvent.class, result)
                .build(CustomItemLibrary.getInstance());

        result.setInventory(inventory);
        populate(inventory, player, cause);
        player.openInventory(inventory, cause);

        return result;
    }

    @Override
    public void populate(Inventory inventory, Player player, Cause cause) {
        Iterator<Slot> slotIterator = inventory.<Slot>slots().iterator();

        for(int index = 0; index < getSize(); index++) {
            final Vector2i location = getSlotLocation(index);

            if(!slotIterator.hasNext())
                throw new IllegalStateException("Could not access GridInventory slot at [" + location.getX() + "; " + location.getY() + "].");

            Slot slot = slotIterator.next();
            CustomSlot customSlot = getCustomSlot(location);
            ItemStack itemStack = customSlot.createDefaultItemStack();

            itemStack.offer(Keys.DISPLAY_NAME, Text.of(customSlot.getDefaultFeatureId()));

            slot.set(itemStack);
        }
    }

    public CustomSlot getCustomSlot(Vector2i location) {
        return slots[location.getY()][location.getX()];
    }

    @Override
    public int getHeight() {
        return slots.length;
    }

    @Override
    public int getSize() {
        return INVENTORY_SLOTS_WIDTH * getHeight();
    }

    public CustomSlot[][] getSlots() {
        CustomSlot[][] result = new CustomSlot[getHeight()][INVENTORY_SLOTS_WIDTH];

        for(int y = 0; y < getHeight(); y++) {
            System.arraycopy(slots[y], 0, result[y], 0, INVENTORY_SLOTS_WIDTH);
        }

        return result;
    }

    public Stream<CustomSlot> getSlotStream() {
        return Arrays.stream(slots).flatMap(Arrays::stream);
    }

    public static int getSlotIndex(int x, int y) {
        return x + y * INVENTORY_SLOTS_WIDTH;
    }

    public static Vector2i getSlotLocation(int index) {
        return Vector2i.from(index % INVENTORY_SLOTS_WIDTH, index / INVENTORY_SLOTS_WIDTH);
    }

    public static SimpleCustomInventoryDefinitionBuilder builder() {
        return new SimpleCustomInventoryDefinitionBuilder();
    }
}
