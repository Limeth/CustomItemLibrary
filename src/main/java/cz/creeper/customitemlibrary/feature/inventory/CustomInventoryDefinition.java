package cz.creeper.customitemlibrary.feature.inventory;

import cz.creeper.customitemlibrary.feature.CustomFeatureDefinition;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.Inventory;

public interface CustomInventoryDefinition<T extends CustomInventory<? extends CustomInventoryDefinition<T>>> extends CustomFeatureDefinition<T> {
    int getHeight();
    int getSize();
    void populate(Inventory inventory);
    T create(DataHolder dataHolder);

    default T open(DataHolder dataHolder, Player player, Cause cause) {
        T inventory = create(dataHolder);

        player.openInventory(inventory.getInventory(), cause);

        return inventory;
    }
}
