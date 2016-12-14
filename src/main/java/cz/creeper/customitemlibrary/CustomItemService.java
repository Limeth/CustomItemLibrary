package cz.creeper.customitemlibrary;

import cz.creeper.customitemlibrary.data.CustomItemData;
import cz.creeper.customitemlibrary.item.CustomItem;
import cz.creeper.customitemlibrary.item.CustomItemDefinition;
import cz.creeper.customitemlibrary.item.block.CustomBlock;
import cz.creeper.customitemlibrary.util.Block;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.Set;

public interface CustomItemService {
    /**
     * Registers a CustomItemDefinition.
     *
     * @param definition The definition to register
     */
    <I extends CustomItem, T extends CustomItemDefinition<I>> void register(T definition);

    /**
     * @return An unmodifiable set of all registered definitions
     */
    Set<CustomItemDefinition<CustomItem>> getDefinitions();

    /**
     * @param itemStack The {@link ItemStack} to get the definition of
     * @return The definition, if one is registered.
     */
    default Optional<CustomItemDefinition<CustomItem>> getDefinition(ItemStack itemStack) {
        return itemStack.get(CustomItemData.class).flatMap(data ->
                getDefinition(data.customItemPluginId().get(), data.customItemTypeId().get()));
    }

    /**
     * @param plugin The instance of the plugin that registered the {@link CustomItemDefinition}
     * @param typeId The id to look for
     * @return The definition, if one is registered.
     */
    default Optional<CustomItemDefinition<CustomItem>> getDefinition(Object plugin, String typeId) {
        return getDefinition(Sponge.getPluginManager().fromInstance(plugin)
                .orElseThrow(() -> new IllegalArgumentException("Could not find a PluginContainer with "
                        + "plugin instance: " + plugin)).getId(), typeId);
    }

    /**
     * @param pluginId The plugin that registered the {@link CustomItemDefinition}
     * @param typeId The id to look for
     * @return The definition, if one is registered.
     */
    Optional<CustomItemDefinition<CustomItem>> getDefinition(String pluginId, String typeId);

    /**
     * @param itemStack The ItemStack to wrap
     * @return The wrapped ItemStack, if it is a registered custom item.
     */
    default Optional<CustomItem> getCustomItem(ItemStack itemStack) {
        return getDefinition(itemStack).flatMap(definition -> definition.wrapIfPossible(itemStack));
    }

    default Optional<CustomBlock> getCustomBlock(Location<World> location) {
        return getCustomBlock(Block.of(location));
    }

    Optional<CustomBlock> getCustomBlock(Block block);

    /**
     * Loads the custom item indexes
     */
    void loadRegistry();

    /**
     * Saves the custom item indexes
     */
    void saveRegistry();

    void finalize();
}
