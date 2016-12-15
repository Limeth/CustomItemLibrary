package cz.creeper.customitemlibrary.feature.item.tool;

import cz.creeper.customitemlibrary.feature.CustomFeatureDefinition;
import cz.creeper.customitemlibrary.feature.item.AbstractCustomItem;
import cz.creeper.customitemlibrary.feature.DurabilityRegistry;
import cz.creeper.customitemlibrary.feature.item.CustomItemDefinition;
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
        return resolveCurrentModel(getDataHolder(), getDefinition().getPluginContainer());
    }

    public static Optional<String> resolveCurrentModel(ItemStack itemStack, PluginContainer pluginContainer) {
        int durability = itemStack.get(Keys.ITEM_DURABILITY)
                .orElseThrow(() -> new IllegalStateException("Could not get the durability of a custom tool."));

        return DurabilityRegistry.getInstance().getModelId(itemStack.getItem(), durability).flatMap(model -> {
            if(model.getNamespace().equals(pluginContainer.getId()))
                return Optional.of(model.getValue());
            else
                return Optional.empty();
        });
    }

    @Override
    protected void applyModel(String model) {
        CustomToolDefinition definition = getDefinition();

        applyModel(getDataHolder(), definition.getPluginContainer(), definition.getItemStackSnapshot().getType(), model);
    }

    public static void applyModel(ItemStack itemStack, PluginContainer pluginContainer, ItemType itemType, String model) {
        int durability = DurabilityRegistry.getInstance().getDurability(itemType, pluginContainer, model)
                .orElseThrow(() -> new IllegalArgumentException("No custom tool with such model registered: " + model));

        itemStack.offer(Keys.ITEM_DURABILITY, durability).isSuccessful();
    }
}
