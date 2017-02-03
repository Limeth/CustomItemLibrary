package cz.creeper.customitemlibrary.data.builder;

import cz.creeper.customitemlibrary.data.CustomItemLibraryKeys;
import cz.creeper.customitemlibrary.data.immutable.ImmutableCustomInventoriesData;
import cz.creeper.customitemlibrary.data.mutable.CustomInventoriesData;
import cz.creeper.customitemlibrary.data.mutable.CustomInventoryData;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Map;
import java.util.Optional;

public class CustomInventoriesManipulatorBuilder extends AbstractDataBuilder<CustomInventoriesData>
        implements DataManipulatorBuilder<CustomInventoriesData, ImmutableCustomInventoriesData> {
    public CustomInventoriesManipulatorBuilder() {
        super(CustomInventoriesData.class, 0);
    }

    @Override
    public CustomInventoriesData create() {
        return new CustomInventoriesData();
    }

    @Override
    public Optional<CustomInventoriesData> createFrom(DataHolder dataHolder) {
        return dataHolder.get(CustomItemLibraryKeys.CUSTOM_INVENTORIES)
                .map(CustomInventoriesData::new);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Optional<CustomInventoriesData> buildContent(DataView container) throws InvalidDataException {
        return container.getMap(CustomItemLibraryKeys.CUSTOM_INVENTORIES.getQuery())
                .map(map -> (Map<String, CustomInventoryData>) map)
                .map(CustomInventoriesData::new);
    }
}
