package cz.creeper.customitemlibrary.feature.block;

import cz.creeper.customitemlibrary.feature.CustomFeature;
import cz.creeper.customitemlibrary.util.Block;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataHolder;

public interface CustomBlock<T extends CustomBlockDefinition<? extends CustomBlock<T>>> extends CustomFeature<T> {
    BlockType BLOCK_TYPE_CUSTOM = BlockTypes.BARRIER;

    Block getBlock();
}
