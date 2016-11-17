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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ToString
public class CustomToolRegistry implements CustomItemRegistry<CustomTool, CustomToolDefinition> {
    public static final String FILE_NAME = "toolRegistry.conf";
    public static final String NODE_DEFINITIONS = "definitions";
    public static final String NODE_TEXTURES = "textures";
    private static final CustomToolRegistry INSTANCE = new CustomToolRegistry();
    private final BiMap<Integer, String> durabilityToTexture = HashBiMap.create();

    private CustomToolRegistry() {}

    @Override
    public void register(CustomToolDefinition definition) {
        definition.getTextures().stream()
                .filter(texture -> !durabilityToTexture.containsValue(texture))
                .forEach(texture -> durabilityToTexture.put(getAvailableDurability(), texture));
    }

    /**
     * Returns the first available durability of shears.
     * The durability of 0 is skipped to preserve the vanilla item with the unbreakable property enabled.
     *
     * @return The first available durability
     */
    private int getAvailableDurability() {
        if(durabilityToTexture.size() <= 0)
            return 1;

        List<Integer> durabilities = Lists.newArrayList(durabilityToTexture.keySet());

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

        // TODO: Check bounds

        return durability;
    }

    @Override
    public void load() {
        durabilityToTexture.clear();

        try {
            Path configPath = getPathToConfig();

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

        /*
        ConfigurationNode definitionsNode = rootNode.getNode(NODE_DEFINITIONS);

        try {
            for(CustomToolDefinition definition : definitionsNode.getList(TypeToken.of(CustomToolDefinition.class))) {
                idToDefinition.put(definition.getId(), definition);
            }
        } catch (ObjectMappingException e) {
            CustomItemLibrary.getInstance().getLogger().error("Could not load the custom tool registry: " + e.getLocalizedMessage());
        }*/

            ConfigurationNode texturesNode = rootNode.getNode(NODE_TEXTURES);

            for (Map.Entry<Object, ? extends ConfigurationNode> entry : texturesNode.getChildrenMap().entrySet()) {
                String rawDurability = (String) entry.getKey();
                int durability;

                try {
                    durability = Integer.valueOf(rawDurability);
                } catch (NumberFormatException e) {
                    CustomItemLibrary.getInstance().getLogger()
                            .warn("Could not parse the durability '" + rawDurability + "', skipping.");
                    continue;
                }

                String texture = entry.getValue().getString();

                if (durability <= 0) {
                    CustomItemLibrary.getInstance().getLogger()
                            .warn("Found a non-positive durability '" + durability + "', skipping.");
                    continue;
                }

                durabilityToTexture.put(durability, texture);
            }
        } catch(Throwable t) {
            CustomItemLibrary.getInstance().getLogger()
                    .error("Could not load the custom tool registry, aborting and creating a new, empty one.");
            t.printStackTrace();
            durabilityToTexture.clear();
        }
    }

    @Override
    public void save() {
        Path configPath = getPathToConfig();
        ConfigurationLoader<CommentedConfigurationNode> loader =
                HoconConfigurationLoader.builder().setPath(configPath).build();
        ConfigurationOptions options = CustomItemLibrary.getInstance().getDefaultConfigurationOptions();
        CommentedConfigurationNode rootNode = loader.createEmptyNode(options);

        /*
        CommentedConfigurationNode definitionsNode = rootNode.getNode(NODE_DEFINITIONS);


        try {
            definitionsNode.setValue(new TypeToken<List<CustomToolDefinition>>() {}, Lists.newArrayList(idToDefinition.values()));
        } catch (ObjectMappingException e) {
            throw new RuntimeException(e);
        }
        */

        CommentedConfigurationNode texturesNode = rootNode.getNode(NODE_TEXTURES);

        texturesNode.setComment("DO NOT EDIT THIS FILE MANUALLY UNLESS YOU ARE ABSOLUTELY SURE ABOUT WHAT YOU ARE DOING!");

        for(Map.Entry<Integer, String> entry : durabilityToTexture.entrySet()) {
            int durability = entry.getKey();
            String texture = entry.getValue();

            texturesNode.getNode(Integer.toString(durability)).setValue(texture);
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

    public Path getPathToConfig() {
        return CustomItemLibrary.getInstance().getConfigPath()
                .resolveSibling(CustomItemServiceImpl.DIRECTORY_NAME).resolve(FILE_NAME);
    }

    public Optional<Integer> getDurability(String texture) {
        return Optional.ofNullable(durabilityToTexture.inverse().get(texture));
    }

    public Optional<String> getTexture(int durability) {
        return Optional.ofNullable(durabilityToTexture.get(durability));
    }

    public static CustomToolRegistry getInstance() {
        return INSTANCE;
    }
}
