package cz.creeper.customitemlibrary.feature;

import cz.creeper.customitemlibrary.data.CustomItemLibraryKeys;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.spongepowered.api.data.DataTransactionResult;

import java.util.Optional;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Getter
public abstract class AbstractCustomModelledFeature<T extends CustomModelledFeatureDefinition<? extends AbstractCustomModelledFeature<T>>> extends AbstractCustomFeature<T> implements CustomModelledFeature<T> {
    public AbstractCustomModelledFeature(T definition) {
        super(definition);
    }

    @Override
    public final String getModel() {
        Optional<String> model = resolveCurrentModel();

        if (!model.isPresent()
                || !getDataHolder().get(CustomItemLibraryKeys.CUSTOM_FEATURE_MODEL)
                .map(savedModel -> model.get().equals(savedModel)).orElse(false)) {
            // If the texture is invalid, change the texture to the default one.
            String defaultModel = getDefinition().getDefaultModel();

            applyModel(defaultModel);

            return defaultModel;
        }

        return model.get();
    }

    @Override
    public final void setModel(String model) {
        if(!getDefinition().getModels().contains(model))
            throw new IllegalArgumentException("This custom tool has no model called '" + model
                    + "'. Available, defined models: "
                    + getDefinition().getModels().stream().collect(Collectors.joining(", ")));

        applyModel(model);

        DataTransactionResult result = getDataHolder().offer(CustomItemLibraryKeys.CUSTOM_FEATURE_MODEL, model);

        if(!result.isSuccessful())
            throw new IllegalStateException("Could not update the item model; rejected: " + result.getRejectedData()
                    + "; replaced: " + result.getReplacedData());
    }
}
