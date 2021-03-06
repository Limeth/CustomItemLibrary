package cz.creeper.customitemlibrary.feature.block.simple;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import javax.annotation.Nonnull;
import java.util.List;

public interface DropProvider {
    /**
     * @return the list of items to be dropped, when a proper tool is used on a {@link SimpleCustomBlock}.
     */
    @Nonnull
    List<ItemStackSnapshot> provideDrops(SimpleCustomBlock block, Player player, Cause cause);
}
