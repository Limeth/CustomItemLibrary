package cz.creeper.customitemlibrary.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.spongepowered.api.item.inventory.ItemStack;

@AllArgsConstructor
public abstract class AbstractCustomItem<I extends AbstractCustomItem<I, T>, T extends CustomItemDefinition<I>> implements CustomItem {
    @Getter
    private ItemStack itemStack;

    @Getter
    private T definition;
}
