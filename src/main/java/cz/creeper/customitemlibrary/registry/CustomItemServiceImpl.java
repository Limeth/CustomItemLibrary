package cz.creeper.customitemlibrary.registry;

import com.google.common.collect.Maps;
import cz.creeper.customitemlibrary.CustomItem;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import lombok.ToString;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.item.inventory.ItemStack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ToString
public class CustomItemServiceImpl implements CustomItemService {
    public static final String DIRECTORY_NAME_REGISTRIES = "registries";
    public static final String DIRECTORY_NAME_RESOURCEPACK = "resourcepack";
    public static final String FILE_NAME_PACK = "pack.mcmeta";
    private final CustomItemRegistryMap registryMap = new CustomItemRegistryMap();
    private final HashMap<String, CustomItemDefinition> definitionMap = Maps.newHashMap();

    public CustomItemServiceImpl() {
        registryMap.put(CustomToolDefinition.class, CustomToolRegistry.getInstance());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I extends CustomItem, T extends CustomItemDefinition<I>> void register(T definition) {
        Optional<CustomItemRegistry<I, T>> registry = registryMap.get(definition);

        if(!registry.isPresent())
            throw new IllegalArgumentException("Invalid definition type.");

        String id = definition.getId();

        if(definitionMap.containsKey(id))
            throw new IllegalStateException("A custom item definition with ID \"" + id + "\" is already registered!");

        registry.get().register(definition);
        definitionMap.put(id, definition);
    }

    @Override
    public Map<String, CustomItemDefinition> getDefinitionMap() {
        return Collections.unmodifiableMap(definitionMap);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<CustomItem> getCustomItem(ItemStack itemStack) {
        return getDefinition(itemStack).flatMap(definition -> definition.wrapIfPossible(itemStack));
    }

    @Override
    public void loadDictionary() {
        Path directory = getDirectoryRegistries();

        registryMap.values().forEach(registry -> registry.load(directory));
    }

    @Override
    public void saveDictionary() {
        Path directory = getDirectoryRegistries();

        registryMap.values().forEach(registry -> registry.save(directory));
    }

    public void generateResourcePack() {
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
