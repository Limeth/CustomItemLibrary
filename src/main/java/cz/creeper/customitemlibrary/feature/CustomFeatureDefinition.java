package cz.creeper.customitemlibrary.feature;

import com.google.common.collect.ImmutableSet;
import cz.creeper.customitemlibrary.data.CustomFeatureData;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Set;

public interface CustomFeatureDefinition<T extends CustomFeature<? extends CustomFeatureDefinition<T>>> extends DefinesModels {
    /**
     * @return The owner plugin
     */
    PluginContainer getPluginContainer();

    /**
     * @return The associated plugin instance, if available
     */
    default Object getPlugin() {
        return getPluginContainer().getInstance()
                .orElseThrow(() -> new IllegalStateException("Could not access the plugin instance."));
    }

    /**
     * The string uniquely identifying this feature type.
     * The latter part of `<pluginId>:<typeId>`.
     *
     * Must be lower-case, separate words with an underscore.
     */
    String getTypeId();

    default Set<String> getAssets() {
        return ImmutableSet.of();
    }

    default CustomFeatureData createDefaultCustomItemData() {
        return new CustomFeatureData(getPluginContainer().getId(), getTypeId(), getDefaultModel());
    }
}
