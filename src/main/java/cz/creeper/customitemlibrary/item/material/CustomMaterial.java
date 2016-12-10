package cz.creeper.customitemlibrary.item.material;

import cz.creeper.customitemlibrary.item.AbstractCustomItem;
import cz.creeper.customitemlibrary.util.Util;
import cz.creeper.mineskinsponge.SkinRecord;
import org.spongepowered.api.item.inventory.ItemStack;

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
        return registry.getTextureId(skinRecord);
    }

    @Override
    protected void applyModel(String texture) {
        CustomMaterialRegistry registry = CustomMaterialRegistry.getInstance();
        String textureId = Util.getId(getDefinition().getPluginId(), texture);
        SkinRecord skin = registry.getSkin(textureId)
                .orElseThrow(() -> new IllegalArgumentException("Could not access the SkinRecord for texture: " + texture));

        skin.apply(getItemStack());
    }
}
