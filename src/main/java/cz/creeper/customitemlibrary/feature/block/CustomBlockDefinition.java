package cz.creeper.customitemlibrary.feature.block;

import cz.creeper.customitemlibrary.feature.CustomFeatureDefinition;
import cz.creeper.customitemlibrary.feature.item.CustomItem;
import cz.creeper.customitemlibrary.util.Block;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;

import java.util.Optional;

/**
 * Apply to models:

 "display": {
   "head": {
   "translation": [0, -43.225, 0],
   "scale": [1.6, 1.6, 1.6]
   }
 }
 */
public interface CustomBlockDefinition<T extends CustomBlock<? extends CustomBlockDefinition<T>>> extends CustomFeatureDefinition<T> {
    T placeBlock(Block block, Cause cause);

    /**
     * Wraps the {@link Location} in a helper class extending {@link CustomItem},
     * if the {@link Location} is representing an actual custom feature
     * created by the {@link CustomBlockDefinition#placeBlock(Block, Cause)} method.
     *
     * @param block The block to wrap
     * @return The wrapped {@link Block}, if the feature actually represents this definition
     */
    Optional<T> wrapIfPossible(Block block);
}
