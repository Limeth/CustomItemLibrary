package cz.creeper.customitemlibrary.feature.item.tool;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import cz.creeper.customitemlibrary.event.CustomItemCreationEvent;
import cz.creeper.customitemlibrary.feature.DurabilityRegistry;
import cz.creeper.customitemlibrary.feature.item.AbstractCustomItemDefinition;
import cz.creeper.customitemlibrary.feature.item.DefinesDurabilityModels;
import cz.creeper.customitemlibrary.util.Util;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Defines a custom feature.
 * This class is immutable and only a single instance should be created for each custom feature type.
 *
 * The feature is represented as remodelled shears, where a specific durability value
 * signifies a specific model of the {@link CustomToolDefinition}.
 *
 * Note: Shears cannot be stacked together.
 */
@EqualsAndHashCode(callSuper = true)
@ToString
public final class CustomToolDefinition extends AbstractCustomItemDefinition<CustomTool> implements DefinesDurabilityModels {
    public static final String MODEL_DIRECTORY_NAME = "tools";

    @Getter
    @NonNull
    private final ItemStackSnapshot itemStackSnapshot;

    /**
     * A list of assets to be copied to the resourcepack.
     * Should be located at `assets/<pluginId>/<asset>` in the JAR.
     */
    @Getter
    @NonNull
    private final ImmutableSet<String> assets;

    private CustomToolDefinition(PluginContainer pluginContainer, String typeId, String defaultModel,
                                Iterable<String> models, @NonNull ItemStackSnapshot itemStackSnapshot, Iterable<String> assets) {
        super(pluginContainer, typeId, defaultModel, models);

        this.itemStackSnapshot = itemStackSnapshot;
        this.assets = ImmutableSet.<String>builder()
                .addAll(getModels().stream()
                        .map(CustomToolDefinition::getModelPath)
                        .collect(Collectors.toSet()))
                .addAll(Util.removeNull(assets).collect(Collectors.toSet()))
                .build();
    }

    @Builder
    public static CustomToolDefinition create(Object plugin, String typeId, @NonNull ItemStackSnapshot itemStackSnapshot, String defaultModel, @Singular Collection<String> additionalModels, @Singular Collection<String> assets) {
        PluginContainer pluginContainer = Sponge.getPluginManager().fromInstance(plugin)
                .orElseThrow(() -> new IllegalArgumentException("Invalid plugin instance."));
        Preconditions.checkArgument(itemStackSnapshot.getCount() == 1, "The ItemStack count must be equal to 1.");
        Preconditions.checkArgument(getNumberOfUses(itemStackSnapshot.createStack()).isPresent(), "Invalid feature type, the feature must have a durability.");

        return new CustomToolDefinition(pluginContainer, typeId, defaultModel, additionalModels, itemStackSnapshot, assets);
    }

    @Override
    public CustomTool createItem(Cause cause) {
        PluginContainer plugin = getPluginContainer();
        CustomToolRegistry registry = CustomToolRegistry.getInstance();
        ItemStack itemStack = itemStackSnapshot.createStack();
        ItemType itemType = itemStack.getItem();
        int durability = DurabilityRegistry.getInstance().getDurability(itemType, plugin, getDefaultModel())
                .orElseThrow(() -> new IllegalStateException("Could not get the durability for the default models."));

        itemStack.offer(Keys.UNBREAKABLE, true);
        itemStack.offer(Keys.ITEM_DURABILITY, durability);
        itemStack.offer(Keys.HIDE_UNBREAKABLE, true);
        itemStack.offer(Keys.HIDE_ATTRIBUTES, true);
        itemStack.offer(createDefaultCustomFeatureData());

        CustomTool tool = new CustomTool(itemStack, this);
        CustomItemCreationEvent event = new CustomItemCreationEvent(tool, cause);

        Sponge.getEventManager().post(event);

        return tool;
    }

    @Override
    public Optional<CustomTool> wrapIfPossible(ItemStack itemStack) {
        if(itemStack.getItem() != itemStackSnapshot.getType())
            return Optional.empty();

        int durability = itemStack.get(Keys.ITEM_DURABILITY)
                .orElseThrow(() -> new IllegalStateException("Could not access the durability of a tool."));

        if(!DurabilityRegistry.getInstance().getModelId(itemStack.getItem(), durability).isPresent())
            return Optional.empty();

        return Optional.of(new CustomTool(itemStack, this));
    }

    public static Optional<Integer> getNumberOfUses(ItemType itemType) {
        return getNumberOfUses(itemType.getTemplate());
    }

    public static Optional<Integer> getNumberOfUses(ItemStackSnapshot itemStack) {
        return itemStack.getProperty(UseLimitProperty.class)
                .map(Property::getValue);
    }

    public static Optional<Integer> getNumberOfUses(ItemStack itemStack) {
        return itemStack.getProperty(UseLimitProperty.class)
                .map(Property::getValue);
    }

    public int getNumberOfUses() {
        return getNumberOfUses(itemStackSnapshot)
                .orElseThrow(() -> new IllegalStateException("Could not access the custom tool use limit property."));
    }

    @Override
    public String getModelDirectoryName() {
        return MODEL_DIRECTORY_NAME;
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
