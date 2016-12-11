package cz.creeper.customitemlibrary.item.block;

import cz.creeper.customitemlibrary.item.material.CustomMaterial;
import cz.creeper.customitemlibrary.item.material.CustomMaterialDefinition;
import org.spongepowered.api.item.inventory.ItemStack;

public class CustomBlock extends CustomMaterial {
    public CustomBlock(ItemStack itemStack, CustomMaterialDefinition definition) {
        super(itemStack, definition);
    }
}
