package cz.creeper.customitemlibrary.item.tool;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.CustomItemServiceImpl;
import cz.creeper.customitemlibrary.item.CustomItemRegistry;
import cz.creeper.customitemlibrary.util.SortedList;
import cz.creeper.customitemlibrary.util.Util;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class CustomToolRegistry implements CustomItemRegistry<CustomTool, CustomToolDefinition> {
    public static final String FILE_NAME = "toolRegistry.conf";
    public static final String NODE_MODEL_IDS = "modelIds";
    private static final CustomToolRegistry INSTANCE = new CustomToolRegistry();
    private final Map<DurabilityIdentifier, String> durabilityIdToModelId = Maps.newHashMap();
    private final Map<ItemType, BiMap<Integer, String>> typeToDurabilityToModelId = Maps.newHashMap();

    @Override
    public void register(CustomToolDefinition definition) {
        definition.getModelIds().stream()
                .forEach(modelId -> {
                    ItemType itemType = definition.getItemStackSnapshot().getType();
                    BiMap<Integer, String> durabilityToModelId = typeToDurabilityToModelId.computeIfAbsent(itemType, k -> HashBiMap.create());

                    // Is the model already registered? If so, skip.
                    if(durabilityToModelId.containsValue(modelId))
                        return;

                    int availableDurability = getAvailableDurability(itemType);

                    durabilityToModelId.put(availableDurability, modelId);
                    durabilityIdToModelId.put(new DurabilityIdentifier(itemType, availableDurability, true), modelId);
                });
    }

    /**
     * Returns the first available durability of shears.
     * The durability of 0 is skipped to preserve the vanilla item with the unbreakable property enabled.
     *
     * @return The first available durability
     */
    private int getAvailableDurability(ItemType itemType) {
        if(durabilityIdToModelId.size() <= 0)
            return 1;

        BiMap<Integer, String> durabilityToModelId = typeToDurabilityToModelId.get(itemType);

        if(durabilityToModelId == null || durabilityToModelId.size() == 0)
            return 1;

        List<Integer> durabilities = Lists.newArrayList(durabilityToModelId.keySet());

        Collections.sort(durabilities);

        int durability = Integer.MIN_VALUE;

        if(durabilities.get(durabilities.size() - 1) > durabilities.size()) {
            int expected = 1;

            for(int current : durabilities) {
                if(current != expected) {
                    durability = expected;
                    break;
                }

                expected++;
            }
        } else {
            durability = durabilities.size() + 1;
        }

        if(durability <= 0)
            throw new IllegalStateException("This should be unreachable!");

        int numberOfUses = CustomToolDefinition.getNumberOfUses(itemType)
                        .orElseThrow(() -> new IllegalStateException("Could not access the max number of uses."));

        if(durability >= numberOfUses)
            throw new IllegalStateException("The number of custom tools exceeded the limit of " + (numberOfUses - 1) + ".");

        return durability;
    }

    @Override
    public void load(Path directory) {
        durabilityIdToModelId.clear();
        typeToDurabilityToModelId.clear();

        try {
            Path configPath = directory.resolve(FILE_NAME);

            if (!Files.isRegularFile(configPath))
                return;

            ConfigurationLoader<CommentedConfigurationNode> loader =
                    HoconConfigurationLoader.builder().setPath(configPath).build();
            ConfigurationOptions options = CustomItemLibrary.getInstance().getDefaultConfigurationOptions();
            ConfigurationNode rootNode;

            try {
                rootNode = loader.load(options);
            } catch (IOException e) {
                rootNode = loader.createEmptyNode(options);
            }

            ConfigurationNode modelIdsNode = rootNode.getNode(NODE_MODEL_IDS);

            for (Map.Entry<Object, ? extends ConfigurationNode> entry : modelIdsNode.getChildrenMap().entrySet()) {
                String rawDurabilityId = (String) entry.getKey();
                DurabilityIdentifier durabilityId;

                try {
                    durabilityId = DurabilityIdentifier.parse(rawDurabilityId);
                } catch (Throwable e) {  // God, forgive me for this sin
                    CustomItemLibrary.getInstance().getLogger()
                            .warn("Could not parse the durability '" + rawDurabilityId + "', skipping.");
                    e.printStackTrace();
                    continue;
                }

                // Add to registry
                String modelId = entry.getValue().getString();

                durabilityIdToModelId.put(durabilityId, modelId);

                BiMap<Integer, String> durabilityToModelId = typeToDurabilityToModelId.computeIfAbsent(durabilityId.getItemType(), k -> HashBiMap.create());

                durabilityToModelId.put(durabilityId.getDurability(), modelId);
            }
        } catch(Throwable t) {
            CustomItemLibrary.getInstance().getLogger()
                    .error("Could not load the custom tool registry, aborting and creating a new, empty one.");
            t.printStackTrace();
            durabilityIdToModelId.clear();
            typeToDurabilityToModelId.clear();
        }
    }

    @Override
    public void save(Path directory) {
        Path configPath = directory.resolve(FILE_NAME);
        ConfigurationLoader<CommentedConfigurationNode> loader =
                HoconConfigurationLoader.builder().setPath(configPath).build();
        ConfigurationOptions options = CustomItemLibrary.getInstance().getDefaultConfigurationOptions();
        CommentedConfigurationNode rootNode = loader.createEmptyNode(options);
        CommentedConfigurationNode modelIdsNode = rootNode.getNode(NODE_MODEL_IDS);

        modelIdsNode.setComment("DO NOT EDIT THIS FILE MANUALLY UNLESS YOU ARE ABSOLUTELY SURE ABOUT WHAT YOU ARE DOING!");

        for(Map.Entry<DurabilityIdentifier, String> entry : durabilityIdToModelId.entrySet()) {
            DurabilityIdentifier durability = entry.getKey();
            String modelId = entry.getValue();

            modelIdsNode.getNode(durability.toString()).setValue(modelId);
        }

        try {
            Files.createDirectories(configPath.getParent());

            if(Files.exists(configPath))
                Files.delete(configPath);

            Files.createFile(configPath);
            loader.save(rootNode);
        } catch(IOException e) {
            e.printStackTrace();
            CustomItemLibrary.getInstance().getLogger()
                    .warn("Could not save the custom tool registry: " + e.getLocalizedMessage());
        }
    }

    @AllArgsConstructor
    private static class ModelPredicate implements Comparable<ModelPredicate> {
        private final PluginContainer plugin;
        private final int durability;
        private final String model;

        @Override
        public int compareTo(ModelPredicate o) {
            return Integer.compare(durability, o.durability);
        }
    }

    @Override
    public void generateResourcePack(Path directory) {
        // TODO broken, fix.
        Map<ItemType, SortedList<ModelPredicate>> predicateMap = Maps.newHashMap();

        CustomItemLibrary.getInstance().getService().getDefinitionMap().values().stream()
                .filter(definition -> definition instanceof CustomToolDefinition)
                .map(CustomToolDefinition.class::cast)
                .forEach(definition ->
                    definition.getPlugin().ifPresent(plugin -> {
                        definition.getModels().forEach(model -> {
                            ItemType itemType = definition.getItemStackSnapshot().getType();
                            int durability = getDurability(itemType, plugin, model)
                                    .orElseThrow(() -> new IllegalStateException("Could not access the durability of model '" + model + "'."));
                            ModelPredicate predicate = new ModelPredicate(plugin, durability, model);
                            SortedList<ModelPredicate> predicates = predicateMap.get(durability);

                            if(predicates == null) {
                                predicates = SortedList.create(Comparator.reverseOrder());
                                predicateMap.put(itemType, predicates);
                            }

                            if(!predicates.containsElement(predicate))
                                predicates.add(predicate);
                        });

                        definition.getAssets().forEach(asset -> copyAsset(plugin, asset));
                    })
                );

        for(Map.Entry<ItemType, BiMap<Integer, String>> entry : typeToDurabilityToModelId.entrySet()) {
            ItemType itemType = entry.getKey();
            BiMap<Integer, String> durabilityToModelId = entry.getValue();
            JsonArray modelOverrides = new JsonArray();

            for(Map.Entry<Integer, String> pair : durabilityToModelId.entrySet()) {
                int durability = pair.getKey();
                String modelId = pair.getValue();
                String pluginId = Util.getNamespaceFromId(modelId);
                String model = Util.getValueFromId(modelId);
                String assetPath = CustomToolDefinition.getModelPath(model);

                Sponge.getPluginManager().getPlugin(pluginId).ifPresent(plugin -> copyAsset(plugin, assetPath));

                double damage = 1.0 - (double) durability / (double) CustomToolDefinition.getNumberOfUses(itemType)
                        .orElseThrow(() -> new IllegalStateException("Could not access the max number of uses."));

                JsonObject modelPredicate = new JsonObject();
                modelPredicate.addProperty("damaged", 0);
                modelPredicate.addProperty("damage", damage);

                JsonObject modelJson = new JsonObject();
                modelJson.add("predicate", modelPredicate);
                modelJson.addProperty("model", getToolModelIdentifier(pluginId, model));
                modelOverrides.add(modelJson);
            }

            String typeId = itemType.getId();
            String typeName = Util.getValueFromId(typeId);
            String namespace = Util.getNamespaceFromId(typeId);
            JsonObject defaultModelPredicate = new JsonObject();
            defaultModelPredicate.addProperty("damaged", 1);
            defaultModelPredicate.addProperty("damage", 0.0);

            JsonObject defaultModel = new JsonObject();
            defaultModel.add("predicate", defaultModelPredicate);
            defaultModel.addProperty("model", getItemModelIdentifier(null, typeName));
            modelOverrides.add(defaultModel);

            JsonObject modelTextures = new JsonObject();
            modelTextures.addProperty("layer0", getItemTextureIdentifier(null, typeName));

            JsonObject descriptorRoot = new JsonObject();
            descriptorRoot.addProperty("parent", getItemModelIdentifier(null, "handheld"));
            descriptorRoot.add("textures", modelTextures);
            descriptorRoot.add("overrides", modelOverrides);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Path modelPath = CustomItemServiceImpl.getDirectoryResourcePack()
                    .resolve("assets").resolve(namespace).resolve("models")
                    .resolve("item").resolve(typeName + ".json");

            try {
                Files.createDirectories(modelPath.getParent());

                try (Writer writer = new FileWriter(modelPath.toFile())) {
                    gson.toJson(descriptorRoot, writer);
                }
            } catch (IOException e) {
                CustomItemLibrary.getInstance().getLogger()
                        .error("Could not write a model file for custom tools.");
                e.printStackTrace();
            }
        }
    }

    private static String getToolModelIdentifier(String pluginId, String tool) {
        return getFileIdentifier(pluginId, "tools", tool);
    }

    private static String getItemTextureIdentifier(String pluginId, String texture) {
        return getFileIdentifier(pluginId, "items", texture);
    }

    private static String getItemModelIdentifier(String pluginId, String model) {
        return getFileIdentifier(pluginId, "item", model);
    }

    private static String getFileIdentifier(String pluginId, String directory, String file) {
        return (pluginId != null ? pluginId : "minecraft") + ":" + directory + "/" + file;
    }

    private static void copyAsset(PluginContainer plugin, String assetPath) {
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

    public Optional<Integer> getDurability(ItemType itemType, PluginContainer plugin, String model) {
        return getDurabilityByModelId(itemType, Util.getId(plugin.getId(), model));
    }

    public Optional<Integer> getDurabilityByModelId(ItemType itemType, String modelId) {
        return Optional.ofNullable(typeToDurabilityToModelId.get(itemType))
                .flatMap(durabilityToModelId -> Optional.ofNullable(durabilityToModelId.inverse().get(modelId)));
    }

    public Optional<String> getModelId(ItemType itemType, int durability) {
        BiMap<Integer, String> durabilityToModelId = typeToDurabilityToModelId.get(itemType);

        if(durabilityToModelId == null)
            return Optional.empty();

        return Optional.ofNullable(durabilityToModelId.get(durability));
    }

    public static CustomToolRegistry getInstance() {
        return INSTANCE;
    }
}
