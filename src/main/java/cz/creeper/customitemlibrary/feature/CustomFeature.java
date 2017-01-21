package cz.creeper.customitemlibrary.feature;

import cz.creeper.customitemlibrary.feature.block.CustomBlock;
import cz.creeper.customitemlibrary.feature.item.CustomItem;
import org.spongepowered.api.data.DataHolder;

/**
 * A common interface for {@link CustomItem}s and {@link CustomBlock}s.
 */
public interface CustomFeature<T extends CustomFeatureDefinition<? extends CustomFeature<T>>> {
    /**
     * @return The definition this {@link CustomFeature} was created with.
     */
    T getDefinition();

    /**
     * @return A data holder which should persist across sessions.
     */
    DataHolder getDataHolder();
}
