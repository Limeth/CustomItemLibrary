package cz.creeper.customitemlibrary.feature.item.material;

import cz.creeper.customitemlibrary.feature.block.CustomBlock;
import cz.creeper.customitemlibrary.feature.block.CustomBlockDefinition;
import cz.creeper.customitemlibrary.util.Block;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Returns the block to place, when a user right-clicks with a {@link CustomMaterial} in their hand.
 */
public interface PlaceProvider {
    @Nonnull
    Optional<BlockSnapshot> provideBlock(CustomMaterial material, Player player, Location<World> location, Cause cause);

    void afterBlockPlace(CustomMaterial material, Player player, Location<World> location, Cause cause);

    /**
     * Places a {@link CustomBlock}.
     *
     * @param block The {@link CustomBlock} to place
     */
    static PlaceProvider of(final CustomBlockDefinition<? extends CustomBlock> block) {
        return new PlaceProvider() {
            @Nonnull
            @Override
            public Optional<BlockSnapshot> provideBlock(CustomMaterial material, Player player, Location<World> location, Cause cause) {
                return Optional.of(CustomBlock.BLOCK_TYPE_CUSTOM.getDefaultState().snapshotFor(location));
            }

            @Override
            public void afterBlockPlace(CustomMaterial material, Player player, Location<World> location, Cause cause) {
                block.placeBlock(Block.of(location), cause);
            }
        };
    }

    /**
     * Doesn't place anything.
     */
    static PlaceProvider cancel() {
        return new PlaceProvider() {
            @Nonnull
            @Override
            public Optional<BlockSnapshot> provideBlock(CustomMaterial material, Player player, Location<World> location, Cause cause) {
                return Optional.empty();
            }

            @Override
            public void afterBlockPlace(CustomMaterial material, Player player, Location<World> location, Cause cause) {
                // Nothing
            }
        };
    }
}
