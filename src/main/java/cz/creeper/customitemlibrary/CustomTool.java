package cz.creeper.customitemlibrary;

import cz.creeper.customitemlibrary.registry.CustomItemDefinition;
import cz.creeper.customitemlibrary.registry.CustomToolRegistry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.inventory.ItemStack;

@AllArgsConstructor
@ToString
public class CustomTool implements CustomItem {
    @Getter
    private ItemStack itemStack;

    @Getter
    private CustomItemDefinition definition;

    public String getTexture() {
        int durability = itemStack.get(Keys.ITEM_DURABILITY)
                .orElseThrow(() -> new IllegalStateException("Could not get the durability of a custom tool."));
        CustomToolRegistry registry = CustomToolRegistry.getInstance();

        return registry.getTexture(durability)
                .orElseThrow(() -> new IllegalStateException("Could not retrieve the texture of a custom tool."));
    }

    public boolean setTexture(String texture) {
        CustomToolRegistry registry = CustomToolRegistry.getInstance();
        int durability = registry.getDurability(texture)
                .orElseThrow(() -> new IllegalArgumentException("No custom tool with such texture registered: " + texture));

        return itemStack.offer(Keys.ITEM_DURABILITY, durability).isSuccessful();
    }
}
