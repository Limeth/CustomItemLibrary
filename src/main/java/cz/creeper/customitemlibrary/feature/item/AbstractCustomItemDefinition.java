package cz.creeper.customitemlibrary.feature.item;

import cz.creeper.customitemlibrary.feature.AbstractCustomFeatureDefinition;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.spongepowered.api.plugin.PluginContainer;

@EqualsAndHashCode(callSuper = true)
@Getter
public abstract class AbstractCustomItemDefinition<T extends CustomItem<? extends AbstractCustomItemDefinition<T>>> extends AbstractCustomFeatureDefinition<T> implements CustomItemDefinition<T> {
    public AbstractCustomItemDefinition(PluginContainer pluginContainer, String typeId, String defaultModel, Iterable<String> models) {
        super(pluginContainer, typeId, defaultModel, models);
    }
}
