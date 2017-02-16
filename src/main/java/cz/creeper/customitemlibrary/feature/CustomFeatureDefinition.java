package cz.creeper.customitemlibrary.feature;

import com.google.common.collect.ImmutableSet;
import cz.creeper.customitemlibrary.feature.block.CustomBlock;
import cz.creeper.customitemlibrary.feature.block.simple.SimpleCustomBlockDefinition;
import cz.creeper.customitemlibrary.feature.inventory.simple.SimpleCustomInventoryDefinition;
import cz.creeper.customitemlibrary.feature.inventory.simple.SimpleCustomInventoryDefinitionBuilder;
import cz.creeper.customitemlibrary.feature.item.CustomItemDefinition;
import cz.creeper.customitemlibrary.feature.item.material.CustomMaterialDefinition;
import cz.creeper.customitemlibrary.feature.item.material.PlaceProvider;
import cz.creeper.customitemlibrary.feature.item.tool.CustomToolDefinition;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.PluginContainer;

import java.nio.file.Path;
import java.util.Set;

public interface CustomFeatureDefinition<T extends CustomFeature<? extends CustomFeatureDefinition<T>>> {
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
    default Set<AssetId> getAssets() {
        return ImmutableSet.of();
    }

    /**
     * Generates resourcepack files.
     *
     * @param resourcePackDirectory The resourcepack directory
     */
    default void generateResourcePackFiles(Path resourcePackDirectory) {
        getAssets().forEach(asset -> CustomFeatureRegistry.copyAsset(this, asset, resourcePackDirectory));
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
     *         {@code correctToolPredicate(CorrectToolPredicate)}
     *         - Determines whether the tool used to break the block
     *           should be used to destroy blocks of this {@link SimpleCustomBlockDefinition};
     *           this affects whether the block drops items when broken
     *           provided by the {@code dropProvider(DropProvider)}
     *     </li>
     *     <li>
     *         {@code hardness(Double)}
     *         - Determines how long a block takes to be broken; uses the hardness of the {@link BlockType} from the
     *           {@code effectState(BlockState)} method if no value is provided
     *     </li>
     *     <li>
     *         {@code effectState(BlockState)}
     *         - The state used to create the particle and sound effect when the block is broken or placed;
     *           the default value is the default state of {@link BlockTypes#STONE}
     *     </li>
     *     <li>
     *         {@code dropProvider(DropProvider)}
     *         - Determines which {@link ItemStackSnapshot}s to drop when the block is broken with the proper tool as
     *           defined with the {@code correctToolPredicate(CorrectToolPredicate)} method
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

    static SimpleCustomInventoryDefinitionBuilder simpleInventoryBuilder() {
        return SimpleCustomInventoryDefinition.builder();
    }
}
