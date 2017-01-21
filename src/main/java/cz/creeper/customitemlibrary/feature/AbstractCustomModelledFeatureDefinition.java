package cz.creeper.customitemlibrary.feature;

import com.google.common.collect.ImmutableSet;
import cz.creeper.customitemlibrary.util.Util;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Getter
public abstract class AbstractCustomModelledFeatureDefinition<T extends CustomModelledFeature<? extends AbstractCustomModelledFeatureDefinition<T>>> extends AbstractCustomFeatureDefinition<T> implements CustomModelledFeatureDefinition<T> {
    @NonNull
    private final String defaultModel;

    @NonNull
    private final ImmutableSet<String> models;

    public AbstractCustomModelledFeatureDefinition(@NonNull PluginContainer pluginContainer, @NonNull String typeId, String defaultModel, Iterable<String> additionalModels) {
        super(pluginContainer, typeId);

        this.defaultModel = defaultModel;
        this.models = ImmutableSet.<String>builder()
                .add(defaultModel)
                .addAll(Util.removeNull(additionalModels).collect(Collectors.toSet()))
                .build();
    }
}
