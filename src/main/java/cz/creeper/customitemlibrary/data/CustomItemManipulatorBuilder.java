package cz.creeper.customitemlibrary.data;

import lombok.ToString;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import javax.annotation.Nonnull;
import java.util.Optional;

@ToString
public class CustomItemManipulatorBuilder extends AbstractDataBuilder<CustomFeatureData>
        implements DataManipulatorBuilder<CustomFeatureData, ImmutableCustomFeatureData> {
    public CustomItemManipulatorBuilder() {
        super(CustomFeatureData.class, 1);
    }

    @Override
    @Nonnull
    public CustomFeatureData create() {
        return new CustomFeatureData();
    }

    @Override
    @Nonnull
    public Optional<CustomFeatureData> createFrom(@Nonnull DataHolder dataHolder) {
        Optional<String> pluginId = dataHolder.get(CustomItemLibraryKeys.CUSTOM_FEATURE_PLUGIN_ID);
        Optional<String> typeId = dataHolder.get(CustomItemLibraryKeys.CUSTOM_ITEM_TYPE_ID);
        Optional<String> model = dataHolder.get(CustomItemLibraryKeys.CUSTOM_ITEM_MODEL);

        if(pluginId.isPresent() && typeId.isPresent() && model.isPresent()) {
            return Optional.of(new CustomFeatureData(pluginId.get(), typeId.get(), model.get()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    @Nonnull
    protected Optional<CustomFeatureData> buildContent(@Nonnull DataView container) throws InvalidDataException {
        Optional<String> pluginId = container.getString(CustomItemLibraryKeys.CUSTOM_FEATURE_PLUGIN_ID.getQuery());
        Optional<String> typeId = container.getString(CustomItemLibraryKeys.CUSTOM_ITEM_TYPE_ID.getQuery());
        Optional<String> model = container.getString(CustomItemLibraryKeys.CUSTOM_ITEM_MODEL.getQuery());

        if(pluginId.isPresent() && typeId.isPresent() && model.isPresent()) {
            return Optional.of(new CustomFeatureData(pluginId.get(), typeId.get(), model.get()));
        } else {
            return Optional.empty();
        }
    }
}
