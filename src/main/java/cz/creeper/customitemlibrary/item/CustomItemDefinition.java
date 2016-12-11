package cz.creeper.customitemlibrary.item;

import cz.creeper.customitemlibrary.data.CustomItemData;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;

import java.util.Optional;

public interface CustomItemDefinition<T extends CustomItem> extends DefinesModels {
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
     * The string uniquely identifying this item type.
     * The latter part of `<pluginId>:<typeId>`.
     *
     * Must be lower-case, separate words with an underscore.
     */
    String getTypeId();

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
     * if the {@link ItemStack} is representing an actual custom item
     * created by the {@link CustomItemDefinition#createItem(Cause)} method.
     *
     * @param itemStack The {@link ItemStack} to wrap
     * @return The wrapped {@link ItemStack}, if the item actually represents this definition
     */
    Optional<T> wrapIfPossible(ItemStack itemStack);

    /**
     * Wraps the {@link Location} in a helper class extending {@link CustomItem},
     * if the {@link Location} is representing an actual custom item
     * created by the {@link CustomItemDefinition#placeBlock(Location, Cause)} method.
     *
     * @param block The block to wrap
     * @return The wrapped {@link ItemStack}, if the item actually represents this definition
     */
    default <E extends Extent> Optional<T> wrapIfPossible(Location<E> block) {
        return Optional.empty();
    }

    default CustomItemData createDefaultCustomItemData() {
        return new CustomItemData(getPluginContainer().getId(), getTypeId(), getDefaultModel());
    }
}
