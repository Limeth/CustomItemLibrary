package cz.creeper.customitemlibrary.registry;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import cz.creeper.customitemlibrary.CustomItemCreationEvent;
import cz.creeper.customitemlibrary.CustomTool;
import cz.creeper.customitemlibrary.data.CustomItemData;
import lombok.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.item.UseLimitProperty;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Defines a custom item.
 * This class is immutable and only a single instance should be created for each custom item type.
 *
 * The item is represented as retextured shears, where a specific durability value
 * signifies a specific texture of the {@link CustomToolDefinition}.
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

    /**
     * The Asset API is used to access the item textures.
     * The path to the texture file in a JAR is the following:
     * `assets/<pluginId>/textures/tools/<texture>.png`
     *
     * Must be lower-case, separate words with an underscore.
     */
    @Getter
    @NonNull
    private final List<String> textures;

    public static CustomToolDefinition create(PluginContainer pluginContainer, String typeId, Collection<String> textures) {
        Preconditions.checkArgument(!textures.isEmpty(), "At least one texture must be specified.");
        textures.forEach(texture ->
                Preconditions.checkNotNull(texture, "The texture array must not contain null values."));

        return new CustomToolDefinition(pluginContainer.getId(), typeId, Lists.newArrayList(textures));
    }

    public static CustomToolDefinition create(Object pluginInstance, String typeId, Collection<String> textures) {
        PluginContainer pluginContainer = Sponge.getPluginManager().fromInstance(pluginInstance)
            .orElseThrow(() -> new IllegalArgumentException("Invalid plugin instance."));

        return create(pluginContainer, typeId, textures);
    }

    @Override
    public CustomTool createItem(Cause cause) {
        PluginContainer plugin = getPlugin()
                .orElseThrow(() -> new IllegalStateException("Could not access the plugin owning this custom tool: "
                                                             + getPluginId()));
        CustomToolRegistry registry = CustomToolRegistry.getInstance();
        ItemStack itemStack = ItemStack.of(getItemType(), 1);
        int defaultDurability = registry.getDurability(plugin, textures.get(0))
                .orElseThrow(() -> new IllegalStateException("Could not get the durability for the default texture."));

        itemStack.offer(Keys.UNBREAKABLE, true);
        itemStack.offer(Keys.ITEM_DURABILITY, defaultDurability);
        itemStack.offer(Keys.HIDE_UNBREAKABLE, true);
        itemStack.offer(Keys.HIDE_ATTRIBUTES, true);
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(getId()));
        itemStack.offer(new CustomItemData(getId()));

        CustomTool tool = new CustomTool(itemStack, this);
        CustomItemCreationEvent event = new CustomItemCreationEvent(cause, tool);

        Sponge.getEventManager().post(event);

        return tool;
    }

    @Override
    public Optional<CustomTool> wrapIfPossible(ItemStack itemStack) {
        if(itemStack.getItem() != CustomToolDefinition.getItemType())
            return Optional.empty();

        return Optional.of(new CustomTool(itemStack, this));
    }

    /**
     * @return A list of "<pluginId>:<texture>"
     */
    public List<String> getTextureIds() {
        return textures.stream()
                .map(texture -> pluginId + CustomItemDefinition.ID_SEPARATOR + texture)
                .collect(Collectors.toList());
    }

    public static ItemType getItemType() {
        return ItemTypes.SHEARS;
    }

    @SuppressWarnings("ConstantConditions")
    public static int getNumberOfUses() {
        return getItemType().getDefaultProperty(UseLimitProperty.class)
                .orElseThrow(() -> new IllegalStateException("Could not access the custom tool use limit property."))
                .getValue();
    }

    public static String getTexturePath(String texture) {
        return "textures/tools/" + texture + ".png";
    }

    public static String getAssetPrefix(PluginContainer plugin) {
        return getAssetPrefix(plugin.getId());
    }

    public static String getAssetPrefix(String pluginId) {
        return "assets/" + pluginId + "/";
    }
}
