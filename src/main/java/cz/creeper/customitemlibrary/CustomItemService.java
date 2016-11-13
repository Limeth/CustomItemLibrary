package cz.creeper.customitemlibrary;

import java.util.Optional;

public interface CustomItemService {
    CustomItemRecord registerCustomItem(Object plugin, String typeId);
    Optional<CustomItemRecord> getCustomItemRecord(Object plugin, String typeId);
    Optional<CustomItemRecord> getCustomItemRecord(Object plugin, int durability);

    /**
     * Loads the custom item indexes
     */
    void loadDictionary();

    /**
     * Saves the custom item indexes
     */
    void saveDictionary();
}
