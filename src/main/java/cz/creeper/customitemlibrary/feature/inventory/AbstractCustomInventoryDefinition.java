package cz.creeper.customitemlibrary.feature.inventory;

import cz.creeper.customitemlibrary.feature.AbstractCustomFeatureDefinition;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.spongepowered.api.plugin.PluginContainer;

@EqualsAndHashCode(callSuper = true)
@Getter
public abstract class AbstractCustomInventoryDefinition<T extends CustomInventory<? extends AbstractCustomInventoryDefinition<T>>> extends AbstractCustomFeatureDefinition<T> implements CustomInventoryDefinition<T> {
    public AbstractCustomInventoryDefinition(@NonNull PluginContainer pluginContainer, @NonNull String typeId) {
        super(pluginContainer, typeId);
    }
}
