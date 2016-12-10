package cz.creeper.customitemlibrary.item.material;

import cz.creeper.customitemlibrary.item.AbstractCustomItem;
import cz.creeper.mineskinsponge.SkinRecord;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;

public class CustomMaterial extends AbstractCustomItem<CustomMaterial, CustomMaterialDefinition> {
    public CustomMaterial(ItemStack itemStack, CustomMaterialDefinition definition) {
        super(itemStack, definition);
    }

    @Override
    protected Optional<String> resolveCurrentModel() {
        CustomMaterialRegistry registry = CustomMaterialRegistry.getInstance();
        SkinRecord skinRecord = SkinRecord.of(getItemStack())
                .orElseThrow(() -> new IllegalStateException("Could not create a SkinRecord out of a"
                        + " CustomMaterial ItemStack."));
        PluginContainer pluginContainer = getDefinition().getPluginContainer();
        return registry.getTexture(pluginContainer, skinRecord);
    }

    @Override
    protected void applyModel(String texture) {
        CustomMaterialRegistry registry = CustomMaterialRegistry.getInstance();
        CustomMaterialDefinition definition = getDefinition();
        PluginContainer pluginContainer = definition.getPluginContainer();
        SkinRecord skin = registry.getSkin(pluginContainer, texture)
                .orElseThrow(() -> new IllegalArgumentException("Could not access the SkinRecord for texture: " + texture));

        skin.apply(getItemStack());
    }
}
