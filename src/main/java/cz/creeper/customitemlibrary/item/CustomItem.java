package cz.creeper.customitemlibrary.item;

import cz.creeper.customitemlibrary.data.CustomItemData;
import cz.creeper.customitemlibrary.data.CustomItemLibraryKeys;
import cz.creeper.customitemlibrary.data.RepresentedCustomItemSnapshotData;
import org.spongepowered.api.item.inventory.ItemStack;

/**
 * A wrapper class for {@link ItemStack}s created by the CustomItemLibrary
 */
public interface CustomItem extends HasModels {
    /**
     * @return The wrapped {@link ItemStack}
     */
    ItemStack getItemStack();

    /**
     * @return The {@link CustomItemDefinition} that was applied to the wrapped {@link ItemStack}
     */
    CustomItemDefinition<? extends CustomItem> getDefinition();

    default CustomItemData createCustomItemData() {
        CustomItemData data = getDefinition().createDefaultCustomItemData();

        data.set(CustomItemLibraryKeys.CUSTOM_ITEM_MODEL, getModel());

        return data;
    }

    default RepresentedCustomItemSnapshotData createRepresentedCustomItemSnapshotData() {
        return new RepresentedCustomItemSnapshotData(getItemStack().createSnapshot());
    }
}
