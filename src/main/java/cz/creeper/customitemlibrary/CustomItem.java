package cz.creeper.customitemlibrary;

import org.spongepowered.api.item.inventory.ItemStack;

public interface CustomItem {
    ItemStack getItemStack();
    CustomItemDefinition getDefinition();
}
