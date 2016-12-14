package cz.creeper.customitemlibrary.feature.block;

import com.google.common.collect.ImmutableSet;
import cz.creeper.customitemlibrary.data.CustomFeatureData;
import cz.creeper.customitemlibrary.feature.CustomFeatureDefinition;
import cz.creeper.customitemlibrary.feature.item.CustomItem;
import cz.creeper.customitemlibrary.feature.item.CustomItemDefinition;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;

import java.util.Optional;
import java.util.Set;

/**
 * Apply to models:

 "display": {
   "head": {
   "translation": [0, -43.225, 0],
   "scale": [1.6, 1.6, 1.6]
   }
 }
 */
public interface CustomBlockDefinition<T extends CustomBlock<? extends CustomBlockDefinition<T>>> extends CustomFeatureDefinition<T> {
    /**
     * @return The owner plugin
     */
    PluginContainer getPluginContainer();

    /**
     * @return The associated plugin instance, if available
     */
    default Object getPlugin() {
        return getPluginContainer().getInstance()
                .orElseThrow(() -> new IllegalStateException("Could not access the plugin instance."));
    }

    /**
     * The string uniquely identifying this feature type.
     * The latter part of `<pluginId>:<typeId>`.
     *
     * Must be lower-case, separate words with an underscore.
     */
    String getTypeId();

    default Set<String> getAssets() {
        return ImmutableSet.of();
    }

    /**
     * @return A {@link CustomItem} with default properties.
     */
    T createItem(Cause cause);

    /**
     * If the definition supports it, places a block and returns the definition.
     *
     * @return A {@link CustomItem} representing the placed block.
     */
    default Optional<T> placeBlock(Location location, Cause cause) {
        return Optional.empty();
    }

    /**
     * Wraps the {@link ItemStack} in a helper class extending {@link CustomItem},
     * if the {@link ItemStack} is representing an actual custom feature
     * created by the {@link CustomItemDefinition#createItem(Cause)} method.
     *
     * @param itemStack The {@link ItemStack} to wrap
     * @return The wrapped {@link ItemStack}, if the feature actually represents this definition
     */
    Optional<T> wrapIfPossible(ItemStack itemStack);

    /**
     * Wraps the {@link Location} in a helper class extending {@link CustomItem},
     * if the {@link Location} is representing an actual custom feature
     * created by the {@link CustomItemDefinition#placeBlock(Location, Cause)} method.
     *
     * @param block The block to wrap
     * @return The wrapped {@link ItemStack}, if the feature actually represents this definition
     */
    default <E extends Extent> Optional<T> wrapIfPossible(Location<E> block) {
        return Optional.empty();
    }

    default CustomFeatureData createDefaultCustomItemData() {
        return new CustomFeatureData(getPluginContainer().getId(), getTypeId(), getDefaultModel());
    }
}
