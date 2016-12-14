package cz.creeper.customitemlibrary.feature;

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
import cz.creeper.customitemlibrary.feature.item.CustomItem;
import cz.creeper.customitemlibrary.feature.item.CustomItemDefinition;
import cz.creeper.customitemlibrary.feature.item.tool.CustomToolDefinition;
import cz.creeper.customitemlibrary.util.Identifier;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DurabilityRegistry {
    public static final String FILE_NAME = "toolRegistry.conf";
    public static final String NODE_MODEL_IDS = "modelIds";
    private static final DurabilityRegistry INSTANCE = new DurabilityRegistry();
    private final BiMap<DurabilityIdentifier, Identifier> durabilityIdToModelId = HashBiMap.create();
    private final Map<ItemType, BiMap<Integer, Identifier>> typeToDurabilityToModelId = Maps.newHashMap();

    public void register(ItemType itemType, CustomItemDefinition<? extends CustomItem> definition) {
        definition.getModels().forEach(model -> {
            BiMap<Integer, Identifier> durabilityToModelId = typeToDurabilityToModelId.computeIfAbsent(itemType, k -> HashBiMap.create());
            Identifier modelId = new Identifier(definition.getPluginContainer().getId(), model);

            // Is the model already registered? If so, skip.
            if (durabilityToModelId.containsValue(modelId))
                return;

            int availableDurability = getAvailableDurability(itemType);

            durabilityToModelId.put(availableDurability, modelId);
            durabilityIdToModelId.put(new DurabilityIdentifier(itemType, availableDurability, true), modelId);
        });
    }

    /**
     * Returns the first available durability of shears.
     * The durability of 0 is skipped to preserve the vanilla feature with the unbreakable property enabled.
     *
     * @return The first available durability
     */
    private int getAvailableDurability(ItemType itemType) {
        if (durabilityIdToModelId.size() <= 0)
            return 1;

        BiMap<Integer, Identifier> durabilityToModelId = typeToDurabilityToModelId.get(itemType);

        if (durabilityToModelId == null || durabilityToModelId.size() == 0)
            return 1;

        List<Integer> durabilities = Lists.newArrayList(durabilityToModelId.keySet());

        Collections.sort(durabilities);

        int durability = Integer.MIN_VALUE;

        if (durabilities.get(durabilities.size() - 1) > durabilities.size()) {
            int expected = 1;

            for (int current : durabilities) {
                if (current != expected) {
                    durability = expected;
                    break;
                }

                expected++;
            }
        } else {
            durability = durabilities.size() + 1;
        }

        if (durability <= 0)
            throw new IllegalStateException("This should be unreachable!");

        int numberOfUses = CustomToolDefinition.getNumberOfUses(itemType)
                .orElseThrow(() -> new IllegalStateException("Could not access the max number of uses."));

        if (durability >= numberOfUses)
            throw new IllegalStateException("The number of custom tools exceeded the limit of " + (numberOfUses - 1) + ".");

        return durability;
    }

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
                Identifier modelId = Identifier.parse(entry.getValue().getString());

                durabilityIdToModelId.put(durabilityId, modelId);

                BiMap<Integer, Identifier> durabilityToModelId = typeToDurabilityToModelId.computeIfAbsent(durabilityId.getItemType(), k -> HashBiMap.create());

                durabilityToModelId.put(durabilityId.getDurability(), modelId);
            }
        } catch (Throwable t) {
            CustomItemLibrary.getInstance().getLogger()
                    .error("Could not load the custom tool registry, aborting and creating a new, empty one.");
            t.printStackTrace();
            durabilityIdToModelId.clear();
            typeToDurabilityToModelId.clear();
        }
    }

    public void save(Path directory) {
        Path configPath = directory.resolve(FILE_NAME);
        ConfigurationLoader<CommentedConfigurationNode> loader =
                HoconConfigurationLoader.builder().setPath(configPath).build();
        ConfigurationOptions options = CustomItemLibrary.getInstance().getDefaultConfigurationOptions();
        CommentedConfigurationNode rootNode = loader.createEmptyNode(options);
        CommentedConfigurationNode modelIdsNode = rootNode.getNode(NODE_MODEL_IDS);

        modelIdsNode.setComment("DO NOT EDIT THIS FILE MANUALLY UNLESS YOU ARE ABSOLUTELY SURE ABOUT WHAT YOU ARE DOING!");

        for (Map.Entry<DurabilityIdentifier, Identifier> entry : durabilityIdToModelId.entrySet()) {
            DurabilityIdentifier durability = entry.getKey();
            Identifier modelId = entry.getValue();

            modelIdsNode.getNode(durability.toString()).setValue(modelId.toString());
        }

        try {
            Files.createDirectories(configPath.getParent());

            if (Files.exists(configPath))
                Files.delete(configPath);

            Files.createFile(configPath);
            loader.save(rootNode);
        } catch (IOException e) {
            e.printStackTrace();
            CustomItemLibrary.getInstance().getLogger()
                    .warn("Could not save the custom tool registry: " + e.getLocalizedMessage());
        }
    }

    public void generateResourcePack(Path directory) {
        for (Map.Entry<ItemType, BiMap<Integer, Identifier>> entry : typeToDurabilityToModelId.entrySet()) {
            ItemType itemType = entry.getKey();
            BiMap<Integer, Identifier> durabilityToModelId = entry.getValue();
            JsonArray modelOverrides = new JsonArray();

            for (Map.Entry<Integer, Identifier> pair : durabilityToModelId.entrySet()) {
                int durability = pair.getKey();
                Identifier modelId = pair.getValue();
                String pluginId = modelId.getNamespace();
                String model = modelId.getValue();

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
            String namespace = Identifier.getNamespaceFromIdString(typeId);
            String typeName = Identifier.getValueFromIdString(typeId);
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
                    .resolve("feature").resolve(typeName + ".json");

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
        return getFileIdentifier(pluginId, "feature", model);
    }

    private static String getFileIdentifier(String pluginId, String directory, String file) {
        return (pluginId != null ? pluginId : "minecraft") + ":" + directory + "/" + file;
    }

    public Optional<Integer> getDurability(ItemType itemType, PluginContainer plugin, String model) {
        return getDurabilityByModelId(itemType, new Identifier(plugin.getId(), model));
    }

    public Optional<Integer> getDurabilityByModelId(ItemType itemType, Identifier modelId) {
        return Optional.ofNullable(typeToDurabilityToModelId.get(itemType))
                .flatMap(durabilityToModelId -> Optional.ofNullable(durabilityToModelId.inverse().get(modelId)));
    }

    public Optional<Identifier> getModelId(ItemType itemType, int durability) {
        BiMap<Integer, Identifier> durabilityToModelId = typeToDurabilityToModelId.get(itemType);

        if (durabilityToModelId == null)
            return Optional.empty();

        return Optional.ofNullable(durabilityToModelId.get(durability));
    }

    public static DurabilityRegistry getInstance() {
        return INSTANCE;
    }
}