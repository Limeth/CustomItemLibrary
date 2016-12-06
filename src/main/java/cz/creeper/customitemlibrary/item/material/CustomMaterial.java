package cz.creeper.customitemlibrary.item.material;

import cz.creeper.customitemlibrary.item.AbstractCustomItem;
import cz.creeper.mineskinsponge.SkinRecord;
import org.spongepowered.api.item.inventory.ItemStack;

public class CustomMaterial extends AbstractCustomItem<CustomMaterial, CustomMaterialDefinition> {
    public CustomMaterial(ItemStack itemStack, CustomMaterialDefinition definition) {
        super(itemStack, definition);
    }

    public String getTexture() {
        CustomMaterialRegistry registry = CustomMaterialRegistry.getInstance();
        String textureId = registry.getTextureId(
                SkinRecord.of(getItemStack()).orElseThrow(
                        () -> new IllegalStateException("Could not create a SkinRecord out of a CustomMaterial ItemStack.")
                )
        );
        String pluginId = CustomMaterialDefinition.getPluginId(textureId);

        if(!getDefinition().getPluginId().equals(pluginId))
            throw new IllegalStateException("This plugin is not the owner of this texture. Have two plugins registered the same texture?");

        return CustomMaterialDefinition.getTexture(textureId);
    }

    public void setTexture(String texture) {
        CustomMaterialRegistry registry = CustomMaterialRegistry.getInstance();
        String textureId = CustomMaterialDefinition.getTextureId(getDefinition().getPluginId(), texture);
        SkinRecord skin = registry.getSkin(textureId);

        skin.apply(getItemStack());
    }
}
