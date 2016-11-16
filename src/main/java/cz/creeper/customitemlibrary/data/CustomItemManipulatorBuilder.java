package cz.creeper.customitemlibrary.data;

import lombok.ToString;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;

@ToString
public class CustomItemManipulatorBuilder implements DataManipulatorBuilder<CustomItemData, ImmutableCustomItemData> {
    @Override
    public CustomItemData create() {
        return new CustomItemData(null);
    }

    @Override
    public Optional<CustomItemData> createFrom(DataHolder dataHolder) {
        return dataHolder.get(CustomItemLibraryKeys.CUSTOM_ITEM_ID).map(CustomItemData::new);
    }

    @Override
    public Optional<CustomItemData> build(DataView container) throws InvalidDataException {
        return container.getString(CustomItemLibraryKeys.CUSTOM_ITEM_ID.getQuery()).map(CustomItemData::new);
    }
}
