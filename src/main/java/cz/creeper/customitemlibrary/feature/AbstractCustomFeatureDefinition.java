package cz.creeper.customitemlibrary.feature;

import com.google.common.collect.ImmutableSet;
import cz.creeper.customitemlibrary.util.Util;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.stream.Collectors;

@EqualsAndHashCode
@Getter
public abstract class AbstractCustomFeatureDefinition<T extends CustomFeature<? extends AbstractCustomFeatureDefinition<T>>> implements CustomFeatureDefinition<T> {
    @NonNull
    private final PluginContainer pluginContainer;

    @NonNull
    private final String typeId;

    @NonNull
    private final String defaultModel;

    @NonNull
    private final ImmutableSet<String> models;

    public AbstractCustomFeatureDefinition(@NonNull PluginContainer pluginContainer, @NonNull String typeId,
                                           @NonNull String defaultModel, Iterable<String> models) {
        this.pluginContainer = pluginContainer;
        this.typeId = typeId;
        this.defaultModel = defaultModel;
        this.models = ImmutableSet.<String>builder()
                .add(defaultModel)
                .addAll(Util.removeNull(models).collect(Collectors.toSet()))
                .build();
    }
}
