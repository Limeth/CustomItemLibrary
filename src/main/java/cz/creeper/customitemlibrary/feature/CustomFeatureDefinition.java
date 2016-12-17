package cz.creeper.customitemlibrary.feature;

import com.google.common.collect.ImmutableSet;
import cz.creeper.customitemlibrary.data.CustomFeatureData;
import cz.creeper.customitemlibrary.feature.block.simple.SimpleCustomBlockDefinition;
import cz.creeper.customitemlibrary.feature.item.material.CustomMaterialDefinition;
import cz.creeper.customitemlibrary.feature.item.tool.CustomToolDefinition;
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



    default CustomFeatureData createDefaultCustomFeatureData() {
        return new CustomFeatureData(getPluginContainer().getId(), getTypeId(), getDefaultModel());
    }

    static CustomToolDefinition.CustomToolDefinitionBuilder itemToolBuilder() {
        return CustomToolDefinition.builder();
    }

    static CustomMaterialDefinition.CustomMaterialDefinitionBuilder itemMaterialBuilder() {
        return CustomMaterialDefinition.builder();
    }

    static SimpleCustomBlockDefinition.SimpleCustomBlockDefinitionBuilder simpleBlockBuilder() {
        return SimpleCustomBlockDefinition.builder();
    }
}
