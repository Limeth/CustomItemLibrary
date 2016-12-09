package cz.creeper.customitemlibrary.item.material;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import cz.creeper.customitemlibrary.data.CustomItemData;
import cz.creeper.customitemlibrary.events.CustomItemCreationEvent;
import cz.creeper.customitemlibrary.item.CustomItemDefinition;
import cz.creeper.customitemlibrary.item.tool.CustomToolDefinition;
import cz.creeper.customitemlibrary.util.Util;
import cz.creeper.mineskinsponge.SkinRecord;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
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
import java.util.Set;
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
     * The default texture of this custom item.
     */
    @Getter
    @NonNull
    private final String defaultTexture;

    /**
     * The Asset API is used to access the material textures.
     * The path to the texture file in a JAR is the following:
     * `assets/<pluginId>/textures/materials/<model>.png`
     *
     * Should be lower-case, separate words with an underscore.
     */
    @Getter
    @NonNull
    private final Set<String> textures;

    public static CustomMaterialDefinition create(Object plugin, String typeId, ItemStackSnapshot itemStackSnapshot, String defaultTexture, Collection<String> additionalTextures) {
        PluginContainer pluginContainer = Sponge.getPluginManager().fromInstance(plugin)
                .orElseThrow(() -> new IllegalArgumentException("Invalid plugin instance."));
        Preconditions.checkArgument(itemStackSnapshot.getCount() == 1, "The ItemStack count must be equal to 1.");
        Preconditions.checkArgument(itemStackSnapshot.getType() == ItemTypes.SKULL, "The ItemStack must be a skull.");
        Preconditions.checkNotNull(defaultTexture, "The default texture must not be null.");
        additionalTextures.forEach(model ->
                Preconditions.checkNotNull(model, "The texture array must not contain null values."));
        Set<String> textureSet = ImmutableSet.<String>builder()
                .add(defaultTexture)
                .addAll(additionalTextures)
                .build();

        return new CustomMaterialDefinition(pluginContainer, typeId, itemStackSnapshot, defaultTexture, textureSet);
    }

    @Override
    public CustomMaterial createItem(Cause cause) {
        CustomMaterialRegistry registry = CustomMaterialRegistry.getInstance();
        ItemStack itemStack = itemStackSnapshot.createStack();
        String defaultTextureId = Util.getId(getPluginId(), defaultTexture);
        SkinRecord skin = registry.getSkin(defaultTextureId)
                .orElseThrow(() -> new IllegalStateException("Textures are not prepared."));

        skin.apply(itemStack);
        itemStack.offer(new CustomItemData(getId()));

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
                .map(texture -> Util.getId(getPluginId(), texture))
                .collect(Collectors.toList());
    }

    public String getDefaultTextureId() {
        return Util.getId(pluginContainer.getId(), defaultTexture);
    }

    public static Path getTexturePath(PluginContainer pluginContainer, String texture) {
        return Paths.get(pluginContainer.getId(), pluginContainer.getVersion().orElse("unknown"), "textures", texture + ".png");
    }

    public static String getTextureAsset(String texture) {
        return "textures/materials/" + texture + ".png";
    }
}
