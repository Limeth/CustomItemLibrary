package cz.creeper.customitemlibrary.feature.block;

import cz.creeper.customitemlibrary.feature.CustomFeature;
import cz.creeper.customitemlibrary.util.Block;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.world.extent.Extent;

import java.util.UUID;

public interface CustomBlock<T extends CustomBlockDefinition<? extends CustomBlock<T>>> extends CustomFeature<T> {
    BlockType BLOCK_TYPE_CUSTOM = BlockTypes.BARRIER;

    Block getBlock();
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
