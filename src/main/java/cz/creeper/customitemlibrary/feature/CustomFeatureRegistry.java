package cz.creeper.customitemlibrary.feature;

import cz.creeper.customitemlibrary.feature.item.CustomItem;
import cz.creeper.customitemlibrary.feature.item.CustomItemDefinition;

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
    void finalize();

    /**
     * Adds files to the resourcepack specified by the argument
     *
     * @param directory The resourcepack directory
     */
    void generateResourcePack(Path directory);
}
