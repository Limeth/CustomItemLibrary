package cz.creeper.customitemlibrary.data;

import lombok.ToString;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;

@ToString
public class CustomItemManipulatorBuilder extends AbstractDataBuilder<CustomItemData>
        implements DataManipulatorBuilder<CustomItemData, ImmutableCustomItemData> {
    public CustomItemManipulatorBuilder() {
        super(CustomItemData.class, 1);
    }

    @Override
    public CustomItemData create() {
        return new CustomItemData();
    }

    @Override
    public Optional<CustomItemData> createFrom(DataHolder dataHolder) {
        Optional<String> pluginId = dataHolder.get(CustomItemLibraryKeys.CUSTOM_ITEM_PLUGIN_ID);
        Optional<String> typeId = dataHolder.get(CustomItemLibraryKeys.CUSTOM_ITEM_TYPE_ID);

        if(pluginId.isPresent() && typeId.isPresent()) {
            return Optional.of(new CustomItemData(pluginId.get(), typeId.get()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    protected Optional<CustomItemData> buildContent(DataView container) throws InvalidDataException {
        Optional<String> pluginId = container.getString(CustomItemLibraryKeys.CUSTOM_ITEM_PLUGIN_ID.getQuery());
        Optional<String> typeId = container.getString(CustomItemLibraryKeys.CUSTOM_ITEM_TYPE_ID.getQuery());

        if(pluginId.isPresent() && typeId.isPresent()) {
            return Optional.of(new CustomItemData(pluginId.get(), typeId.get()));
        } else {
            return Optional.empty();
        }
    }
}
