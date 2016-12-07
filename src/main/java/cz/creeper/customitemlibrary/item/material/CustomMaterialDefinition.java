package cz.creeper.customitemlibrary.item.material;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import cz.creeper.customitemlibrary.events.CustomItemCreationEvent;
import cz.creeper.customitemlibrary.item.CustomItemDefinition;
import cz.creeper.customitemlibrary.item.tool.CustomToolDefinition;
import cz.creeper.mineskinsponge.SkinRecord;
import lombok.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.PluginContainer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Defines a custom material.
 * This class is immutable and only a single instance should be created for each custom item type.
 *
 * The item is represented as a player head with a custom skin.
 * The textures are sent to Mojang via the Mineskin service, where they are signed and sent back.
 *
 * The advantage of using this class is,
 * that these items can be stacked together (in contrast with {@link CustomToolDefinition}).
 * The disadvantage, on the other hand, is, that this item cannot have custom models assigned to it
 * and has to make use of the {@link #textures} instead.
 */
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class CustomMaterialDefinition implements CustomItemDefinition<CustomMaterial> {
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
     * The Asset API is used to access the material textures.
     * The path to the texture file in a JAR is the following:
     * `assets/<pluginId>/textures/materials/<model>.png`
     *
     * Should be lower-case, separate words with an underscore.
     */
    @Getter
    @NonNull
    private final List<String> textures;

    public static CustomMaterialDefinition create(Object plugin, String typeId, ItemStackSnapshot itemStackSnapshot, Collection<String> textures) {
        PluginContainer pluginContainer = Sponge.getPluginManager().fromInstance(plugin)
                .orElseThrow(() -> new IllegalArgumentException("Invalid plugin instance."));
        Preconditions.checkArgument(!textures.isEmpty(), "At least one texture must be specified.");
        textures.forEach(model ->
                Preconditions.checkNotNull(model, "The texture array must not contain null values."));
        Preconditions.checkArgument(itemStackSnapshot.getCount() == 1, "The ItemStack count must be equal to 1.");
        Preconditions.checkArgument(itemStackSnapshot.getType() == ItemTypes.SKULL, "The ItemStack must be a skull.");

        return new CustomMaterialDefinition(pluginContainer, typeId, itemStackSnapshot, Lists.newArrayList(textures));
    }

    @Override
    public CustomMaterial createItem(Cause cause) {
        PluginContainer plugin = getPlugin()
                .orElseThrow(() -> new IllegalStateException("Could not access the plugin owning this custom tool: "
                        + getPluginId()));
        CustomMaterialRegistry registry = CustomMaterialRegistry.getInstance();
        ItemStack itemStack = itemStackSnapshot.createStack();
        String defaultTexture = textures.get(0);
        SkinRecord skin = registry.getSkin(defaultTexture);

        skin.apply(itemStack);

        CustomMaterial material = new CustomMaterial(itemStack, this);
        CustomItemCreationEvent event = new CustomItemCreationEvent(cause, material);

        Sponge.getEventManager().post(event);

        return material;
    }

    @Override
    public Optional<CustomMaterial> wrapIfPossible(ItemStack itemStack) {
        if(itemStack.getItem() != ItemTypes.SKULL)
            return Optional.empty();

        return Optional.of(new CustomMaterial(itemStack, this));
    }

    @Override
    public String getPluginId() {
        return pluginContainer.getId();
    }

    public List<String> getTextureIds() {
        return textures.stream()
                .map(texture -> getTextureId(getPluginId(), texture))
                .collect(Collectors.toList());
    }

    public static String getTextureId(String pluginId, String texture) {
        return pluginId + CustomItemDefinition.ID_SEPARATOR + texture;
    }

    public static String getPluginId(String textureId) {
        return textureId.substring(0, textureId.indexOf(CustomItemDefinition.ID_SEPARATOR));
    }

    public static PluginContainer getPlugin(String textureId) {
        return Sponge.getPluginManager().getPlugin(getPluginId(textureId))
                .orElseThrow(() -> new IllegalStateException("Could not access the owning plugin of texture: " + textureId));
    }

    public static String getTexture(String textureId) {
        return textureId.substring(textureId.indexOf(CustomItemDefinition.ID_SEPARATOR) + 1);
    }

    public static Path getTexturePath(PluginContainer pluginContainer, String texture) {
        return Paths.get(pluginContainer.getId(), pluginContainer.getVersion().orElse("unknown"), "textures", texture + ".png");
    }

    public static String getTextureAsset(String texture) {
        return "textures/materials/" + texture + ".png";
    }
}
