package cz.creeper.customitemlibrary;

public interface CustomItemService {
    void define(CustomItemDefinition definition);

    /**
     * Loads the custom item indexes
     */
    void loadDictionary();

    /**
     * Saves the custom item indexes
     */
    void saveDictionary();
}
