package cz.creeper.customitemlibrary;

import com.google.common.collect.*;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.inventory.ItemStack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CustomToolRegistry implements CustomItemRegistry<CustomTool, CustomToolDefinition> {
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

        int durability = itemStack.get(Keys.ITEM_DURABILITY)
                .orElseThrow(() -> new IllegalStateException("Odd, the shears don't have a durability value."));

        if(!durabilityToDefinition.containsKey(durability))
            return Optional.empty();

        return Optional.of(durabilityToDefinition.get(durability).createItem());
    }

    @Override
    public void load() {
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

    }

    @Override
    public void save() {
        Path configPath = getPathToConfig();
        ConfigurationLoader<CommentedConfigurationNode> loader =
                HoconConfigurationLoader.builder().setPath(configPath).build();
        ConfigurationOptions options = CustomItemLibrary.getDefaultConfigurationOptions();
        ConfigurationNode rootNode = loader.createEmptyNode(options);

        // TODO
        // TIL: LinkedHashMaps preserve order.
        Map<CustomToolDefinition, List<Integer>> definitions = Maps.newLinkedHashMap();

        for(Map.Entry<Integer, CustomToolDefinition> entry : durabilityToDefinition.entrySet()) {
            CustomToolDefinition definition = entry.getValue();
            int durability = entry.getKey();

            if(definitions.containsKey(definition))
                definitions.get(definition).add(durability);
            else
                definitions.put(definition, Lists.newArrayList(durability));
        }

        ConfigurationNode definitionsNode = rootNode.getNode("definitions");
        ConfigurationNode definitionsToDurabilities = rootNode.getNode("definitionsToDurabilities");
        int i = 0;

        for(Map.Entry<CustomToolDefinition, List<Integer>> entry : definitions.entrySet()) {
            definitionsNode.getNode(i).setValue(definitionsNode);
            definitionsToDurabilities.getNode(i).setValue(entry.getValue());
            i++;
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
                .resolveSibling("registries").resolve("toolRegistry.conf");
    }

    public Optional<Integer> getDurability(String texture) {
        return Optional.ofNullable(durabilityToTexture.inverse().get(texture));
    }

    public Optional<CustomToolDefinition> getDefinition(int durability) {
        return Optional.ofNullable(durabilityToDefinition.get(durability));
    }

    public Optional<String> getTexture(int durability) {
        return Optional.ofNullable(durabilityToTexture.get(durability));
    }

    public static CustomToolRegistry getInstance() {
        return INSTANCE;
    }
}
