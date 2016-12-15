package cz.creeper.customitemlibrary.feature;

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
    void prepare();

    /**
     * Adds files to the resourcepack specified by the argument
     *
     * @param directory The resourcepack directory
     */
    void generateResourcePack(Path directory);
}
