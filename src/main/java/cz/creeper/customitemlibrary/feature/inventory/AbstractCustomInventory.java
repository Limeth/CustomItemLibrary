package cz.creeper.customitemlibrary.feature.inventory;

import cz.creeper.customitemlibrary.feature.AbstractCustomFeature;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
@Getter
public abstract class AbstractCustomInventory<T extends CustomInventoryDefinition<? extends AbstractCustomInventory<T>>> extends AbstractCustomFeature<T> implements CustomInventory<T> {
    public AbstractCustomInventory(T definition) {
        super(definition);
    }
}
