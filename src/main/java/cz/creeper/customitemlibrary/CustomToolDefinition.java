package cz.creeper.customitemlibrary;

import com.google.common.base.Preconditions;
import lombok.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.util.Arrays;
import java.util.function.Consumer;

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
public class CustomToolDefinition implements CustomItemDefinition {
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
    private final String[] textures;

    @Getter
    @NonNull
    private final String displayName;

    @Getter
    private final Consumer<CustomTool> onCreate;

    public CustomToolDefinition create(PluginContainer pluginContainer, String typeId, String[] textures,
                                       String displayName, Consumer<CustomTool> onCreate) {
        Preconditions.checkArgument(textures.length > 0, "At least one texture must be specified.");
        Arrays.stream(textures).forEach(texture ->
                Preconditions.checkNotNull(texture, "The texture array must not contain null values."));

        return new CustomToolDefinition(pluginContainer.getId(), typeId, textures, displayName, onCreate);
    }

    public CustomToolDefinition create(Object pluginInstance, String typeId, String[] textures, String displayName,
                                       Consumer<CustomTool> onCreate) {
        PluginContainer pluginContainer = Sponge.getPluginManager().fromInstance(pluginInstance)
            .orElseThrow(() -> new IllegalArgumentException("Invalid plugin instance."));

        return create(pluginContainer, typeId, textures, displayName, onCreate);
    }

    @Override
    public CustomTool createItem() {
        ItemStack itemStack = ItemStack.of(ItemTypes.SHEARS, 1);

        itemStack.offer(Keys.DISPLAY_NAME, Text.of(displayName));

        CustomTool tool = new CustomTool(itemStack, this);

        if(onCreate != null)
            onCreate.accept(tool);

        return tool;
    }
}
