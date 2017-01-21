package cz.creeper.customitemlibrary.feature;

import cz.creeper.customitemlibrary.data.CustomFeatureData;
import org.spongepowered.api.data.DataHolder;

public interface CustomModelledFeatureDefinition<T extends CustomModelledFeature<? extends CustomModelledFeatureDefinition<T>>> extends CustomFeatureDefinition<T>, DefinesModels {
    /**
     * @return A {@link CustomFeatureData} instance which identifies which {@link CustomFeatureDefinition}
     * a {@link DataHolder} represents.
     */
    default CustomFeatureData createDefaultCustomFeatureData() {
        return new CustomFeatureData(getPluginContainer().getId(), getTypeId(), getDefaultModel());
    }
}
