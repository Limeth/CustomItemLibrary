package cz.creeper.customitemlibrary.feature.item.material;

import com.google.common.base.Preconditions;
import cz.creeper.customitemlibrary.event.CustomItemCreationEvent;
import cz.creeper.customitemlibrary.feature.item.AbstractCustomItemDefinition;
import cz.creeper.customitemlibrary.feature.item.tool.CustomToolDefinition;
import cz.creeper.mineskinsponge.SkinRecord;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.ToString;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Collection;
import java.util.Optional;

/**
 * Defines a custom material.
 * This class is immutable and only a single instance should be created for each custom feature type.
 *
 * The feature is represented as a player head with a custom skin.
 * The textures are sent to Mojang via the Mineskin service, where they are signed and sent back.
 *
 * The advantage of using this class is,
 * that these items can be stacked together (in contrast with {@link CustomToolDefinition}).
 * The disadvantage, on the other hand, is, that this feature cannot have custom models assigned to it
 * and has to make use of the {@link #models} instead.
 */
@EqualsAndHashCode(callSuper = true)
@ToString
public class CustomMaterialDefinition extends AbstractCustomItemDefinition<CustomMaterial> {
    @Getter
    @NonNull
    private final ItemStackSnapshot itemStackSnapshot;

    public CustomMaterialDefinition(PluginContainer pluginContainer, String typeId, String defaultModel, Iterable<String> models, @NonNull ItemStackSnapshot itemStackSnapshot) {
        super(pluginContainer, typeId, defaultModel, models);

        this.itemStackSnapshot = itemStackSnapshot;
    }

    @Builder
    public static CustomMaterialDefinition create(Object plugin, String typeId, ItemStackSnapshot itemStackSnapshot, String defaultModel, @Singular Collection<String> additionalModels) {
        PluginContainer pluginContainer = Sponge.getPluginManager().fromInstance(plugin)
                .orElseThrow(() -> new IllegalArgumentException("Invalid plugin instance."));
        Preconditions.checkArgument(itemStackSnapshot.getCount() == 1, "The ItemStack count must be equal to 1.");
        Preconditions.checkArgument(itemStackSnapshot.getType() == ItemTypes.SKULL, "The ItemStack must be a skull.");

        return new CustomMaterialDefinition(pluginContainer, typeId, defaultModel, additionalModels, itemStackSnapshot);
    }

    @Override
    public CustomMaterial createItem(Cause cause) {
        CustomMaterialRegistry registry = CustomMaterialRegistry.getInstance();
        ItemStack itemStack = itemStackSnapshot.createStack();
        SkinRecord skin = registry.getSkin(getPluginContainer(), getDefaultModel())
                .orElseThrow(() -> new IllegalStateException("Models are not prepared."));

        skin.apply(itemStack);
        itemStack.offer(createDefaultCustomFeatureData());

        CustomMaterial material = new CustomMaterial(itemStack, this);
        CustomItemCreationEvent event = new CustomItemCreationEvent(material, cause);

        Sponge.getEventManager().post(event);

        return material;
    }

    @Override
    public Optional<CustomMaterial> wrapIfPossible(ItemStack itemStack) {
        if(itemStack.getItem() != ItemTypes.SKULL)
            return Optional.empty();

        return Optional.of(new CustomMaterial(itemStack, this));
    }

    public static String getTextureAsset(String model) {
        return "textures/materials/" + model + ".png";
    }
}
