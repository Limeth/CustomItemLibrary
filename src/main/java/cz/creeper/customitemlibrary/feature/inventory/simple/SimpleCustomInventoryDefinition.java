package cz.creeper.customitemlibrary.feature.inventory.simple;

import com.flowpowered.math.vector.Vector2i;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.feature.inventory.AbstractCustomInventoryDefinition;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.key.Keys;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EqualsAndHashCode(callSuper = true)
@Getter
public class SimpleCustomInventoryDefinition extends AbstractCustomInventoryDefinition<SimpleCustomInventory> {
    public static final int INVENTORY_SLOTS_WIDTH = 9;
    public static final int INVENTORY_TEXTURE_PADDING_TOP = 18;
    public static final int INVENTORY_TEXTURE_PADDING_RIGHT = 8;
    public static final int INVENTORY_TEXTURE_PADDING_BOTTOM = 1;
    public static final int INVENTORY_TEXTURE_PADDING_LEFT = 8;
    public static final int INVENTORY_TEXTURE_SLOT_SIZE = 16;
    public static final int INVENTORY_TEXTURE_SLOT_GAP = 2;
    private final CustomSlot[][] slots;
    private final BiMap<String, Vector2i> slotIdToPosition;

    public SimpleCustomInventoryDefinition(PluginContainer pluginContainer, String typeId, @NonNull CustomSlot[][] slots) {
        super(pluginContainer, typeId);
        Preconditions.checkArgument(slots.length > 0, "The slots array have a positive height.");

        ImmutableBiMap.Builder<String, Vector2i> slotIdToLocationBuilder = ImmutableBiMap.builder();

        for(int y = 0; y < slots.length; y++) {
            Preconditions.checkNotNull(slots[y], "slots");
            Preconditions.checkArgument(slots[y].length == INVENTORY_SLOTS_WIDTH, "The slots array must be " + INVENTORY_SLOTS_WIDTH + " items wide.");

            for(int x = 0; x < slots[y].length; x++) {
                CustomSlot slot = slots[y][x];
                Vector2i position = slot.getPosition();

                Preconditions.checkNotNull(slot, "The slots array must not contain null values.");
                Preconditions.checkArgument(position.getX() >= 0 && position.getX() < INVENTORY_SLOTS_WIDTH
                                && position.getY() >= 0 && position.getY() < slots.length,
                        "Position in slotIdToPosition map out of bounds: " + position);

                slot.getId().ifPresent(slotId ->
                        slotIdToLocationBuilder.put(slotId, position));
            }
        }

        this.slots = slots;
        this.slotIdToPosition = slotIdToLocationBuilder.build();
    }

    @Override
    public SimpleCustomInventory create(DataHolder dataHolder) {
        SimpleCustomInventory result = new SimpleCustomInventory(this, dataHolder);
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
        populate(inventory);

        return result;
    }

    @Override
    public void populate(Inventory inventory) {
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

    public CustomSlot getCustomSlot(int x, int y) {
        return slots[y][x];
    }

    public CustomSlot getCustomSlot(Vector2i position) {
        return getCustomSlot(position.getX(), position.getY());
    }

    public Optional<CustomSlot> getCustomSlot(String slotId) {
        return getCustomSlotPosition(slotId).map(this::getCustomSlot);
    }

    public Optional<Vector2i> getCustomSlotPosition(String slotId) {
        return Optional.ofNullable(slotIdToPosition.get(slotId));
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

    @Override
    public Set<String> getAssets() {
        return getSlotStream()
                .flatMap(customSlot -> customSlot.getFeatures().values().stream())
                .map(GUIFeature::getModel)
                .map(GUIModel::getTextureAssetPath)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
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

    public static int getInventoryTextureWidth() {
        return INVENTORY_TEXTURE_PADDING_LEFT + INVENTORY_TEXTURE_SLOT_SIZE * INVENTORY_SLOTS_WIDTH
                + INVENTORY_TEXTURE_SLOT_GAP * (INVENTORY_SLOTS_WIDTH - 1) + INVENTORY_TEXTURE_PADDING_RIGHT;
    }

    public static int getInventoryTextureHeight(int rows) {
        return INVENTORY_TEXTURE_PADDING_TOP + INVENTORY_TEXTURE_SLOT_SIZE * rows
                + INVENTORY_TEXTURE_SLOT_GAP * (rows - 1) + INVENTORY_TEXTURE_PADDING_BOTTOM;
    }
}
