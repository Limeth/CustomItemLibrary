package cz.creeper.customitemlibrary;

import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;

public interface CustomItemRegistry<I extends CustomItem, T extends CustomItemDefinition<I>> {
    /**
     * Register the definition.
     *
     * @param definition the definition.
     * @return {@code true}, if the item has been registered the first time, {@code false} otherwise
     */
    boolean register(T definition);

    /**
     * Wraps the {@link ItemStack} in a helper class extending {@link CustomItem},
     * if the {@link ItemStack} is representing an actual custom item
     * created by the {@link CustomItemDefinition#createItem()} method.
     *
     * @param itemStack The {@link ItemStack} to wrap
     * @return A wrapped custom item
     */
    Optional<I> wrapIfPossible(ItemStack itemStack);
}
