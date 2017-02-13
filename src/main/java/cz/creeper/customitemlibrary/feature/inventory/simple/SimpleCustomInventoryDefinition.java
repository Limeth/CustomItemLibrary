package cz.creeper.customitemlibrary.feature.inventory.simple;

import com.flowpowered.math.vector.Vector2i;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.data.mutable.CustomInventoriesData;
import cz.creeper.customitemlibrary.data.mutable.CustomInventoryData;
import cz.creeper.customitemlibrary.feature.inventory
        .AbstractCustomInventoryDefinition;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ToString
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
    private final CustomSlotDefinition[][] slots;
    private final BiMap<String, Vector2i> slotIdToPosition;

    public SimpleCustomInventoryDefinition(PluginContainer pluginContainer, String typeId, @NonNull CustomSlotDefinition[][] slots) {
        super(pluginContainer, typeId);
        Preconditions.checkArgument(slots.length > 0, "The slots array have a positive height.");

        ImmutableBiMap.Builder<String, Vector2i> slotIdToLocationBuilder = ImmutableBiMap.builder();

        for(int y = 0; y < slots.length; y++) {
            Preconditions.checkNotNull(slots[y], "slots");
            Preconditions.checkArgument(slots[y].length == INVENTORY_SLOTS_WIDTH, "The slots array must be " + INVENTORY_SLOTS_WIDTH + " items wide.");

            for(int x = 0; x < slots[y].length; x++) {
                CustomSlotDefinition slot = slots[y][x];
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
        populate(result);

        return result;
    }

    private void populate(SimpleCustomInventory inventory) {
        CustomInventoryData data = inventory.getCustomInventoryData();
        val slotIdToItemStack = data.getSlotIdToItemStack();

        inventory.customSlots().forEach(customSlot -> {
            ItemStack itemStack = customSlot.getDefinition().getId()
                    .flatMap(slotId -> Optional.ofNullable(slotIdToItemStack.get(slotId)))
                    .map(ItemStackSnapshot::createStack)
                    .orElseGet(() -> customSlot.getDefinition().createDefaultItemStack());

            customSlot.setItemStack(itemStack);
        });

        val slotIdToFeatureId = data.getSlotIdToFeatureId();

        slotIdToFeatureId.forEach((slotId, featureId) ->
                inventory.getCustomSlot(slotId).ifPresent(customSlot ->
                        customSlot.setFeature(featureId)
                )
        );
    }

    public CustomSlotDefinition getCustomSlotDefinition(int x, int y) {
        return slots[y][x];
    }

    public CustomSlotDefinition getCustomSlotDefinition(Vector2i position) {
        return getCustomSlotDefinition(position.getX(), position.getY());
    }

    public Optional<CustomSlotDefinition> getCustomSlotDefinition(String slotId) {
        return getCustomSlotPosition(slotId).map(this::getCustomSlotDefinition);
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

    public CustomSlotDefinition[][] getSlots() {
        CustomSlotDefinition[][] result = new CustomSlotDefinition[getHeight()][INVENTORY_SLOTS_WIDTH];

        for(int y = 0; y < getHeight(); y++) {
            System.arraycopy(slots[y], 0, result[y], 0, INVENTORY_SLOTS_WIDTH);
        }

        return result;
    }

    public Stream<CustomSlotDefinition> getSlotStream() {
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

    public CustomInventoryData getCustomInventoryData(DataHolder dataHolder, Function<DataHolder, CustomInventoryData> putIfAbsent) {
        CustomInventoriesData customInventoriesData = getCustomInventoriesData(dataHolder);
        String id = getTypeId();

        return customInventoriesData.get(id)
                .orElseGet(() -> {
                    CustomInventoryData result = putIfAbsent.apply(dataHolder);

                    setCustomInventoryData(dataHolder, result);

                    return result;
                });
    }

    public CustomInventoryData getCustomInventoryData(DataHolder dataHolder) {
        return getCustomInventoryData(dataHolder, _dataHolder -> CustomInventoryData.empty(this));
    }

    public void setCustomInventoryData(DataHolder dataHolder, CustomInventoryData data) {
        CustomInventoriesData customInventoriesData = getCustomInventoriesData(dataHolder);
        String id = getTypeId();

        customInventoriesData.put(id, data);
        dataHolder.offer(customInventoriesData);
    }

    public CustomInventoriesData getCustomInventoriesData(DataHolder dataHolder) {
        return dataHolder.get(CustomInventoriesData.class)
                .orElseGet(() -> {
                    CustomInventoriesData result = new CustomInventoriesData();

                    dataHolder.offer(result);

                    return result;
                });
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
