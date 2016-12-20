package cz.creeper.customitemlibrary.feature.block;

import cz.creeper.customitemlibrary.feature.CustomFeature;
import cz.creeper.customitemlibrary.util.Block;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.extent.Extent;

import java.util.UUID;

/**
 * A wrapped block created by the {@link CustomBlockDefinition#placeBlock(Block, Cause)} method.
 * Allows for changing the model using {@link #setModel(String)} and {@link #getModel()}.
 */
public interface CustomBlock<T extends CustomBlockDefinition<? extends CustomBlock<T>>> extends CustomFeature<T> {
    BlockType BLOCK_TYPE_CUSTOM = BlockTypes.BARRIER;

    /**
     * @return The wrapped {@link Block}
     */
    Block getBlock();

    /**
     * @return The {@link UUID} of the wrapped {@link ArmorStand}
     */
    UUID getArmorStandId();

    @Override
    default ArmorStand getDataHolder() {
        Entity entity = getExtent().getEntity(getArmorStandId())
                .orElseThrow(() -> new IllegalStateException("Could not find the armor stand entity."));

        if(!(entity instanceof ArmorStand))
            throw new IllegalStateException("The data holder entity assigned to this custom block is not an armor stand.");

        return (ArmorStand) entity;
    }

    default Extent getExtent() {
        return getBlock().getExtent()
                .orElseThrow(() -> new IllegalStateException("Could not access the extent."));
    }
}
