package cz.creeper.customitemlibrary.feature;

public interface CustomModelledFeature<T extends CustomModelledFeatureDefinition<? extends CustomModelledFeature<T>>> extends CustomFeature<T>, HasModels {
}
