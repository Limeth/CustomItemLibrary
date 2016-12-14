package cz.creeper.customitemlibrary.feature.item;

import cz.creeper.customitemlibrary.feature.CustomFeatureDefinition;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;

public interface CustomItemDefinition<T extends CustomItem<? extends CustomItemDefinition<T>>> extends CustomFeatureDefinition<T> {
    /**
     * @return A {@link CustomItem} with default properties.
     */
    T createItem(Cause cause);

    /**
     * Wraps the {@link ItemStack} in a helper class extending {@link CustomItem},
     * if the {@link ItemStack} is representing an actual custom feature
     * created by the {@link CustomItemDefinition#createItem(Cause)} method.
     *
     * @param itemStack The {@link ItemStack} to wrap
     * @return The wrapped {@link ItemStack}, if the feature actually represents this definition
     */
    Optional<T> wrapIfPossible(ItemStack itemStack);
}
