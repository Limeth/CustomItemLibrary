package cz.creeper.customitemlibrary.feature.item.tool;

import cz.creeper.customitemlibrary.feature.item.AbstractCustomItem;
import cz.creeper.customitemlibrary.feature.DurabilityRegistry;
import lombok.ToString;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;

@ToString
public class CustomTool extends AbstractCustomItem<CustomToolDefinition> {
    public CustomTool(ItemStack itemStack, CustomToolDefinition definition) {
        super(itemStack, definition);
    }

    @Override
    protected Optional<String> resolveCurrentModel() {
        ItemStack itemStack = getItemStack();
        int durability = itemStack.get(Keys.ITEM_DURABILITY)
                .orElseThrow(() -> new IllegalStateException("Could not get the durability of a custom tool."));
        CustomToolRegistry registry = CustomToolRegistry.getInstance();

        return DurabilityRegistry.getInstance().getModelId(itemStack.getItem(), durability).flatMap(model -> {
            if(model.getNamespace().equals(getDefinition().getPluginContainer().getId()))
                return Optional.of(model.getValue());
            else
                return Optional.empty();
        });
    }

    @Override
    protected void applyModel(String model) {
        ItemStack itemStack = getItemStack();
        CustomToolDefinition definition = getDefinition();
        PluginContainer plugin = definition.getPluginContainer();
        CustomToolRegistry registry = CustomToolRegistry.getInstance();
        ItemType itemType = definition.getItemStackSnapshot().getType();
        int durability = DurabilityRegistry.getInstance().getDurability(itemType, plugin, model)
                .orElseThrow(() -> new IllegalArgumentException("No custom tool with such model registered: " + model));

        itemStack.offer(Keys.ITEM_DURABILITY, durability).isSuccessful();
    }
}