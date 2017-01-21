package cz.creeper.customitemlibrary.feature.item;

import cz.creeper.customitemlibrary.feature.AbstractCustomModelledFeature;
import lombok.NonNull;
import org.spongepowered.api.item.inventory.ItemStack;

public abstract class AbstractCustomItem<T extends CustomItemDefinition<? extends AbstractCustomItem<T>>> extends AbstractCustomModelledFeature<T> implements CustomItem<T> {
    @NonNull
    private ItemStack itemStack;

    public AbstractCustomItem(ItemStack itemStack, T definition) {
        super(definition);
        this.itemStack = itemStack;
    }

    @Override
    public ItemStack getDataHolder() {
        return itemStack;
    }
}
