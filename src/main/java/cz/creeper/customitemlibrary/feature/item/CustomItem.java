package cz.creeper.customitemlibrary.feature.item;

import cz.creeper.customitemlibrary.data.CustomFeatureData;
import cz.creeper.customitemlibrary.data.CustomItemLibraryKeys;
import cz.creeper.customitemlibrary.data.RepresentedCustomItemSnapshotData;
import cz.creeper.customitemlibrary.feature.CustomFeature;
import cz.creeper.customitemlibrary.feature.CustomFeatureDefinition;
import org.spongepowered.api.item.inventory.ItemStack;

/**
 * A wrapper class for {@link ItemStack}s created by the CustomItemLibrary
 */
public interface CustomItem<T extends CustomItemDefinition<? extends CustomItem<T>>> extends CustomFeature<T> {
    ItemStack getDataHolder();

    default CustomFeatureData createCustomItemData() {
        CustomFeatureData data = getDefinition().createDefaultCustomItemData();

        data.set(CustomItemLibraryKeys.CUSTOM_FEATURE_MODEL, getModel());

        return data;
    }

    default RepresentedCustomItemSnapshotData createRepresentedCustomItemSnapshotData() {
        return new RepresentedCustomItemSnapshotData(getDataHolder().createSnapshot());
    }
}
