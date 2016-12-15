package cz.creeper.customitemlibrary.feature.item.tool;

import cz.creeper.customitemlibrary.feature.DurabilityRegistry;
import cz.creeper.customitemlibrary.feature.item.AbstractCustomItem;
import lombok.ToString;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;

@ToString
public class CustomTool extends AbstractCustomItem<CustomToolDefinition> {
    public CustomTool(ItemStack itemStack, CustomToolDefinition definition) {
        super(itemStack, definition);
    }

    @Override
    protected Optional<String> resolveCurrentModel() {
        return DurabilityRegistry.resolveCurrentModel(getDataHolder(), getDefinition().getPluginContainer());
    }

    @Override
    protected void applyModel(String model) {
        CustomToolDefinition definition = getDefinition();

        DurabilityRegistry.applyModel(getDataHolder(), definition.getPluginContainer(), definition.getItemStackSnapshot().getType(), model);
    }
}
