package cz.creeper.customitemlibrary.feature.inventory.simple;

import cz.creeper.customitemlibrary.feature.inventory.AbstractCustomInventory;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;

import java.util.Optional;
import java.util.function.Consumer;

@EqualsAndHashCode(callSuper = true)
@Getter
public class SimpleCustomInventory extends AbstractCustomInventory<SimpleCustomInventoryDefinition> implements Consumer<InteractInventoryEvent> {
    // Initialized after construction
    private Inventory inventory;

    public SimpleCustomInventory(SimpleCustomInventoryDefinition definition) {
        super(definition);
    }

    void setInventory(@NonNull Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    protected Optional<String> resolveCurrentModel() {
        return null;
    }

    @Override
    protected void applyModel(String model) {

    }

    @Override
    public DataHolder getDataHolder() {
        return null;
    }

    @Override
    public void accept(InteractInventoryEvent interactInventoryEvent) {

    }
}
