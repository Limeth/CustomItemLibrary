package cz.creeper.customitemlibrary.item.block;

import cz.creeper.customitemlibrary.util.Block;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Set;
import java.util.UUID;

public interface CustomBlockModelDefinition {
    BlockType BLOCK_TYPE_CUSTOM = BlockTypes.BARRIER;

    /**
     * @return the name that uniquely identifies this {@link CustomBlockModelDefinition}
     */
    String getName();

    /**
     * Spawns entities which create the model.
     *
     * @param block the location to build the model at
     * @param cause the cause to use when building the model
     * @return a set of all spawned entities
     */
    Set<UUID> buildAppearance(Block block, Cause cause);

    /**
     * Places a {@link #BLOCK_TYPE_CUSTOM} block at the specified location
     * and spawns entities which create the model.
     *
     * @param block the location to build the model at
     * @param cause the cause to use when building the model
     * @return a set of all spawned entities
     */
    default Set<UUID> build(Block block, Cause cause) {
        Location<World> location = block.getLocation()
                .orElseThrow(() -> new IllegalArgumentException("Could not access the location of the specified block."));

        location.setBlock(BlockState.builder().blockType(BLOCK_TYPE_CUSTOM).build(), cause);

        return buildAppearance(block, cause);
    }
}
