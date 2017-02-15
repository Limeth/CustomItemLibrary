package cz.creeper.customitemlibrary.feature.inventory;

import com.google.common.base.Preconditions;
import cz.creeper.customitemlibrary.feature.AbstractCustomFeature;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;

import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Getter
public abstract class AbstractCustomInventory<T extends CustomInventoryDefinition<? extends AbstractCustomInventory<T>>> extends AbstractCustomFeature<T> implements CustomInventory<T> {
    private final DataHolder dataHolder;
    // Initialized after construction
    private Inventory inventory;
    private Container container;

    public AbstractCustomInventory(T definition, @NonNull DataHolder dataHolder) {
        super(definition);

        this.dataHolder = dataHolder;
    }

    public final void setInventory(@NonNull Inventory inventory) {
        Preconditions.checkState(this.inventory == null, "The inventory has already been set.");

        this.inventory = inventory;
    }

    /**
     * @deprecated Do not call {@link Player#openInventory(Inventory, Cause)} with this
     */
    @Deprecated
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public Optional<Container> getContainer() {
        return Optional.ofNullable(container);
    }

    @Override
    public Container open(Player player, Cause cause) {
        if(container != null) {
            container.open(player, cause);
        } else {
            container = player.openInventory(inventory, cause)
                .orElseThrow(() -> new IllegalStateException("Could not open the inventory."));
        }

        return container;
    }

    @Override
    public DataHolder getDataHolder() {
        return dataHolder;
    }
}
