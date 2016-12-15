package cz.creeper.customitemlibrary.feature.item;

import cz.creeper.customitemlibrary.data.CustomItemLibraryKeys;
import cz.creeper.customitemlibrary.feature.AbstractCustomFeature;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractCustomItem<T extends CustomItemDefinition<? extends AbstractCustomItem<T>>> extends AbstractCustomFeature<T> implements CustomItem<T> {
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
