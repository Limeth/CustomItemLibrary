package cz.creeper.customitemlibrary;

import lombok.NonNull;

public interface CustomItemDefinition<T extends CustomItem> {
    char ID_SEPARATOR = ':';

    /**
     * The ID of the plugin that created this item type.
     * The former part of `<pluginId>:<typeId>`.
     *
     * Must be lower-case, separate words with an underscore.
     */
    @NonNull String getPluginId();

    /**
     * The string uniquely identifying this item type.
     * The latter part of `<pluginId>:<typeId>`.
     *
     * Must be lower-case, separate words with an underscore.
     */
    @NonNull String getTypeId();

    /**
     * @return "<pluginId>:<typeId>"
     */
    @NonNull default String getId() {
        return getPluginId() + ID_SEPARATOR + getTypeId();
    }

    /**
     * The human-readable display name of the item.
     * Starts with an uppercase letter.
     */
    @NonNull String getDisplayName();

    /**
     * @return A {@link CustomItem} in with default properties.
     */
    @NonNull T createItem();
}
