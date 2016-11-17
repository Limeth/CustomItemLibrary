package cz.creeper.customitemlibrary.registry;

import cz.creeper.customitemlibrary.CustomItem;

import java.nio.file.Path;

public interface CustomItemRegistry<I extends CustomItem, T extends CustomItemDefinition<I>> {
    /**
     * Registers the definition.
     *
     * @param definition the definition to register
     */
    void register(T definition);

    /**
     * Loads the registry from persistent storage.
     */
    void load(Path directory);

    /**
     * Saves the registry to persistent storage.
     */
    void save(Path directory);

    /**
     * Adds files to the resourcepack specified by the argument
     *
     * @param directory The resourcepack directory
     */
    void generateResourcePack(Path directory);
}
