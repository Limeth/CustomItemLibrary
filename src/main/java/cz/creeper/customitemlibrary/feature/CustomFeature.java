package cz.creeper.customitemlibrary.feature;

public interface CustomFeature<T extends CustomFeatureDefinition<? extends CustomFeature<T>>> extends HasModels {
    T getDefinition();
}
