package cz.creeper.customitemlibrary;

import cz.creeper.customitemlibrary.data.CustomFeatureData;
import cz.creeper.customitemlibrary.feature.CustomFeature;
import cz.creeper.customitemlibrary.feature.CustomFeatureDefinition;
import cz.creeper.customitemlibrary.feature.block.CustomBlock;
import cz.creeper.customitemlibrary.feature.block.CustomBlockDefinition;
import cz.creeper.customitemlibrary.feature.item.CustomItem;
import cz.creeper.customitemlibrary.feature.item.CustomItemDefinition;
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
    <I extends CustomFeature<T>, T extends CustomFeatureDefinition<I>> void register(T definition);

    /**
     * @return An unmodifiable set of all registered definitions
     */
    Set<CustomFeatureDefinition<? extends CustomFeature>> getDefinitions();

    /**
     * @param itemStack The {@link ItemStack} to get the definition of
     * @return The definition, if one is registered.
     */
    default Optional<CustomItemDefinition<? extends CustomItem>> getDefinition(ItemStack itemStack) {
        //noinspection unchecked
        return itemStack.get(CustomFeatureData.class)
                .flatMap(data -> getDefinition(data.customFeaturePluginId().get(), data.customFeatureTypeId().get()))
                .filter(CustomItemDefinition.class::isInstance)
                .map(CustomItemDefinition.class::cast);
    }

    default Optional<CustomBlockDefinition<? extends CustomBlock>> getDefinition(Location<World> location) {
        return getDefinition(Block.of(location));
    }

    Optional<CustomBlockDefinition<? extends CustomBlock>> getDefinition(Block block);

    /**
     * @param plugin The instance of the plugin that registered the {@link CustomItemDefinition}
     * @param typeId The id to look for
     * @return The definition, if one is registered.
     */
    default Optional<CustomFeatureDefinition<? extends CustomFeature>> getDefinition(Object plugin, String typeId) {
        return getDefinition(Sponge.getPluginManager().fromInstance(plugin)
                .orElseThrow(() -> new IllegalArgumentException("Could not find a PluginContainer with "
                        + "plugin instance: " + plugin)).getId(), typeId);
    }

    /**
     * @param pluginId The plugin that registered the {@link CustomItemDefinition}
     * @param typeId The id to look for
     * @return The definition, if one is registered.
     */
    Optional<CustomFeatureDefinition<? extends CustomFeature>> getDefinition(String pluginId, String typeId);

    /**
     * @param itemStack The ItemStack to wrap
     * @return The wrapped ItemStack, if it is a registered custom feature.
     */
    default Optional<? extends CustomItem> getCustomItem(ItemStack itemStack) {
        return getDefinition(itemStack)
                .flatMap((CustomItemDefinition<? extends CustomItem> definition) -> definition.wrapIfPossible(itemStack));
    }

    default Optional<? extends CustomBlock> getCustomBlock(Location<World> location) {
        return getCustomBlock(Block.of(location));
    }

    default Optional<? extends CustomBlock> getCustomBlock(Block block) {
        return getDefinition(block)
                .flatMap((CustomBlockDefinition<? extends CustomBlock> definition) -> definition.wrapIfPossible(block));
    }

    /**
     * Loads the custom feature indexes
     */
    void loadRegistry();

    /**
     * Saves the custom feature indexes
     */
    void saveRegistry();
}
