package cz.creeper.customitemlibrary.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.CustomTool;
import cz.creeper.customitemlibrary.util.SortedList;
import lombok.AllArgsConstructor;
import lombok.ToString;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@ToString
public class CustomToolRegistry implements CustomItemRegistry<CustomTool, CustomToolDefinition> {
    public static final String FILE_NAME = "toolRegistry.conf";
    public static final String NODE_MODEL_IDS = "modelIds";
    private static final CustomToolRegistry INSTANCE = new CustomToolRegistry();
    private final BiMap<Integer, String> durabilityToModelId = HashBiMap.create();

    private CustomToolRegistry() {}

    @Override
    public void register(CustomToolDefinition definition) {
        definition.getModelIds().stream()
                .filter(modelId -> !durabilityToModelId.containsValue(modelId))
                .forEach(modelId -> durabilityToModelId.put(getAvailableDurability(), modelId));
    }

    /**
     * Returns the first available durability of shears.
     * The durability of 0 is skipped to preserve the vanilla item with the unbreakable property enabled.
     *
     * @return The first available durability
     */
    private int getAvailableDurability() {
        if(durabilityToModelId.size() <= 0)
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

        if(durability >= CustomToolDefinition.getNumberOfUses())
            throw new IllegalStateException("The number of custom tools exceeded the limit of " + (CustomToolDefinition.getNumberOfUses() - 1) + ".");

        return durability;
    }

    @Override
    public void load(Path directory) {
        durabilityToModelId.clear();

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
                String rawDurability = (String) entry.getKey();
                int durability;

                try {
                    durability = Integer.valueOf(rawDurability);
                } catch (NumberFormatException e) {
                    CustomItemLibrary.getInstance().getLogger()
                            .warn("Could not parse the durability '" + rawDurability + "', skipping.");
                    continue;
                }

                String modelId = entry.getValue().getString();

                if (durability <= 0) {
                    CustomItemLibrary.getInstance().getLogger()
                            .warn("Found a non-positive durability '" + durability + "', skipping.");
                    continue;
                } else if(durability >= CustomToolDefinition.getNumberOfUses()) {
                     CustomItemLibrary.getInstance().getLogger()
                            .warn("Found a model with an overflowing durability of '" + durability + "', skipping.");
                    continue;
                }

                durabilityToModelId.put(durability, modelId);
            }
        } catch(Throwable t) {
            CustomItemLibrary.getInstance().getLogger()
                    .error("Could not load the custom tool registry, aborting and creating a new, empty one.");
            t.printStackTrace();
            durabilityToModelId.clear();
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

        for(Map.Entry<Integer, String> entry : durabilityToModelId.entrySet()) {
            int durability = entry.getKey();
            String modelId = entry.getValue();

            modelIdsNode.getNode(Integer.toString(durability)).setValue(modelId);
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
        JsonArray modelOverrides = new JsonArray();
        SortedList<ModelPredicate> predicates = SortedList.create(Comparator.reverseOrder());

        CustomItemLibrary.getInstance().getService().getDefinitionMap().values().stream()
                .filter(definition -> definition instanceof CustomToolDefinition)
                .map(CustomToolDefinition.class::cast)
                .forEach(definition ->
                    definition.getPlugin().ifPresent(plugin -> {
                        definition.getModels().forEach(model -> {
                            int durability = getDurability(plugin, model)
                                    .orElseThrow(() -> new IllegalStateException("Could not access the durability of model '" + model + "'."));
                            ModelPredicate predicate = new ModelPredicate(plugin, durability, model);

                            if(!predicates.containsElement(predicate))
                                predicates.add(predicate);
                        });

                        definition.getAssets().forEach(asset -> copyAsset(plugin, asset));
                    })
                );

        predicates.forEach(predicate -> {
            String assetPath = CustomToolDefinition.getModelPath(predicate.model);

            copyAsset(predicate.plugin, assetPath);

            double damage = 1.0 - (double) predicate.durability / (double) CustomToolDefinition.getNumberOfUses();

            JsonObject modelPredicate = new JsonObject();
            modelPredicate.addProperty("damaged", 0);
            modelPredicate.addProperty("damage", damage);

            JsonObject modelJson = new JsonObject();
            modelJson.add("predicate", modelPredicate);
            modelJson.addProperty("model", getToolModelIdentifier(predicate.plugin, predicate.model));
            modelOverrides.add(modelJson);
        });

        String fullDefaultId = CustomToolDefinition.getItemType().getId();
        String defaultId = fullDefaultId.substring(fullDefaultId.indexOf(CustomItemDefinition.ID_SEPARATOR) + 1);
        JsonObject defaultModelPredicate = new JsonObject();
        defaultModelPredicate.addProperty("damaged", 1);
        defaultModelPredicate.addProperty("damage", 0.0);

        JsonObject defaultModel = new JsonObject();
        defaultModel.add("predicate", defaultModelPredicate);
        defaultModel.addProperty("model", getItemModelIdentifier(null, defaultId));
        modelOverrides.add(defaultModel);

        JsonObject modelTextures = new JsonObject();
        modelTextures.addProperty("layer0", getItemTextureIdentifier(null, defaultId));

        JsonObject descriptorRoot = new JsonObject();
        descriptorRoot.addProperty("parent", getItemModelIdentifier(null, "handheld"));
        descriptorRoot.add("textures", modelTextures);
        descriptorRoot.add("overrides", modelOverrides);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Path modelPath = CustomItemServiceImpl.getDirectoryResourcePack()
                .resolve("assets").resolve("minecraft").resolve("models")
                .resolve("item").resolve("shears.json");

        try {
            Files.createDirectories(modelPath.getParent());

            try(Writer writer = new FileWriter(modelPath.toFile())) {
                gson.toJson(descriptorRoot, writer);
            }
        } catch (IOException e) {
            CustomItemLibrary.getInstance().getLogger()
                    .error("Could not write a model file for custom tools.");
            e.printStackTrace();
        }
    }

    private static String getToolModelIdentifier(PluginContainer plugin, String tool) {
        return getFileIdentifier(plugin, "tools", tool);
    }

    private static String getItemTextureIdentifier(PluginContainer plugin, String texture) {
        return getFileIdentifier(plugin, "items", texture);
    }

    private static String getItemModelIdentifier(PluginContainer plugin, String model) {
        return getFileIdentifier(plugin, "item", model);
    }

    private static String getFileIdentifier(PluginContainer plugin, String directory, String file) {
        return (plugin != null ? plugin.getId() : "minecraft") + ":" + directory + "/" + file;
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
            return;
        }
    }

    public Optional<Integer> getDurability(PluginContainer plugin, String model) {
        return getDurabilityById(plugin.getId() + CustomItemDefinition.ID_SEPARATOR + model);
    }

    public Optional<Integer> getDurabilityById(String modelId) {
        return Optional.ofNullable(durabilityToModelId.inverse().get(modelId));
    }

    public Optional<String> getModelId(int durability) {
        return Optional.ofNullable(durabilityToModelId.get(durability));
    }

    public Optional<String> getModel(int durability) {
        return getModelId(durability).map(modelId ->
                modelId.substring(modelId.indexOf(CustomItemDefinition.ID_SEPARATOR) + 1));
    }

    public static CustomToolRegistry getInstance() {
        return INSTANCE;
    }
}
