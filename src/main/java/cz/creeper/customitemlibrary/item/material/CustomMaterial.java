package cz.creeper.customitemlibrary.item.material;

import cz.creeper.customitemlibrary.item.AbstractCustomItem;
import org.spongepowered.api.item.inventory.ItemStack;

public class CustomMaterial extends AbstractCustomItem<CustomMaterial, CustomMaterialDefinition> {
    public CustomMaterial(ItemStack itemStack, CustomMaterialDefinition definition) {
        super(itemStack, definition);
    }

}
