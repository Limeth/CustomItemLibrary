package cz.creeper.customitemlibrary.feature.inventory;

import cz.creeper.customitemlibrary.feature.CustomFeatureDefinition;
import org.spongepowered.api.data.DataHolder;

public interface CustomInventoryDefinition<T extends CustomInventory<? extends CustomInventoryDefinition<T>>> extends CustomFeatureDefinition<T> {
    int getHeight();
    int getSize();
    T create(DataHolder dataHolder);
}
