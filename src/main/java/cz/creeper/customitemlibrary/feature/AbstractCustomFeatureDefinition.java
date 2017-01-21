package cz.creeper.customitemlibrary.feature;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.spongepowered.api.plugin.PluginContainer;

@EqualsAndHashCode
@Getter
public abstract class AbstractCustomFeatureDefinition<T extends CustomFeature<? extends AbstractCustomFeatureDefinition<T>>> implements CustomFeatureDefinition<T> {
    @NonNull
    private final PluginContainer pluginContainer;

    @NonNull
    private final String typeId;

    public AbstractCustomFeatureDefinition(@NonNull PluginContainer pluginContainer, @NonNull String typeId) {
        this.pluginContainer = pluginContainer;
        this.typeId = typeId;
    }
}
