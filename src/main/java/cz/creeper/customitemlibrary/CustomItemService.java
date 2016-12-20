package cz.creeper.customitemlibrary;

import com.google.common.collect.Sets;
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
     * Registers a {@link CustomFeatureDefinition}.
     * To construct a {@link CustomFeatureDefinition}, use the static methods on {@link CustomFeatureDefinition}.
     *
     * @param definition The definition to register
     */
    <I extends CustomFeature<T>, T extends CustomFeatureDefinition<I>> void register(T definition);

    /**
     * @return An unmodifiable set of all registered definitions
     */
    default Set<CustomFeatureDefinition<? extends CustomFeature>> getDefinitions() {
        Set<CustomFeatureDefinition<? extends CustomFeature>> result = Sets.newHashSet();

        result.addAll(getItemDefinitions());
        result.addAll(getBlockDefinitions());

        return result;
    }

    /**
     * @return An unmodifiable set of all registered item definitions
     */
    Set<CustomItemDefinition<? extends CustomItem>> getItemDefinitions();

    /**
     * @return An unmodifiable set of all registered block definitions
     */
    Set<CustomBlockDefinition<? extends CustomBlock>> getBlockDefinitions();

    /**
     * @param itemStack The {@link ItemStack} to get the definition of
     * @return The definition, if one is registered.
     */
    default Optional<CustomItemDefinition<? extends CustomItem>> getItemDefinition(ItemStack itemStack) {
        //noinspection unchecked
        return itemStack.get(CustomFeatureData.class)
                .flatMap(data -> getItemDefinition(data.customFeaturePluginId().get(), data.customFeatureTypeId().get()))
                .filter(CustomItemDefinition.class::isInstance)
                .map(CustomItemDefinition.class::cast);
    }

    /**
     * @param location The block to get the definition of
     * @return The definition, if found
     */
    default Optional<CustomBlockDefinition<? extends CustomBlock>> getBlockDefinition(Location<World> location) {
        return getBlockDefinition(Block.of(location));
    }

    /**
     * @param block The block to get the definition of
     * @return The definition, if found
     */
    Optional<CustomBlockDefinition<? extends CustomBlock>> getBlockDefinition(Block block);

    /**
     * @param plugin The instance of the plugin that registered the {@link CustomItemDefinition}
     * @param typeId The id to look for
     * @return The definition, if one is registered.
     */
    default Optional<CustomItemDefinition<? extends CustomItem>> getItemDefinition(Object plugin, String typeId) {
        return getItemDefinition(Sponge.getPluginManager().fromInstance(plugin)
                .orElseThrow(() -> new IllegalArgumentException("Could not find a PluginContainer with "
                        + "plugin instance: " + plugin)).getId(), typeId);
    }

    /**
     * @param pluginId The plugin that registered the {@link CustomItemDefinition}
     * @param typeId The id to look for
     * @return The definition, if one is registered.
     */
    Optional<CustomItemDefinition<? extends CustomItem>> getItemDefinition(String pluginId, String typeId);

    /**
     * @param plugin The instance of the plugin that registered the {@link CustomBlockDefinition}
     * @param typeId The id to look for
     * @return The definition, if one is registered.
     */
    default Optional<CustomBlockDefinition<? extends CustomBlock>> getBlockDefinition(Object plugin, String typeId) {
        return getBlockDefinition(Sponge.getPluginManager().fromInstance(plugin)
                .orElseThrow(() -> new IllegalArgumentException("Could not find a PluginContainer with "
                        + "plugin instance: " + plugin)).getId(), typeId);
    }

    /**
     * @param pluginId The plugin that registered the {@link CustomBlockDefinition}
     * @param typeId The id to look for
     * @return The definition, if one is registered.
     */
    Optional<CustomBlockDefinition<? extends CustomBlock>> getBlockDefinition(String pluginId, String typeId);

    /**
     * @param itemStack The ItemStack to wrap
     * @return The wrapped ItemStack, if it is a registered custom item.
     */
    default Optional<? extends CustomItem<?>> getItem(ItemStack itemStack) {
        return getItemDefinition(itemStack)
                .flatMap((CustomItemDefinition<? extends CustomItem> definition) -> definition.wrapIfPossible(itemStack));
    }

    /**
     * @param location The block to wrap
     * @return The wrapped block, if it is a registered custom block.
     */
    default Optional<? extends CustomBlock<?>> getBlock(Location<World> location) {
        return getBlock(Block.of(location));
    }

    /**
     * @param block The block to wrap
     * @return The wrapped block, if it is a registered custom block.
     */
    default Optional<? extends CustomBlock<?>> getBlock(Block block) {
        return getBlockDefinition(block)
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
