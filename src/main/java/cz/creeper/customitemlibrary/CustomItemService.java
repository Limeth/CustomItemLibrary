package cz.creeper.customitemlibrary;

import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;

public interface CustomItemService {
    /**
     * Registers a CustomItemDefinition.
     *
     * @param definition The definition to register
     */
    <I extends CustomItem, T extends CustomItemDefinition<I>> void register(T definition);

    /**
     * @param itemStack The ItemStack to wrap
     * @return The wrapped ItemStack, if it is a registered custom item.
     */
    Optional<CustomItem> getCustomItem(ItemStack itemStack);

    /**
     * Loads the custom item indexes
     */
    void loadDictionary();

    /**
     * Saves the custom item indexes
     */
    void saveDictionary();
}
