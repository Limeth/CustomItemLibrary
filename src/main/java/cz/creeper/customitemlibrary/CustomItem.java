package cz.creeper.customitemlibrary;

import cz.creeper.customitemlibrary.registry.CustomItemDefinition;
import org.spongepowered.api.item.inventory.ItemStack;

public interface CustomItem {
    ItemStack getItemStack();
    CustomItemDefinition getDefinition();
}
