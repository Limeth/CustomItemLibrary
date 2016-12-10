package cz.creeper.customitemlibrary.item;

import org.spongepowered.api.item.inventory.ItemStack;

/**
 * A wrapper class for {@link ItemStack}s created by the CustomItemLibrary
 */
public interface CustomItem {
    /**
     * @return The wrapped {@link ItemStack}
     */
    ItemStack getItemStack();

    /**
     * @return The {@link CustomItemDefinition} that was applied to the wrapped {@link ItemStack}
     */
    CustomItemDefinition getDefinition();

    /**
     * One of the models defined by the {@link CustomItemDefinition} returned by {@link #getDefinition()}
     *
     * @return The current model of this {@link CustomItem}
     */
    String getModel();

    /**
     * @param model One of the models defined by the {@link CustomItemDefinition} returned by {@link #getDefinition()}
     */
    void setModel(String model);
}
