package cz.creeper.customitemlibrary;

import cz.creeper.customitemlibrary.registry.CustomToolDefinition;
import cz.creeper.customitemlibrary.registry.CustomToolRegistry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;

@AllArgsConstructor
@ToString
public class CustomTool implements CustomItem {
    @Getter
    private ItemStack itemStack;

    @Getter
    private CustomToolDefinition definition;

    public String getModel() {
        int durability = itemStack.get(Keys.ITEM_DURABILITY)
                .orElseThrow(() -> new IllegalStateException("Could not get the durability of a custom tool."));
        CustomToolRegistry registry = CustomToolRegistry.getInstance();

        return registry.getModel(itemStack.getItem(), durability)
                .orElseThrow(() -> new IllegalStateException("Could not retrieve the model of a custom tool."));
    }

    public boolean setModel(String model) {
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
