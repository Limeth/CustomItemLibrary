package cz.creeper.customitemlibrary;

import com.google.common.collect.Maps;
import cz.creeper.customitemlibrary.item.CustomItem;
import cz.creeper.customitemlibrary.item.CustomItemDefinition;
import cz.creeper.customitemlibrary.item.CustomItemRegistry;
import cz.creeper.customitemlibrary.item.CustomItemRegistryMap;
import cz.creeper.customitemlibrary.item.material.CustomMaterialDefinition;
import cz.creeper.customitemlibrary.item.material.CustomMaterialRegistry;
import cz.creeper.customitemlibrary.item.tool.CustomToolDefinition;
import cz.creeper.customitemlibrary.item.tool.CustomToolRegistry;
import lombok.ToString;
import lombok.val;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ToString
public class CustomItemServiceImpl implements CustomItemService {
    public static final String DIRECTORY_NAME_REGISTRIES = "registries";
    public static final String DIRECTORY_NAME_RESOURCEPACK = "resourcepack";
    public static final String FILE_NAME_PACK = "pack.mcmeta";
    private final CustomItemRegistryMap registryMap = new CustomItemRegistryMap();
    private final Map<String, Map<String, CustomItemDefinition<CustomItem>>> pluginIdsToTypeIdsToDefinitions = Maps.newHashMap();

    public CustomItemServiceImpl() {
        registryMap.put(CustomToolDefinition.class, CustomToolRegistry.getInstance());
        registryMap.put(CustomMaterialDefinition.class, CustomMaterialRegistry.getInstance());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I extends CustomItem, T extends CustomItemDefinition<I>> void register(T definition) {
        Optional<CustomItemRegistry<I, T>> registry = registryMap.get(definition);

        if(!registry.isPresent())
            throw new IllegalArgumentException("Invalid definition type.");

        val typeIdsToDefinitions = getTypeIdsToDefinitions(definition.getPluginContainer());

        if(typeIdsToDefinitions.containsKey(definition.getTypeId()))
            throw new IllegalStateException("A custom item definition with ID \"" + definition.getTypeId() + "\" is already registered!");

        registry.get().register(definition);
        typeIdsToDefinitions.put(definition.getTypeId(), (CustomItemDefinition<CustomItem>) definition);
    }

    @Override
    public void loadRegistry() {
        Path directory = getDirectoryRegistries();

        registryMap.values().forEach(registry -> registry.load(directory));
    }

    @Override
    public void saveRegistry() {
        Path directory = getDirectoryRegistries();

        registryMap.values().forEach(registry -> registry.save(directory));
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
}
