package cz.creeper.customitemlibrary.feature;

import com.google.common.collect.ImmutableSet;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.data.CustomFeatureData;
import cz.creeper.customitemlibrary.feature.block.CustomBlock;
import cz.creeper.customitemlibrary.feature.block.simple.SimpleCustomBlockDefinition;
import cz.creeper.customitemlibrary.feature.item.CustomItemDefinition;
import cz.creeper.customitemlibrary.feature.item.material.CustomMaterialDefinition;
import cz.creeper.customitemlibrary.feature.item.material.PlaceProvider;
import cz.creeper.customitemlibrary.feature.item.tool.CustomToolDefinition;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
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
     * Generates resourcepack files.
     *
     * @param resourcePackDirectory The resourcepack directory
     */
    default void generateResourcePackFiles(Path resourcePackDirectory) {
        getAssets().forEach(asset -> copyAsset(this, asset, resourcePackDirectory));
    }

    static String getFilePath(PluginContainer plugin, String assetPath) {
        return CustomToolDefinition.getAssetPrefix(plugin) + assetPath;
    }

    static void copyAsset(CustomFeatureDefinition definition, String assetPath, Path resourcePackDirectory) {
        PluginContainer plugin = definition.getPluginContainer();
        String filePath = getFilePath(plugin, assetPath);
        Optional<Asset> optionalAsset = Sponge
                .getAssetManager().getAsset(plugin, assetPath);

        if (!optionalAsset.isPresent()) {
            CustomItemLibrary.getInstance().getLogger()
                    .warn("Could not locate an asset for plugin '"
                            + plugin.getName() + "' with path '" + filePath + "'.");
            return;
        }

        Asset asset = optionalAsset.get();
        Path outputFile = resourcePackDirectory.resolve(Paths.get(filePath));

        try {
            Files.createDirectories(outputFile.getParent());

            if (Files.exists(outputFile))
                Files.delete(outputFile);

            Files.createFile(outputFile);

            //noinspection unchecked
            CustomFeatureRegistry registry = (CustomFeatureRegistry) CustomItemLibrary.getInstance()
                            .getService().getRegistry(definition).get();

            try (
                    ReadableByteChannel input = Channels
                            .newChannel(asset.getUrl().openStream());
                    SeekableByteChannel output = Files.newByteChannel
                            (outputFile, StandardOpenOption.WRITE)
            ) {
                //noinspection unchecked
                registry.writeAsset(definition, assetPath, input, output, outputFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
            CustomItemLibrary.getInstance().getLogger()
                    .warn("Could not copy a file from assets (" + filePath + "): " + e.getLocalizedMessage());
        }
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
}
