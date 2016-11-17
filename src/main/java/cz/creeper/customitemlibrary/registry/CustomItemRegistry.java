package cz.creeper.customitemlibrary.registry;

import cz.creeper.customitemlibrary.CustomItem;

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
    void load();

    /**
     * Saves the registry to persistent storage.
     */
    void save();
}
