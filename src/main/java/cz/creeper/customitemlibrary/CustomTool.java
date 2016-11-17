package cz.creeper.customitemlibrary;

import cz.creeper.customitemlibrary.registry.CustomToolDefinition;
import cz.creeper.customitemlibrary.registry.CustomToolRegistry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;

@AllArgsConstructor
@ToString
public class CustomTool implements CustomItem {
    @Getter
    private ItemStack itemStack;

    @Getter
    private CustomToolDefinition definition;

    public String getTexture() {
        int durability = itemStack.get(Keys.ITEM_DURABILITY)
                .orElseThrow(() -> new IllegalStateException("Could not get the durability of a custom tool."));
        CustomToolRegistry registry = CustomToolRegistry.getInstance();

        return registry.getTexture(durability)
                .orElseThrow(() -> new IllegalStateException("Could not retrieve the texture of a custom tool."));
    }

    public boolean setTexture(String texture) {
        PluginContainer plugin = definition.getPlugin()
                .orElseThrow(() -> new IllegalStateException("Could not access the plugin owning this custom tool: "
                                                             + definition.getPluginId()));
        CustomToolRegistry registry = CustomToolRegistry.getInstance();
        int durability = registry.getDurability(plugin, texture)
                .orElseThrow(() -> new IllegalArgumentException("No custom tool with such texture registered: " + texture));

        return itemStack.offer(Keys.ITEM_DURABILITY, durability).isSuccessful();
    }
}
