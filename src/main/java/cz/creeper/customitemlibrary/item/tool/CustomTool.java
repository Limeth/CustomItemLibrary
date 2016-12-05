package cz.creeper.customitemlibrary.item.tool;

import cz.creeper.customitemlibrary.item.AbstractCustomItem;
import lombok.ToString;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;

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

        return registry.getModel(itemStack.getItem(), durability)
                .orElseThrow(() -> new IllegalStateException("Could not retrieve the model of a custom tool."));
    }

    public boolean setModel(String model) {
        ItemStack itemStack = getItemStack();
        CustomToolDefinition definition = getDefinition();
        PluginContainer plugin = definition.getPlugin()
                .orElseThrow(() -> new IllegalStateException("Could not access the plugin owning this custom tool: "
                                                             + definition.getPluginId()));
        CustomToolRegistry registry = CustomToolRegistry.getInstance();
        ItemType itemType = definition.getItemStackSnapshot().getType();
        int durability = registry.getDurability(itemType, plugin, model)
                .orElseThrow(() -> new IllegalArgumentException("No custom tool with such model registered: " + model));

        return itemStack.offer(Keys.ITEM_DURABILITY, durability).isSuccessful();
    }
}
