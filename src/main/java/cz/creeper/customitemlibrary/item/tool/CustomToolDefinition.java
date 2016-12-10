package cz.creeper.customitemlibrary.item.tool;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import cz.creeper.customitemlibrary.data.CustomItemData;
import cz.creeper.customitemlibrary.events.CustomItemCreationEvent;
import cz.creeper.customitemlibrary.item.CustomItemDefinition;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.item.UseLimitProperty;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Defines a custom item.
 * This class is immutable and only a single instance should be created for each custom item type.
 *
 * The item is represented as remodelled shears, where a specific durability value
 * signifies a specific model of the {@link CustomToolDefinition}.
 *
 * Note: Shears cannot be stacked together.
 */
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public final class CustomToolDefinition implements CustomItemDefinition<CustomTool> {
    @Getter
    @NonNull
    private final PluginContainer pluginContainer;

    @Getter
    @NonNull
    private final String typeId;

    @Getter
    @NonNull
    private final ItemStackSnapshot itemStackSnapshot;

    /**
     * The default model of this custom item.
     */
    @Getter
    @NonNull
    private final String defaultModel;

    /**
     * The Asset API is used to access the item models.
     * The path to the model file in a JAR is the following:
     * `assets/<pluginId>/models/tools/<model>.png`
     *
     * Must be lower-case, separate words with an underscore.
     */
    @Getter
    @NonNull
    private final Set<String> models;

    /**
     * A list of additional assets to be copied to the resourcepack.
     * Should be located at `assets/<pluginId>/<asset>` in the JAR.
     */
    @Getter
    @NonNull
    private final List<String> assets;

    public static CustomToolDefinition create(Object plugin, String typeId, ItemStackSnapshot itemStackSnapshot, String defaultModel, Collection<String> additionalModels, Collection<String> assets) {
        PluginContainer pluginContainer = Sponge.getPluginManager().fromInstance(plugin)
                .orElseThrow(() -> new IllegalArgumentException("Invalid plugin instance."));
        Preconditions.checkArgument(itemStackSnapshot.getCount() == 1, "The ItemStack count must be equal to 1.");
        Preconditions.checkArgument(getNumberOfUses(itemStackSnapshot.createStack()).isPresent(), "Invalid item type, the item must have a durability.");
        Preconditions.checkNotNull(defaultModel, "The default model must not be null.");
        additionalModels.forEach(model ->
                Preconditions.checkNotNull(model, "The model array must not contain null values."));
        Set<String> modelSet = ImmutableSet.<String>builder()
                .add(defaultModel)
                .addAll(additionalModels)
                .build();

        return new CustomToolDefinition(pluginContainer, typeId, itemStackSnapshot, defaultModel, modelSet, assets == null ? ImmutableList.of() : ImmutableList.copyOf(assets));
    }

    @Override
    public CustomTool createItem(Cause cause) {
        PluginContainer plugin = getPluginContainer();
        CustomToolRegistry registry = CustomToolRegistry.getInstance();
        ItemStack itemStack = itemStackSnapshot.createStack();
        ItemType itemType = itemStack.getItem();
        int durability = registry.getDurability(itemType, plugin, defaultModel)
                .orElseThrow(() -> new IllegalStateException("Could not get the durability for the default models."));

        itemStack.offer(Keys.UNBREAKABLE, true);
        itemStack.offer(Keys.ITEM_DURABILITY, durability);
        itemStack.offer(Keys.HIDE_UNBREAKABLE, true);
        itemStack.offer(Keys.HIDE_ATTRIBUTES, true);
        itemStack.offer(new CustomItemData(getId()));

        CustomTool tool = new CustomTool(itemStack, this);
        CustomItemCreationEvent event = new CustomItemCreationEvent(cause, tool);

        Sponge.getEventManager().post(event);

        return tool;
    }

    @Override
    public Optional<CustomTool> wrapIfPossible(ItemStack itemStack) {
        if(itemStack.getItem() != itemStackSnapshot.getType())
            return Optional.empty();

        int durability = itemStack.get(Keys.ITEM_DURABILITY)
                .orElseThrow(() -> new IllegalStateException("Could not access the durability of a tool."));

        if(!CustomToolRegistry.getInstance().getModelId(itemStack.getItem(), durability).isPresent())
            return Optional.empty();

        return Optional.of(new CustomTool(itemStack, this));
    }

    public static Optional<Integer> getNumberOfUses(ItemType itemType) {
        return getNumberOfUses(itemType.getTemplate());
    }

    @SuppressWarnings("ConstantConditions")
    public static Optional<Integer> getNumberOfUses(ItemStackSnapshot itemStack) {
        return itemStack.getProperty(UseLimitProperty.class)
                .map(Property::getValue);
    }

    @SuppressWarnings("ConstantConditions")
    public static Optional<Integer> getNumberOfUses(ItemStack itemStack) {
        return itemStack.getProperty(UseLimitProperty.class)
                .map(Property::getValue);
    }

    public int getNumberOfUses() {
        return getNumberOfUses(itemStackSnapshot)
                .orElseThrow(() -> new IllegalStateException("Could not access the custom tool use limit property."));
    }

    public static String getModelPath(String model) {
        return "models/tools/" + model + ".json";
    }

    public static String getAssetPrefix(PluginContainer plugin) {
        return getAssetPrefix(plugin.getId());
    }

    public static String getAssetPrefix(String pluginId) {
        return "assets/" + pluginId + "/";
    }
}
