package cz.creeper.customitemlibrary.feature.block;

import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.CustomItemServiceImpl;
import cz.creeper.customitemlibrary.feature.CustomModelledFeature;
import cz.creeper.customitemlibrary.util.Block;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;

import java.util.UUID;

/**
 * A wrapped block created by the {@link CustomBlockDefinition#placeBlock(Block, Cause)} method.
 * Allows for changing the model using {@link #setModel(String)} and {@link #getModel()}.
 */
public interface CustomBlock<T extends CustomBlockDefinition<? extends CustomBlock<T>>> extends CustomModelledFeature<T> {
    BlockType BLOCK_TYPE_CUSTOM = BlockTypes.BARRIER;

    /**
     * @return The wrapped {@link Block}
     */
    Block getBlock();

    /**
     * @return The {@link UUID} of the wrapped {@link ArmorStand}
     */
    UUID getArmorStandId();

    /**
     * Removes the {@link CustomBlock} and replaces it with a vanilla {@link BlockState}.
     *
     * @param replacement The {@link BlockState} to replace the block with
     * @param flag How the world should be updated
     * @param cause The cause
     * @return Whether the block has been successfully replaced
     */
    default boolean replaceWith(BlockState replacement, BlockChangeFlag flag, Cause cause) {
        Location<World> location = getBlock().getLocation()
                .orElseThrow(() -> new IllegalStateException("Could not access the location of this block."));
        CustomItemServiceImpl service = CustomItemLibrary.getInstance().getService();
        Block block = getBlock();

        service.unregisterBlockAsLoaded(block);
        service.removeArmorStandsAt(block);

        return location.setBlock(replacement, flag, cause);
    }

    /**
     * Removes the {@link CustomBlock} and replaces it with a vanilla {@link BlockState}.
     * All physics updates ({@link BlockChangeFlag#ALL}) are applied.
     *
     * @param replacement The {@link BlockState} to replace the block with
     * @param cause The cause
     * @return Whether the block has been successfully replaced
     */
    default boolean replaceWith(BlockState replacement, Cause cause) {
        return replaceWith(replacement, BlockChangeFlag.ALL, cause);
    }

    /**
     * Removes the {@link CustomBlock} from the world.
     * All physics updates ({@link BlockChangeFlag#ALL}) are applied.
     *
     * @param cause The cause
     * @return Whether the block has been successfully replaced
     */
    default boolean remove(Cause cause) {
        return replaceWith(BlockTypes.AIR.getDefaultState(), BlockChangeFlag.ALL, cause);
    }

    /**
     * Removes the {@link CustomBlock} and replaces it with another {@link CustomBlock}.
     * All physics updates ({@link BlockChangeFlag#ALL}) are applied.
     *
     * @param replacement The {@link CustomBlockDefinition} to replace the block with
     * @param cause The cause
     * @return Whether the block has been successfully replaced
     */
    default <R extends CustomBlock<? extends D>, D extends CustomBlockDefinition<? extends R>> R replaceWith(D replacement, BlockChangeFlag flag, Cause cause) {
        replaceWith(BlockState.builder().blockType(BlockTypes.AIR).build(), BlockChangeFlag.NONE, cause);

        return replacement.placeBlock(getBlock(), flag, cause);
    }

    /**
     * Should be called every tick by the registry.
     * You probably don't want to use this directly.
     */
    void update();

    /**
     * @return Whether the block is loaded or not
     */
    boolean isAccessible();

    @Override
    default ArmorStand getDataHolder() {
        Entity entity = getExtent().getEntity(getArmorStandId())
                .orElseThrow(() -> new IllegalStateException("Could not find the armor stand entity."));

        if(!(entity instanceof ArmorStand))
            throw new IllegalStateException("The data holder entity assigned to this custom block is not an armor stand.");

        return (ArmorStand) entity;
    }

    default Extent getExtent() {
        return getBlock().getWorld()
                .orElseThrow(() -> new IllegalStateException("Could not access the extent."));
    }
}
