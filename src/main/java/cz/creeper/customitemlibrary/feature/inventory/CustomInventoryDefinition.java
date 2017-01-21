package cz.creeper.customitemlibrary.feature.inventory;

import cz.creeper.customitemlibrary.feature.CustomFeatureDefinition;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.Inventory;

public interface CustomInventoryDefinition<T extends CustomInventory<? extends CustomInventoryDefinition<T>>> extends CustomFeatureDefinition<T> {
    int getHeight();
    int getSize();
    T open(Player player, Cause cause);
    void populate(Inventory inventory, Player player, Cause cause);
}
