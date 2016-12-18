package cz.creeper.customitemlibrary;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
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
import cz.creeper.customitemlibrary.feature.block.simple.SimpleCustomBlockDefinition;
import cz.creeper.customitemlibrary.feature.block.simple.SimpleCustomBlockRegistry;
import cz.creeper.customitemlibrary.feature.item.CustomItem;
import cz.creeper.customitemlibrary.feature.item.CustomItemDefinition;
import cz.creeper.customitemlibrary.feature.item.material.CustomMaterialDefinition;
import cz.creeper.customitemlibrary.feature.item.material.CustomMaterialRegistry;
import cz.creeper.customitemlibrary.feature.item.tool.CustomToolDefinition;
import cz.creeper.customitemlibrary.feature.item.tool.CustomToolRegistry;
import cz.creeper.customitemlibrary.util.Block;
import lombok.ToString;
import lombok.val;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.Identifiable;
import org.spongepowered.api.world.Chunk;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@ToString
public class CustomItemServiceImpl implements CustomItemService {
    public static final String DIRECTORY_NAME_REGISTRIES = "registries";
    public static final String DIRECTORY_NAME_RESOURCEPACK = "resourcepack";
    public static final String FILE_NAME_PACK = "pack.mcmeta";
    private final CustomFeatureRegistryMap registryMap = new CustomFeatureRegistryMap();
    private final Map<String, Map<String, CustomItemDefinition<? extends CustomItem>>> pluginIdsToTypeIdsToItemDefinitions = Maps.newHashMap();
    private final Map<String, Map<String, CustomBlockDefinition<? extends CustomBlock>>> pluginIdsToTypeIdsToBlockDefinitions = Maps.newHashMap();
    private final Map<Block, Optional<UUID>> blockToArmorStand = Maps.newHashMap();

    public CustomItemServiceImpl() {
        registryMap.put(CustomToolDefinition.class, CustomToolRegistry.getInstance());
        registryMap.put(CustomMaterialDefinition.class, CustomMaterialRegistry.getInstance());
        registryMap.put(SimpleCustomBlockDefinition.class, SimpleCustomBlockRegistry.getInstance());

        registryMap.values().forEach(registry -> Sponge.getEventManager().registerListeners(
                CustomItemLibrary.getInstance(),
                registry
        ));

        Sponge.getEventManager().registerListeners(CustomItemLibrary.getInstance(), this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I extends CustomFeature<T>, T extends CustomFeatureDefinition<I>> void register(T definition) {
        Preconditions.checkArgument(!"minecraft".equals(definition.getPluginContainer().getId()),
                "The plugin id must not be 'minecraft'.");

        Optional<CustomFeatureRegistry<I, T>> registry = registryMap.get(definition);

        if(!registry.isPresent())
            throw new IllegalArgumentException("Invalid definition type.");

        if(definition instanceof CustomItemDefinition) {
            val typeIdsToDefinitions = getTypeIdsToItemDefinitions(definition.getPluginContainer());

            if (typeIdsToDefinitions.containsKey(definition.getTypeId()))
                throw new IllegalStateException("A custom feature definition with ID \"" + definition.getTypeId() + "\" is already registered!");

            typeIdsToDefinitions.put(definition.getTypeId(), (CustomItemDefinition<? extends CustomItem>) definition);
        } else if(definition instanceof CustomBlockDefinition) {
            val typeIdsToDefinitions = getTypeIdsToBlockDefinitions(definition.getPluginContainer());

            if (typeIdsToDefinitions.containsKey(definition.getTypeId()))
                throw new IllegalStateException("A custom feature definition with ID \"" + definition.getTypeId() + "\" is already registered!");

            typeIdsToDefinitions.put(definition.getTypeId(), (CustomBlockDefinition<? extends CustomBlock>) definition);
        } else {
            throw new IllegalArgumentException("Invalid custom definition type. It must extend either CustomItemDefinition or CustomBlockDefinition.");
        }

        registry.get().register(definition);
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

    public void prepare() {
        // It doesn't work with a method reference
        //noinspection Convert2MethodRef
        registryMap.values().forEach(registry -> registry.prepare());
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
            definition.getAssets().forEach(asset -> copyAsset(definition, asset))
        );

        DurabilityRegistry.getInstance().generateResourcePack(directory);

        return directory;
    }

    @Override
    public Set<CustomItemDefinition<? extends CustomItem>> getItemDefinitions() {
        return pluginIdsToTypeIdsToItemDefinitions.values().stream()
                .flatMap(typeIdsToDefinitions -> typeIdsToDefinitions.values().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<CustomBlockDefinition<? extends CustomBlock>> getBlockDefinitions() {
        return pluginIdsToTypeIdsToBlockDefinitions.values().stream()
                .flatMap(typeIdsToDefinitions -> typeIdsToDefinitions.values().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<CustomBlockDefinition<? extends CustomBlock>> getBlockDefinition(Block block) {
        return getArmorStandAt(block).flatMap(armorStand -> {
            String pluginId = armorStand.get(CustomItemLibraryKeys.CUSTOM_FEATURE_PLUGIN_ID).get();
            String typeId = armorStand.get(CustomItemLibraryKeys.CUSTOM_FEATURE_TYPE_ID).get();

            return getBlockDefinition(pluginId, typeId)
                    .filter(CustomBlockDefinition.class::isInstance)
                    .map(CustomBlockDefinition.class::cast);
        });
    }

    public void setArmorStandAt(Block block, ArmorStand armorStand) {
        blockToArmorStand.put(block, Optional.of(armorStand.getUniqueId()));
    }

    public Optional<ArmorStand> getArmorStandAt(Block block) {
        return blockToArmorStand.computeIfAbsent(block, k -> findArmorStandAt(k).map(Identifiable::getUniqueId))
                .flatMap(id -> block.getExtent().flatMap(extent -> extent.getEntity(id)).map(ArmorStand.class::cast));
    }

    public Optional<ArmorStand> findArmorStandAt(Block block) {
        Set<ArmorStand> armorStands = findArmorStandsAt(block);

        if (armorStands.isEmpty())
            return Optional.empty();

        Iterator<ArmorStand> armorStandIterator = armorStands.iterator();
        ArmorStand armorStand = armorStandIterator.next();

        while (armorStandIterator.hasNext()) {
            armorStandIterator.next().remove();
        }

        return Optional.of(armorStand);
    }

    public Set<ArmorStand> findArmorStandsAt(Block block) {
        Chunk chunk = block.getChunk()
                .orElseThrow(() -> new IllegalStateException("Could not access the chunk of this block."));
        Vector3i blockPosition = block.getPosition();
        AABB aabb = new AABB(blockPosition, blockPosition.add(Vector3i.ONE));
        Set<Entity> entities = chunk.getIntersectingEntities(aabb, entity ->
                entity instanceof ArmorStand && entity.get(CustomFeatureData.class).isPresent());
        Iterator<Entity> entityIterator = entities.iterator();

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(entityIterator, Spliterator.ORDERED), false)
                .map(ArmorStand.class::cast)
                .collect(Collectors.toSet());
    }

    public boolean removeArmorStandsAt(Block block) {
        findArmorStandsAt(block).forEach(Entity::remove);
        return blockToArmorStand.remove(block) != null;
    }

    @Override
    public Optional<CustomItemDefinition<? extends CustomItem>> getItemDefinition(String pluginId, String typeId) {
        Map<String, CustomItemDefinition<? extends CustomItem>> typeIdsToDefinitions = pluginIdsToTypeIdsToItemDefinitions.get(pluginId);

        if(typeIdsToDefinitions == null)
            return Optional.empty();

        return Optional.ofNullable(typeIdsToDefinitions.get(typeId));
    }

    @Override
    public Optional<CustomBlockDefinition<? extends CustomBlock>> getBlockDefinition(String pluginId, String typeId) {
        Map<String, CustomBlockDefinition<? extends CustomBlock>> typeIdsToDefinitions = pluginIdsToTypeIdsToBlockDefinitions.get(pluginId);

        if(typeIdsToDefinitions == null)
            return Optional.empty();

        return Optional.ofNullable(typeIdsToDefinitions.get(typeId));
    }

    private Map<String, CustomItemDefinition<? extends CustomItem>> getTypeIdsToItemDefinitions(PluginContainer pluginContainer) {
        return getTypeIdsToItemDefinitions(pluginContainer.getId());
    }

    private Map<String, CustomItemDefinition<? extends CustomItem>> getTypeIdsToItemDefinitions(String pluginId) {
        return pluginIdsToTypeIdsToItemDefinitions.computeIfAbsent(pluginId, k -> Maps.newHashMap());
    }

    private Map<String, CustomBlockDefinition<? extends CustomBlock>> getTypeIdsToBlockDefinitions(PluginContainer pluginContainer) {
        return getTypeIdsToBlockDefinitions(pluginContainer.getId());
    }

    private Map<String, CustomBlockDefinition<? extends CustomBlock>> getTypeIdsToBlockDefinitions(String pluginId) {
        return pluginIdsToTypeIdsToBlockDefinitions.computeIfAbsent(pluginId, k -> Maps.newHashMap());
    }

    public void copyAsset(CustomFeatureDefinition<? extends CustomFeature> definition, String assetPath) {
        PluginContainer plugin = definition.getPluginContainer();
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

            Files.createFile(outputFile);

            CustomFeatureRegistry registry = (CustomFeatureRegistry) registryMap.get(definition.getClass()).get();

            try (
                    ReadableByteChannel input = Channels.newChannel(asset.getUrl().openStream());
                    SeekableByteChannel output = Files.newByteChannel(outputFile, StandardOpenOption.WRITE)
            ) {
                //noinspection unchecked
                registry.writeAsset(definition, assetPath, input, output);
            }
        } catch (IOException e) {
            e.printStackTrace();
            CustomItemLibrary.getInstance().getLogger()
                    .warn("Could not copy a file from assets (" + filePath + "): " + e.getLocalizedMessage());
        }
    }

    @Listener(order = Order.BEFORE_POST)
    public void onChangeBlockBreak(ChangeBlockEvent event) {
        event.getTransactions().stream()
                .filter(Transaction::isValid)
                .forEach(blockSnapshotTransaction -> {
            BlockSnapshot original = blockSnapshotTransaction.getOriginal();
            original.getLocation().map(Block::of).ifPresent(this::removeArmorStandsAt);
        });
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
