package cz.creeper.customitemlibrary;

import cz.creeper.customitemlibrary.item.CustomItem;
import cz.creeper.customitemlibrary.data.CustomItemLibraryKeys;
import cz.creeper.customitemlibrary.item.CustomItemDefinition;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;

public interface CustomItemService {
    /**
     * Registers a CustomItemDefinition.
     *
     * @param definition The definition to register
     */
    <I extends CustomItem, T extends CustomItemDefinition<I>> void register(T definition);

    /**
     * @return An unmodifiable map; the keys are the ids, the values are the definitions
     */
    Map<String, CustomItemDefinition> getDefinitionMap();

    /**
     * @param itemStack The {@link ItemStack} to get the definition of
     * @return The definition, if one is registered.
     */
    default Optional<CustomItemDefinition> getDefinition(ItemStack itemStack) {
        return itemStack.get(CustomItemLibraryKeys.CUSTOM_ITEM_ID).flatMap(customItemId -> Optional.ofNullable(getDefinitionMap().get(customItemId)));
    }

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
