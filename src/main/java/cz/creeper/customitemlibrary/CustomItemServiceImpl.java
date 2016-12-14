package cz.creeper.customitemlibrary;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Maps;
import cz.creeper.customitemlibrary.data.CustomFeatureData;
import cz.creeper.customitemlibrary.data.CustomItemLibraryKeys;
import cz.creeper.customitemlibrary.feature.CustomFeature;
import cz.creeper.customitemlibrary.feature.CustomFeatureDefinition;
import cz.creeper.customitemlibrary.feature.CustomFeatureRegistry;
import cz.creeper.customitemlibrary.feature.CustomFeatureRegistryMap;
import cz.creeper.customitemlibrary.feature.DurabilityRegistry;
import cz.creeper.customitemlibrary.feature.block.CustomBlock;
import cz.creeper.customitemlibrary.feature.block.CustomBlockDefinition;
import cz.creeper.customitemlibrary.feature.item.material.CustomMaterialDefinition;
import cz.creeper.customitemlibrary.feature.item.material.CustomMaterialRegistry;
import cz.creeper.customitemlibrary.feature.item.tool.CustomToolDefinition;
import cz.creeper.customitemlibrary.feature.item.tool.CustomToolRegistry;
import cz.creeper.customitemlibrary.util.Block;
import lombok.ToString;
import lombok.val;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ToString
public class CustomItemServiceImpl implements CustomItemService {
    public static final String DIRECTORY_NAME_REGISTRIES = "registries";
    public static final String DIRECTORY_NAME_RESOURCEPACK = "resourcepack";
    public static final String FILE_NAME_PACK = "pack.mcmeta";
    private final CustomFeatureRegistryMap registryMap = new CustomFeatureRegistryMap();
    private final Map<String, Map<String, CustomFeatureDefinition<? extends CustomFeature>>> pluginIdsToTypeIdsToDefinitions = Maps.newHashMap();

    public CustomItemServiceImpl() {
        registryMap.put(CustomToolDefinition.class, CustomToolRegistry.getInstance());
        registryMap.put(CustomMaterialDefinition.class, CustomMaterialRegistry.getInstance());

        registryMap.values().forEach(registry -> Sponge.getEventManager().registerListeners(
                CustomItemLibrary.getInstance(),
                registry
        ));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I extends CustomFeature<T>, T extends CustomFeatureDefinition<I>> void register(T definition) {
        Optional<CustomFeatureRegistry<I, T>> registry = registryMap.get(definition);

        if(!registry.isPresent())
            throw new IllegalArgumentException("Invalid definition type.");

        val typeIdsToDefinitions = getTypeIdsToDefinitions(definition.getPluginContainer());

        if(typeIdsToDefinitions.containsKey(definition.getTypeId()))
            throw new IllegalStateException("A custom feature definition with ID \"" + definition.getTypeId() + "\" is already registered!");

        registry.get().register(definition);
        typeIdsToDefinitions.put(definition.getTypeId(), definition);
    }

    @Override
    public void loadRegistry() {
        Path directory = getDirectoryRegistries();

        DurabilityRegistry.getInstance().load(directory);
    }

    @Override
    public void saveRegistry() {
        Path directory = getDirectoryRegistries();

        DurabilityRegistry.getInstance().save(directory);
    }

    @Override
    public void finalize() {
        // It doesn't work with a method reference
        //noinspection Convert2MethodRef
        registryMap.values().forEach(registry -> registry.finalize());
    }

    public Path generateResourcePack() {
        Path directory = getDirectoryResourcePack();
        Path packFile = directory.resolve(FILE_NAME_PACK);

        try {
            Files.createDirectories(directory);

            if (Files.exists(packFile))
                Files.delete(packFile);

            Asset pack = Sponge.getAssetManager().getAsset(CustomItemLibrary.getInstance(), FILE_NAME_PACK)
                    .orElseThrow(() -> new IllegalStateException("Could not access the 'pack.mcmeta' asset."));
            pack.copyToFile(packFile);
        } catch(IOException e) {
            CustomItemLibrary.getInstance().getLogger()
                    .warn("Could not create the 'pack.mcmeta' file.");
            e.printStackTrace();
        }

        getDefinitions().forEach(definition ->
            definition.getAssets().forEach(asset -> copyAsset(definition.getPluginContainer(), asset))
        );

        registryMap.values().forEach(registry -> registry.generateResourcePack(directory));

        return directory;
    }

    @Override
    public Set<CustomFeatureDefinition<? extends CustomFeature>> getDefinitions() {
        return pluginIdsToTypeIdsToDefinitions.values().stream()
                .flatMap(typeIdsToDefinitions -> typeIdsToDefinitions.values().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<CustomBlockDefinition<? extends CustomBlock>> getDefinition(Block block) {
        return block.getChunk().flatMap(chunk -> {
            Optional<Location<World>> location = block.getLocation();

            if(!location.isPresent() || location.get().getBlockType() != CustomBlock.BLOCK_TYPE_CUSTOM)
                return Optional.empty();

            Vector3i blockPosition = block.getPosition();
            AABB aabb = new AABB(blockPosition, blockPosition.add(Vector3i.ONE));
            Set<Entity> entities = chunk.getIntersectingEntities(aabb, entity -> entity.get(CustomFeatureData.class).isPresent());
            Iterator<Entity> entityIterator = entities.iterator();

            if(!entityIterator.hasNext())
                return Optional.empty();

            Entity dataHolder = entityIterator.next();

            while(entityIterator.hasNext()) {
                entityIterator.next().remove();
            }

            String pluginId = dataHolder.get(CustomItemLibraryKeys.CUSTOM_FEATURE_PLUGIN_ID).get();
            String typeId = dataHolder.get(CustomItemLibraryKeys.CUSTOM_FEATURE_TYPE_ID).get();

            return getDefinition(pluginId, typeId)
                    .filter(CustomBlockDefinition.class::isInstance)
                    .map(CustomBlockDefinition.class::cast);
        });
    }

    @Override
    public Optional<CustomFeatureDefinition<? extends CustomFeature>> getDefinition(String pluginId, String typeId) {
        Map<String, CustomFeatureDefinition<? extends CustomFeature>> typeIdsToDefinitions = pluginIdsToTypeIdsToDefinitions.get(pluginId);

        if(typeIdsToDefinitions == null)
            return Optional.empty();

        return Optional.ofNullable(typeIdsToDefinitions.get(typeId));
    }

    private Map<String, CustomFeatureDefinition<? extends CustomFeature>> getTypeIdsToDefinitions(PluginContainer pluginContainer) {
        return getTypeIdsToDefinitions(pluginContainer.getId());
    }

    private Map<String, CustomFeatureDefinition<? extends CustomFeature>> getTypeIdsToDefinitions(String pluginId) {
        return pluginIdsToTypeIdsToDefinitions.computeIfAbsent(pluginId, k -> Maps.newHashMap());
    }

    public static Path getDirectoryRegistries() {
        return CustomItemLibrary.getInstance().getConfigPath()
                .resolveSibling(DIRECTORY_NAME_REGISTRIES);
    }

    public static Path getDirectoryResourcePack() {
        return CustomItemLibrary.getInstance().getConfigPath()
                .resolveSibling(DIRECTORY_NAME_RESOURCEPACK);
    }

    public static void copyAsset(PluginContainer plugin, String assetPath) {
        String filePath = CustomToolDefinition.getAssetPrefix(plugin) + assetPath;
        Optional<Asset> optionalAsset = Sponge.getAssetManager().getAsset(plugin, assetPath);

        if (!optionalAsset.isPresent()) {
            CustomItemLibrary.getInstance().getLogger()
                    .warn("Could not locate an asset for plugin '"
                            + plugin.getName() + "' with path '" + filePath + "'.");
            return;
        }

        Asset asset = optionalAsset.get();
        Path outputFile = CustomItemServiceImpl.getDirectoryResourcePack()
                .resolve(Paths.get(filePath));

        try {
            Files.createDirectories(outputFile.getParent());

            if (Files.exists(outputFile))
                Files.delete(outputFile);

            asset.copyToFile(outputFile);
        } catch (IOException e) {
            CustomItemLibrary.getInstance().getLogger()
                    .warn("Could not copy a file from assets (" + filePath + "): " + e.getLocalizedMessage());
        }
    }
}
