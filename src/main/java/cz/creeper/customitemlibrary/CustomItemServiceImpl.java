package cz.creeper.customitemlibrary;

import com.google.common.collect.Maps;
import cz.creeper.customitemlibrary.feature.item.CustomItem;
import cz.creeper.customitemlibrary.feature.item.CustomItemDefinition;
import cz.creeper.customitemlibrary.feature.CustomFeatureRegistry;
import cz.creeper.customitemlibrary.feature.CustomFeatureRegistryMap;
import cz.creeper.customitemlibrary.feature.DurabilityRegistry;
import cz.creeper.customitemlibrary.feature.block.CustomBlock;
import cz.creeper.customitemlibrary.feature.item.material.CustomMaterialDefinition;
import cz.creeper.customitemlibrary.feature.item.material.CustomMaterialRegistry;
import cz.creeper.customitemlibrary.feature.item.tool.CustomToolDefinition;
import cz.creeper.customitemlibrary.feature.item.tool.CustomToolRegistry;
import cz.creeper.customitemlibrary.util.Block;
import lombok.ToString;
import lombok.val;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private final Map<String, Map<String, CustomItemDefinition<CustomItem>>> pluginIdsToTypeIdsToDefinitions = Maps.newHashMap();

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
    public <I extends CustomItem, T extends CustomItemDefinition<I>> void register(T definition) {
        Optional<CustomFeatureRegistry<I, T>> registry = registryMap.get(definition);

        if(!registry.isPresent())
            throw new IllegalArgumentException("Invalid definition type.");

        val typeIdsToDefinitions = getTypeIdsToDefinitions(definition.getPluginContainer());

        if(typeIdsToDefinitions.containsKey(definition.getTypeId()))
            throw new IllegalStateException("A custom feature definition with ID \"" + definition.getTypeId() + "\" is already registered!");

        registry.get().register(definition);
        typeIdsToDefinitions.put(definition.getTypeId(), (CustomItemDefinition<CustomItem>) definition);
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
        registryMap.values().forEach(CustomFeatureRegistry::finalize);
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
    public Set<CustomItemDefinition<CustomItem>> getDefinitions() {
        return pluginIdsToTypeIdsToDefinitions.values().stream()
                .flatMap(typeIdsToDefinitions -> typeIdsToDefinitions.values().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<CustomItemDefinition<CustomItem>> getDefinition(String pluginId, String typeId) {
        val typeIdsToDefinitions = pluginIdsToTypeIdsToDefinitions.get(pluginId);

        if(typeIdsToDefinitions == null)
            return Optional.empty();

        return Optional.ofNullable(typeIdsToDefinitions.get(typeId));
    }

    @Override
    public Optional<CustomBlock> getCustomBlock(Block block) {
        return null; //TODO
    }

    private Map<String, CustomItemDefinition<CustomItem>> getTypeIdsToDefinitions(PluginContainer pluginContainer) {
        return getTypeIdsToDefinitions(pluginContainer.getId());
    }

    private Map<String, CustomItemDefinition<CustomItem>> getTypeIdsToDefinitions(String pluginId) {
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
