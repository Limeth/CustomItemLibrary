package cz.creeper.customitemlibrary.registry;

import cz.creeper.customitemlibrary.CustomItem;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;

public interface CustomItemDefinition<T extends CustomItem> {
    char ID_SEPARATOR = ':';

    static String getId(String pluginId, String typeId) {
        return pluginId + ID_SEPARATOR + typeId;
    }

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
    String getPluginId();

    /**
     * The string uniquely identifying this item type.
     * The latter part of `<pluginId>:<typeId>`.
     *
     * Must be lower-case, separate words with an underscore.
     */
    String getTypeId();

    /**
     * @return "<pluginId>:<typeId>"
     */
    default String getId() {
        return getId(getPluginId(), getTypeId());
    }

    /**
     * @return A {@link CustomItem} in with default properties.
     */
    T createItem(Cause cause);

    /**
     * Wraps the {@link ItemStack} in a helper class extending {@link CustomItem},
     * if the {@link ItemStack} is representing an actual custom item
     * created by the {@link CustomItemDefinition#createItem(Cause)} method.
     *
     * @param itemStack The {@link ItemStack} to wrap
     * @return The wrapped {@link ItemStack}, if the item actually represents this definition
     */
    Optional<T> wrapIfPossible(ItemStack itemStack);
}
