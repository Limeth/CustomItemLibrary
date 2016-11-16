package cz.creeper.customitemlibrary;

import com.google.common.base.Preconditions;
import lombok.*;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.util.Arrays;

/**
 * Defines a custom item.
 * This class is immutable and only a single instance should be created for each custom item type.
 *
 * The item is represented as retextured shears, where a specific durability value
 * signifies a specific texture of the {@link CustomToolDefinition}.
 *
 * Note: Shears cannot be stacked together.
 */
@ConfigSerializable
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CustomToolDefinition implements CustomItemDefinition<CustomTool> {
    @Getter
    @NonNull
    @Setting("pluginId")
    private final String pluginId;

    @Getter
    @NonNull
    @Setting("typeId")
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
    @Setting("textures")
    private final String[] textures;

    public CustomToolDefinition create(PluginContainer pluginContainer, String typeId, String[] textures) {
        Preconditions.checkArgument(textures.length > 0, "At least one texture must be specified.");
        Arrays.stream(textures).forEach(texture ->
                Preconditions.checkNotNull(texture, "The texture array must not contain null values."));

        return new CustomToolDefinition(pluginContainer.getId(), typeId, textures);
    }

    public CustomToolDefinition create(Object pluginInstance, String typeId, String[] textures) {
        PluginContainer pluginContainer = Sponge.getPluginManager().fromInstance(pluginInstance)
            .orElseThrow(() -> new IllegalArgumentException("Invalid plugin instance."));

        return create(pluginContainer, typeId, textures);
    }

    @Override
    public CustomTool createItem(Cause cause) {
        CustomToolRegistry registry = CustomToolRegistry.getInstance();
        ItemStack itemStack = ItemStack.of(getItemType(), 1);
        int defaultDurability = registry.getDurability(textures[0])
                .orElseThrow(() -> new IllegalStateException("Could not get the durability for the default texture."));

        itemStack.offer(Keys.ITEM_DURABILITY, defaultDurability);
        itemStack.offer(Keys.UNBREAKABLE, true);
        itemStack.offer(Keys.HIDE_UNBREAKABLE, true);
        itemStack.offer(Keys.HIDE_ATTRIBUTES, true);
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(getId()));

        CustomTool tool = new CustomTool(itemStack, this);
        CustomItemCreationEvent event = new CustomItemCreationEvent(cause, tool);

        Sponge.getEventManager().post(event);

        return tool;
    }

    public static ItemType getItemType() {
        return ItemTypes.SHEARS;
    }
}
