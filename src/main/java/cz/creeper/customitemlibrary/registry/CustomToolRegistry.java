package cz.creeper.customitemlibrary.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.CustomTool;
import cz.creeper.customitemlibrary.data.CustomItemLibraryKeys;
import lombok.ToString;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.item.inventory.ItemStack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

@ToString
public class CustomToolRegistry implements CustomItemRegistry<CustomTool, CustomToolDefinition> {
    public static final String FILE_NAME = "toolRegistry.conf";
    public static final String NODE_DEFINITIONS = "definitions";
    public static final String NODE_TEXTURES = "textures";
    private static final CustomToolRegistry INSTANCE = new CustomToolRegistry();
    private final Map<String, CustomToolDefinition> idToDefinition = Maps.newHashMap();
    private final BiMap<Integer, String> durabilityToTexture = HashBiMap.create();

    private CustomToolRegistry() {}

    @Override
    public void register(CustomToolDefinition definition) {
        idToDefinition.put(definition.getId(), definition);

        for(String texture : definition.getTextures())
            if(!durabilityToTexture.containsValue(texture))
                durabilityToTexture.put(durabilityToTexture.size(), texture);
    }

    @Override
    public Optional<CustomTool> wrapIfPossible(ItemStack itemStack) {
        if(itemStack.getItem() != CustomToolDefinition.getItemType())
            return Optional.empty();

        return itemStack.get(CustomItemLibraryKeys.CUSTOM_ITEM_ID).map(idToDefinition::get).flatMap(Optional::ofNullable)
                .map(definition -> new CustomTool(itemStack, definition));
    }

    @Override
    public void load() {
        idToDefinition.clear();
        durabilityToTexture.clear();

        Path configPath = getPathToConfig();

        if(!Files.isRegularFile(configPath))
            return;

        ConfigurationLoader<CommentedConfigurationNode> loader =
                HoconConfigurationLoader.builder().setPath(configPath).build();
        ConfigurationOptions options = CustomItemLibrary.getDefaultConfigurationOptions();
        ConfigurationNode rootNode;

        try {
            rootNode = loader.load(options);
        } catch(IOException e) {
            rootNode = loader.createEmptyNode(options);
        }

        ConfigurationNode definitionsNode = rootNode.getNode(NODE_DEFINITIONS);

        for(Object rawDefinition : definitionsNode.getChildrenList()) {
            CustomToolDefinition definition = (CustomToolDefinition) rawDefinition;

            idToDefinition.put(definition.getId(), definition);
        }

        ConfigurationNode texturesNode = rootNode.getNode(NODE_TEXTURES);

        for(Map.Entry<Object, ? extends ConfigurationNode> entry : texturesNode.getChildrenMap().entrySet()) {
            String texture = (String) entry.getKey();
            int durability = entry.getValue().getInt();

            durabilityToTexture.put(durability, texture);
        }
    }

    @Override
    public void save() {
        Path configPath = getPathToConfig();
        ConfigurationLoader<CommentedConfigurationNode> loader =
                HoconConfigurationLoader.builder().setPath(configPath).build();
        ConfigurationOptions options = CustomItemLibrary.getDefaultConfigurationOptions();
        ConfigurationNode rootNode = loader.createEmptyNode(options);
        ConfigurationNode definitionsNode = rootNode.getNode(NODE_DEFINITIONS);

        definitionsNode.setValue(idToDefinition.values());

        ConfigurationNode texturesNode = rootNode.getNode(NODE_TEXTURES);

        for(Map.Entry<Integer, String> entry : durabilityToTexture.entrySet()) {
            int durability = entry.getKey();
            String texture = entry.getValue();

            texturesNode.getNode(texture).setValue(durability);
        }

        try {
            loader.save(rootNode);
        } catch(IOException e) {
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

    public Optional<CustomToolDefinition> getDefinition(String id) {
        return Optional.ofNullable(idToDefinition.get(id));
    }

    public Optional<String> getTexture(int durability) {
        return Optional.ofNullable(durabilityToTexture.get(durability));
    }

    public static CustomToolRegistry getInstance() {
        return INSTANCE;
    }
}
