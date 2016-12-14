package cz.creeper.customitemlibrary.feature.item;

import cz.creeper.customitemlibrary.feature.CustomFeatureDefinition;
import cz.creeper.customitemlibrary.feature.item.material.CustomMaterialDefinition;
import cz.creeper.customitemlibrary.feature.item.tool.CustomToolDefinition;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;

import java.util.Optional;

public interface CustomItemDefinition<T extends CustomItem<? extends CustomItemDefinition<T>>> extends CustomFeatureDefinition<T> {
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

    static CustomToolDefinition.CustomToolDefinitionBuilder toolBuilder() {
        return CustomToolDefinition.builder();
    }

    static CustomMaterialDefinition.CustomMaterialDefinitionBuilder materialBuilder() {
        return CustomMaterialDefinition.builder();
    }
}
