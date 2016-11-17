package cz.creeper.customitemlibrary.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.CustomTool;
import lombok.ToString;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ToString
public class CustomToolRegistry implements CustomItemRegistry<CustomTool, CustomToolDefinition> {
    public static final String FILE_NAME = "toolRegistry.conf";
    public static final String NODE_TEXTURE_IDS = "textureIds";
    private static final CustomToolRegistry INSTANCE = new CustomToolRegistry();
    private final BiMap<Integer, String> durabilityToTextureId = HashBiMap.create();

    private CustomToolRegistry() {}

    @Override
    public void register(CustomToolDefinition definition) {
        definition.getTextureIds().stream()
                .filter(textureId -> !durabilityToTextureId.containsValue(textureId))
                .forEach(textureId -> durabilityToTextureId.put(getAvailableDurability(), textureId));
    }

    /**
     * Returns the first available durability of shears.
     * The durability of 0 is skipped to preserve the vanilla item with the unbreakable property enabled.
     *
     * @return The first available durability
     */
    private int getAvailableDurability() {
        if(durabilityToTextureId.size() <= 0)
            return 1;

        List<Integer> durabilities = Lists.newArrayList(durabilityToTextureId.keySet());

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
        durabilityToTextureId.clear();

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

            ConfigurationNode textureIdsNode = rootNode.getNode(NODE_TEXTURE_IDS);

            for (Map.Entry<Object, ? extends ConfigurationNode> entry : textureIdsNode.getChildrenMap().entrySet()) {
                String rawDurability = (String) entry.getKey();
                int durability;

                try {
                    durability = Integer.valueOf(rawDurability);
                } catch (NumberFormatException e) {
                    CustomItemLibrary.getInstance().getLogger()
                            .warn("Could not parse the durability '" + rawDurability + "', skipping.");
                    continue;
                }

                String textureId = entry.getValue().getString();

                if (durability <= 0) {
                    CustomItemLibrary.getInstance().getLogger()
                            .warn("Found a non-positive durability '" + durability + "', skipping.");
                    continue;
                } else if(durability >= CustomToolDefinition.getNumberOfUses()) {
                     CustomItemLibrary.getInstance().getLogger()
                            .warn("Found a texture with an overflowing durability of '" + durability + "', skipping.");
                    continue;
                }

                durabilityToTextureId.put(durability, textureId);
            }
        } catch(Throwable t) {
            CustomItemLibrary.getInstance().getLogger()
                    .error("Could not load the custom tool registry, aborting and creating a new, empty one.");
            t.printStackTrace();
            durabilityToTextureId.clear();
        }
    }

    @Override
    public void save(Path directory) {
        Path configPath = directory.resolve(FILE_NAME);
        ConfigurationLoader<CommentedConfigurationNode> loader =
                HoconConfigurationLoader.builder().setPath(configPath).build();
        ConfigurationOptions options = CustomItemLibrary.getInstance().getDefaultConfigurationOptions();
        CommentedConfigurationNode rootNode = loader.createEmptyNode(options);
        CommentedConfigurationNode textureIdsNode = rootNode.getNode(NODE_TEXTURE_IDS);

        textureIdsNode.setComment("DO NOT EDIT THIS FILE MANUALLY UNLESS YOU ARE ABSOLUTELY SURE ABOUT WHAT YOU ARE DOING!");

        for(Map.Entry<Integer, String> entry : durabilityToTextureId.entrySet()) {
            int durability = entry.getKey();
            String textureId = entry.getValue();

            textureIdsNode.getNode(Integer.toString(durability)).setValue(textureId);
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

    @Override
    public void generateResourcePack(Path directory) {
        CustomItemLibrary.getInstance().getService().getDefinitionMap().values().stream()
                .filter(definition -> definition instanceof CustomToolDefinition)
                .map(CustomToolDefinition.class::cast)
                .forEach(definition ->
                    definition.getPlugin().ifPresent(plugin ->
                        definition.getTextures().forEach(texture -> {
                            String assetPath = CustomToolDefinition.getTexturePath(texture);
                            String filePath = CustomToolDefinition.getAssetPrefix(plugin) + assetPath;
                            Optional<Asset> optionalAsset = Sponge.getAssetManager().getAsset(plugin, assetPath);

                            if(!optionalAsset.isPresent()) {
                                CustomItemLibrary.getInstance().getLogger()
                                        .warn("Could not locate a texture for plugin '"
                                              + plugin.getName() + "' with path '" + filePath + "'.");
                                return;
                            }

                            Asset asset = optionalAsset.get();
                            Path outputFile = CustomItemServiceImpl.getDirectoryResourcePack()
                                    .resolve(Paths.get(filePath));

                            try {
                                Files.createDirectories(outputFile.getParent());

                                if(Files.exists(outputFile))
                                    Files.delete(outputFile);

                                asset.copyToFile(outputFile);
                            } catch(IOException e) {
                                CustomItemLibrary.getInstance().getLogger()
                                        .warn("Could not copy texture a texture from assets (" + filePath + "): " + e.getLocalizedMessage());
                            }
                        })
                    )
                );
    }

    public Optional<Integer> getDurability(PluginContainer plugin, String texture) {
        return getDurabilityById(plugin.getId() + CustomItemDefinition.ID_SEPARATOR + texture);
    }

    public Optional<Integer> getDurabilityById(String textureId) {
        return Optional.ofNullable(durabilityToTextureId.inverse().get(textureId));
    }

    public Optional<String> getTextureId(int durability) {
        return Optional.ofNullable(durabilityToTextureId.get(durability));
    }

    public Optional<String> getTexture(int durability) {
        return getTextureId(durability).map(textureId ->
                textureId.substring(textureId.indexOf(CustomItemDefinition.ID_SEPARATOR) + 1));
    }

    public static CustomToolRegistry getInstance() {
        return INSTANCE;
    }
}
