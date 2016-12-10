package cz.creeper.customitemlibrary.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
public abstract class AbstractCustomItem<I extends AbstractCustomItem<I, T>, T extends CustomItemDefinition<I>> implements CustomItem {
    @Getter
    private ItemStack itemStack;

    @Getter
    private T definition;

    protected abstract Optional<String> resolveCurrentModel();

    protected abstract void applyModel(String model);

    @Override
    public final String getModel() {
        Optional<String> model = resolveCurrentModel();

        if (!model.isPresent() || !getDefinition().getModels().contains(model.get())) {
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
    }
}
