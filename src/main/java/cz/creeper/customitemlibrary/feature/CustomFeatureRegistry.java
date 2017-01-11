package cz.creeper.customitemlibrary.feature;

import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.feature.item.tool.CustomToolDefinition;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

public interface CustomFeatureRegistry<I extends CustomFeature<T>, T extends CustomFeatureDefinition<I>> {
    /**
     * Registers the definition.
     *
     * @param definition the definition to register
     */
    void register(T definition);

    /**
     * Finish all preparations
     */
    default void prepare() {}

    /**
     * Generates resourcepack files.
     * This should not call {@link CustomFeatureDefinition#generateResourcePackFiles(Path)}
     * as that method is called separately.
     *
     * @param resourcePackDirectory The resourcepack directory
     */
    default void generateResourcePackFiles(Path resourcePackDirectory) {}

    /**
     * @param asset The asset to modify
     * @param input The data that would be written to the file
     * @param output Where to write the data to
     * @param outputFile The file to write the data to
     */
    default void writeAsset(T definition, String asset,
            ReadableByteChannel input, WritableByteChannel output,
            Path outputFile) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(2048);

        while(input.read(buffer) != -1) {
            buffer.flip();
            output.write(buffer);
            buffer.clear();
        }
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

    static String getFilePath(PluginContainer plugin, String assetPath) {
        return CustomToolDefinition.getAssetPrefix(plugin) + assetPath;
    }
}
