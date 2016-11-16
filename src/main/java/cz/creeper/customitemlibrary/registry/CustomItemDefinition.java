package cz.creeper.customitemlibrary.registry;

import cz.creeper.customitemlibrary.CustomItem;
import lombok.NonNull;
import org.spongepowered.api.event.cause.Cause;

public interface CustomItemDefinition<T extends CustomItem> {
    char ID_SEPARATOR = ':';

    static String getPluginId(String id) {
        return id.substring(0, id.indexOf(ID_SEPARATOR));
    }

    static String getTypeId(String id) {
        return id.substring(id.indexOf(ID_SEPARATOR) + 1);
    }

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
     * @return A {@link CustomItem} in with default properties.
     */
    @NonNull T createItem(Cause cause);
}
