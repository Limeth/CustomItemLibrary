package cz.creeper.customitemlibrary.item;

import java.nio.file.Path;

public interface CustomItemRegistry<I extends CustomItem, T extends CustomItemDefinition<I>> {
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
