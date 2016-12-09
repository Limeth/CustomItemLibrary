package cz.creeper.customitemlibrary.item.tool;

import cz.creeper.customitemlibrary.item.AbstractCustomItem;
import cz.creeper.customitemlibrary.util.Util;
import lombok.ToString;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;
import java.util.stream.Collectors;

@ToString
public class CustomTool extends AbstractCustomItem<CustomTool, CustomToolDefinition> {
    public CustomTool(ItemStack itemStack, CustomToolDefinition definition) {
        super(itemStack, definition);
    }

    public String getModel() {
        ItemStack itemStack = getItemStack();
        int durability = itemStack.get(Keys.ITEM_DURABILITY)
                .orElseThrow(() -> new IllegalStateException("Could not get the durability of a custom tool."));
        CustomToolRegistry registry = CustomToolRegistry.getInstance();

        Optional<String> modelId = registry.getModelId(itemStack.getItem(), durability);

        textureResolution:
        if(modelId.isPresent()) {
            String pluginId = Util.getNamespaceFromId(modelId.get());

            if (!getDefinition().getPluginId().equals(pluginId))
                break textureResolution;

            String model = Util.getValueFromId(modelId.get());

            if (!getDefinition().getModels().contains(model))
                break textureResolution;

            return model;
        }

        // If the texture is invalid, change the texture to the default one.
        String defaultModel = getDefinition().getDefaultModel();

        setModel(defaultModel);

        return defaultModel;
    }

    public void setModel(String model) {
        if(!getDefinition().getModels().contains(model))
            throw new IllegalArgumentException("This custom tool has no model called '" + model
                    + "'. Available, defined models: "
                    + getDefinition().getModels().stream().collect(Collectors.joining(", ")));

        ItemStack itemStack = getItemStack();
        CustomToolDefinition definition = getDefinition();
        PluginContainer plugin = definition.getPlugin()
                .orElseThrow(() -> new IllegalStateException("Could not access the plugin owning this custom tool: "
                                                             + definition.getPluginId()));
        CustomToolRegistry registry = CustomToolRegistry.getInstance();
        ItemType itemType = definition.getItemStackSnapshot().getType();
        int durability = registry.getDurability(itemType, plugin, model)
                .orElseThrow(() -> new IllegalArgumentException("No custom tool with such model registered: " + model));

        itemStack.offer(Keys.ITEM_DURABILITY, durability).isSuccessful();
    }
}
