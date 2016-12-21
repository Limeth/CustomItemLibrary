package cz.creeper.customitemlibrary.feature;

import com.google.common.collect.ImmutableSet;
import cz.creeper.customitemlibrary.data.CustomFeatureData;
import cz.creeper.customitemlibrary.feature.block.CustomBlock;
import cz.creeper.customitemlibrary.feature.block.simple.SimpleCustomBlockDefinition;
import cz.creeper.customitemlibrary.feature.item.CustomItemDefinition;
import cz.creeper.customitemlibrary.feature.item.material.CustomMaterialDefinition;
import cz.creeper.customitemlibrary.feature.item.material.PlaceProvider;
import cz.creeper.customitemlibrary.feature.item.tool.CustomToolDefinition;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Set;

public interface CustomFeatureDefinition<T extends CustomFeature<? extends CustomFeatureDefinition<T>>> extends DefinesModels {
    /**
     * @return The owner plugin, which created this {@link CustomFeatureDefinition}
     */
    PluginContainer getPluginContainer();

    /**
     * @return The instance of the owner plugin, which created this {@link CustomFeatureDefinition}
     */
    default Object getPlugin() {
        return getPluginContainer().getInstance()
                .orElseThrow(() -> new IllegalStateException("Could not access the plugin instance."));
    }

    /**
     * The string uniquely identifying this feature type.
     * Two {@link CustomItemDefinition}s or two {@link SimpleCustomBlockDefinition}s
     * with the same type ID cannot be registered at the same time.
     * The latter part of `<pluginId>:<typeId>`.
     *
     * Must be lower-case, separate words with an underscore.
     */
    String getTypeId();

    /**
     * @return A {@link Set} of all assets copied over to the generated resource pack.
     */
    default Set<String> getAssets() {
        return ImmutableSet.of();
    }

    /**
     * @return A {@link CustomFeatureData} instance which identifies which {@link CustomFeatureDefinition}
     * a {@link DataHolder} represents.
     */
    default CustomFeatureData createDefaultCustomFeatureData() {
        return new CustomFeatureData(getPluginContainer().getId(), getTypeId(), getDefaultModel());
    }

    /**
     * Creates a new {@link CustomToolDefinition.CustomToolDefinitionBuilder}.
     *
     * <h2>Required fields:</h2>
     * <ol>
     *     <li>
     *         {@code plugin(Object)}
     *         - The owner plugin
     *     </li>
     *     <li>
     *         {@code typeId(String)}
     *         - The unique identifier (should be lowercase; latter part of `%PLUGIN_ID%:%TYPE_ID%`)
     *     </li>
     *     <li>
     *         {@code itemStackSnapshot(ItemStackSnapshot)}
     *         - The original snapshot used to build custom items;
     *           the quantity must be equal to 1, the type must be damageable (eg.: shears, sword)
     *     </li>
     *     <li>
     *         {@code defaultModel(String)}
     *         - The default item model (at `assets/%PLUGIN_ID%/models/tools/%TYPE_ID%.json`)
     *     </li>
     * </ol>
     *
     * <h2>Optional fields:</h2>
     * <ol>
     *     <li>
     *         {@code additionalModels(Collection)}
     *         - The rest of the models this custom item uses
     *     </li>
     *     <li>
     *         {@code additionalAssets(Collection)}
     *         - Any additional assets to copy when generating the resource pack
     *     </li>
     * </ol>
     */
    static CustomToolDefinition.CustomToolDefinitionBuilder itemToolBuilder() {
        return CustomToolDefinition.builder();
    }

    /**
     * Creates a new {@link CustomMaterialDefinition.CustomMaterialDefinitionBuilder}.
     *
     * <h2>Required fields:</h2>
     * <ol>
     *     <li>
     *         {@code plugin(Object)}
     *         - The owner plugin
     *     </li>
     *     <li>
     *         {@code typeId(String)}
     *         - The unique identifier (should be lowercase; latter part of `%PLUGIN_ID%:%TYPE_ID%`)
     *     </li>
     *     <li>
     *         {@code itemStackSnapshot(ItemStackSnapshot)}
     *         - The original snapshot used to build custom items;
     *           the quantity must be equal to 1, the type must be damageable (eg.: shears, sword)
     *     </li>
     *     <li>
     *         {@code defaultModel(String)}
     *         - The default item model (at `assets/%PLUGIN_ID%/textures/materials/%TYPE_ID%.png`)
     *     </li>
     * </ol>
     *
     * <h2>Optional fields:</h2>
     * <ol>
     *     <li>
     *         {@code placeProvider(PlaceProvider)}
     *         - Defines what happens when a user right-clicks the ground;
     *           if no {@link PlaceProvider} is set, the {@link CustomBlock} with the same {@code typeId} is placed,
     *           if found
     *     </li>
     *     <li>
     *         {@code additionalModels(Collection)}
     *         - The rest of the models this custom item uses
     *     </li>
     * </ol>
     */
    static CustomMaterialDefinition.CustomMaterialDefinitionBuilder itemMaterialBuilder() {
        return CustomMaterialDefinition.builder();
    }

    /**
     * Creates a new {@link SimpleCustomBlockDefinition.SimpleCustomBlockDefinitionBuilder}.
     *
     * <h2>Required fields:</h2>
     * <ol>
     *     <li>
     *         {@code plugin(Object)}
     *         - The owner plugin
     *     </li>
     *     <li>
     *         {@code typeId(String)}
     *         - The unique identifier (should be lowercase; latter part of `%PLUGIN_ID%:%TYPE_ID%`)
     *     </li>
     *     <li>
     *         {@code defaultModel(String)}
     *         - The default block model (at `assets/%PLUGIN_ID%/models/blocks/%TYPE_ID%.json`)
     *     </li>
     * </ol>
     *
     * <h2>Optional fields:</h2>
     * <ol>
     *     <li>
     *         {@code harvestingType(BlockType)}
     *         - The {@link BlockType} to determine whether a correct tool has been used to break the block to decide
     *           whether drops appear; {@link BlockTypes#STONE} by default
     *     </li>
     *     <li>
     *         {@code hardness(Double)}
     *         - Determines how long a block takes to be broken; uses the hardness of the {@link BlockType} from the
     *           {@code harvestingType(BlockType)} method if no value is provided
     *     </li>
     *     <li>
     *         {@code effectState(BlockState)}
     *         - The state used to create the particle and sound effect when the block is broken or placed;
     *           uses the default block state of the {@link BlockType} from the {@code harvestingType(BlockType)} method
     *           if no value is provided
     *     </li>
     *     <li>
     *         {@code dropProvider(DropProvider)}
     *         - Determines which {@link ItemStackSnapshot}s to drop when the block is broken with the proper tool as
     *           defined with the {@code harvestingType(BlockType)} method
     *     </li>
     *     <li>
     *         {@code rotateHorizontally(boolean)}
     *         - Determines whether the block model gets rotated towards the player when placed
     *     </li>
     *     <li>
     *         {@code additionalModels(Collection)}
     *         - The rest of the models this custom item uses
     *     </li>
     *     <li>
     *         {@code additionalAssets(Collection)}
     *         - Any additional assets to copy when generating the resource pack
     *     </li>
     * </ol>
     */
    static SimpleCustomBlockDefinition.SimpleCustomBlockDefinitionBuilder simpleBlockBuilder() {
        return SimpleCustomBlockDefinition.builder();
    }
}
