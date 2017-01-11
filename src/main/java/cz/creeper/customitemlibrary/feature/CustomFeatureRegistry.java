package cz.creeper.customitemlibrary.feature;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;

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
}
