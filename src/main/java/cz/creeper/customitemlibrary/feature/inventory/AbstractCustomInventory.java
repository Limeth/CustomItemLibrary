package cz.creeper.customitemlibrary.feature.inventory;

import com.google.common.base.Preconditions;
import cz.creeper.customitemlibrary.feature.AbstractCustomFeature;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.item.inventory.Inventory;

@EqualsAndHashCode(callSuper = true)
@Getter
public abstract class AbstractCustomInventory<T extends CustomInventoryDefinition<? extends AbstractCustomInventory<T>>> extends AbstractCustomFeature<T> implements CustomInventory<T> {
    private final DataHolder dataHolder;
    // Initialized after construction
    private Inventory inventory;

    public AbstractCustomInventory(T definition, @NonNull DataHolder dataHolder) {
        super(definition);

        this.dataHolder = dataHolder;
    }

    public final void setInventory(@NonNull Inventory inventory) {
        Preconditions.checkState(this.inventory == null, "The inventory has already been set.");

        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public DataHolder getDataHolder() {
        return dataHolder;
    }
}
