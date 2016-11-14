package cz.creeper.customitemlibrary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.spongepowered.api.item.inventory.ItemStack;

@AllArgsConstructor
public class CustomTool implements CustomItem {
    @Getter
    private ItemStack itemStack;

    @Getter
    private CustomItemDefinition definition;
}
