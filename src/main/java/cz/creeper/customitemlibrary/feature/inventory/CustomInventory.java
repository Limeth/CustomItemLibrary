package cz.creeper.customitemlibrary.feature.inventory;

import cz.creeper.customitemlibrary.feature.CustomFeature;
import org.spongepowered.api.item.inventory.Inventory;

public interface CustomInventory<T extends CustomInventoryDefinition<? extends CustomInventory<T>>> extends CustomFeature<T> {
    /**
     * @return The wrapped inventory
     */
    Inventory getInventory();
}
