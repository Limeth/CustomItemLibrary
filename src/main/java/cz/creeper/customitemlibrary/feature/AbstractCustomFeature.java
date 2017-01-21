package cz.creeper.customitemlibrary.feature;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.Optional;

@EqualsAndHashCode
@AllArgsConstructor
@Getter
public abstract class AbstractCustomFeature<T extends CustomFeatureDefinition<? extends AbstractCustomFeature<T>>> implements CustomFeature<T> {
    @NonNull
    private T definition;

    protected abstract Optional<String> resolveCurrentModel();

    protected abstract void applyModel(String model);
}
