package cz.creeper.customitemlibrary.registry;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import cz.creeper.customitemlibrary.CustomItemCreationEvent;
import cz.creeper.customitemlibrary.CustomTool;
import cz.creeper.customitemlibrary.data.CustomItemData;
import lombok.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final String pluginId;

    @Getter
    @NonNull
    private final String typeId;

    @Getter
    @NonNull
    private final String displayName;

    /**
     * The Asset API is used to access the item models.
     * The path to the model file in a JAR is the following:
     * `assets/<pluginId>/models/tools/<model>.png`
     *
     * Must be lower-case, separate words with an underscore.
     */
    @Getter
    @NonNull
    private final List<String> models;

    /**
     * A list of additional assets to be copied to the resourcepack.
     * Should be located at `assets/<pluginId>/<asset>` in the JAR.
     */
    @Getter
    @NonNull
    private final List<String> assets;

    public static CustomToolDefinition create(PluginContainer pluginContainer, String typeId, String displayName, Collection<String> models, Collection<String> assets) {
        Preconditions.checkArgument(!models.isEmpty(), "At least one model must be specified.");
        models.forEach(model ->
                Preconditions.checkNotNull(model, "The model array must not contain null values."));

        return new CustomToolDefinition(pluginContainer.getId(), typeId, displayName, Lists.newArrayList(models), assets == null ? Lists.newArrayList() : Lists.newArrayList(assets));
    }

    public static CustomToolDefinition create(Object pluginInstance, String typeId, String displayName, Collection<String> models, Collection<String> assets) {
        PluginContainer pluginContainer = Sponge.getPluginManager().fromInstance(pluginInstance)
            .orElseThrow(() -> new IllegalArgumentException("Invalid plugin instance."));

        return create(pluginContainer, typeId, displayName, models, assets);
    }

    @Override
    public CustomTool createItem(Cause cause) {
        PluginContainer plugin = getPlugin()
                .orElseThrow(() -> new IllegalStateException("Could not access the plugin owning this custom tool: "
                                                             + getPluginId()));
        CustomToolRegistry registry = CustomToolRegistry.getInstance();
        ItemStack itemStack = ItemStack.of(getItemType(), 1);
        int defaultDurability = registry.getDurability(plugin, models.get(0))
                .orElseThrow(() -> new IllegalStateException("Could not get the durability for the default models."));

        itemStack.offer(Keys.UNBREAKABLE, true);
        itemStack.offer(Keys.ITEM_DURABILITY, defaultDurability);
        itemStack.offer(Keys.HIDE_UNBREAKABLE, true);
        itemStack.offer(Keys.HIDE_ATTRIBUTES, true);
        itemStack.offer(new CustomItemData(getId()));

        updateItemStack(itemStack);

        CustomTool tool = new CustomTool(itemStack, this);
        CustomItemCreationEvent event = new CustomItemCreationEvent(cause, tool);

        Sponge.getEventManager().post(event);

        return tool;
    }

    @Override
    public Optional<CustomTool> wrapIfPossible(ItemStack itemStack) {
        if(itemStack.getItem() != CustomToolDefinition.getItemType())
            return Optional.empty();

        int durability = itemStack.get(Keys.ITEM_DURABILITY)
                .orElseThrow(() -> new IllegalStateException("Could not access the durability of a tool."));

        if(!CustomToolRegistry.getInstance().getModelId(durability).isPresent())
            return Optional.empty();

        updateItemStack(itemStack);

        return Optional.of(new CustomTool(itemStack, this));
    }

    public void updateItemStack(ItemStack itemStack) {
        /* TODO: This currently does not work, due to Text returning invalid colors
        Optional<Text> displayName = itemStack.get(Keys.DISPLAY_NAME);
        Text originalNameText = getDisplayNameText();
        boolean isOriginalName = displayName
                .map(name -> name.equals(originalNameText))
                .orElse(false);

        if(!isOriginalName) {
            boolean isPlayerRenamed = displayName
                    .map(name -> !TextColors.RESET.equals(name.getColor()))
                    .orElse(false);

            if(!isPlayerRenamed) {
                itemStack.offer(Keys.DISPLAY_NAME, originalNameText);
            }
        }
        */
        itemStack.offer(Keys.DISPLAY_NAME, getDisplayNameText());
    }

    public Text getDisplayNameText() {
        return Text.builder().color(TextColors.RESET).append(Text.of(displayName)).build();
    }

    /**
     * @return A list of "<pluginId>:<model>"
     */
    public List<String> getModelIds() {
        return models.stream()
                .map(model -> pluginId + CustomItemDefinition.ID_SEPARATOR + model)
                .collect(Collectors.toList());
    }

    public static ItemType getItemType() {
        return ItemTypes.SHEARS;
    }

    @SuppressWarnings("ConstantConditions")
    public static int getNumberOfUses() {
        return 238;
        /*
        return ItemStack.of(getItemType(), 1).getProperty(UseLimitProperty.class)
                .orElseThrow(() -> new IllegalStateException("Could not access the custom tool use limit property."))
                .getValue();
                */
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
