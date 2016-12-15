package cz.creeper.customitemlibrary.feature;

import org.spongepowered.api.data.DataHolder;

public interface CustomFeature<T extends CustomFeatureDefinition<? extends CustomFeature<T>>> extends HasModels {
    T getDefinition();
    DataHolder getDataHolder();
}
