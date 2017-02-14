package cz.creeper.customitemlibrary.feature.inventory;

import cz.creeper.customitemlibrary.feature.CustomFeature;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.Container;

import java.util.Optional;

public interface CustomInventory<T extends CustomInventoryDefinition<? extends CustomInventory<T>>> extends CustomFeature<T> {
    /**
     * @return The wrapped inventory, if {@link #open(Player, Cause)} was used
     */
    Optional<Container> getContainer();

    /**
     * Opens the inventory.
     *  @param player The player to display the inventory to
     * @param cause The cause
     */
    Container open(Player player, Cause cause);
}
