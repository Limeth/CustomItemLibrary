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
import java.util.Optional;
import java.util.Set;

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
 * and has to make use of the {@link #models} instead.
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

    @Getter
    @NonNull
    private final String defaultModel;

    @Getter
    @NonNull
    private final Set<String> models;

    public static CustomMaterialDefinition create(Object plugin, String typeId, ItemStackSnapshot itemStackSnapshot, String defaultModel, Collection<String> additionalModels) {
        PluginContainer pluginContainer = Sponge.getPluginManager().fromInstance(plugin)
                .orElseThrow(() -> new IllegalArgumentException("Invalid plugin instance."));
        Preconditions.checkArgument(itemStackSnapshot.getCount() == 1, "The ItemStack count must be equal to 1.");
        Preconditions.checkArgument(itemStackSnapshot.getType() == ItemTypes.SKULL, "The ItemStack must be a skull.");
        Preconditions.checkNotNull(defaultModel, "The default model must not be null.");
        additionalModels.forEach(model ->
                Preconditions.checkNotNull(model, "The model array must not contain null values."));
        Set<String> modelSet = ImmutableSet.<String>builder()
                .add(defaultModel)
                .addAll(additionalModels)
                .build();

        return new CustomMaterialDefinition(pluginContainer, typeId, itemStackSnapshot, defaultModel, modelSet);
    }

    @Override
    public CustomMaterial createItem(Cause cause) {
        CustomMaterialRegistry registry = CustomMaterialRegistry.getInstance();
        ItemStack itemStack = itemStackSnapshot.createStack();
        String defaultModelId = Util.getId(getPluginId(), defaultModel);
        SkinRecord skin = registry.getSkin(defaultModelId)
                .orElseThrow(() -> new IllegalStateException("Models are not prepared."));

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

    public static Path getTexturePath(PluginContainer pluginContainer, String model) {
        return Paths.get(pluginContainer.getId(), pluginContainer.getVersion().orElse("unknown"), "models", model + ".png");
    }

    public static String getTextureAsset(String model) {
        return "textures/materials/" + model + ".png";
    }
}
