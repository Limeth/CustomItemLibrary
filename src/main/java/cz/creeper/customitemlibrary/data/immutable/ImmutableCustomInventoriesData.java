package cz.creeper.customitemlibrary.data.immutable;

import cz.creeper.customitemlibrary.data.mutable.CustomInventoriesData;
import cz.creeper.customitemlibrary.data.mutable.CustomInventoryData;
import cz.creeper.customitemlibrary.data.CustomItemLibraryKeys;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableMappedData;

import java.util.Map;

public class ImmutableCustomInventoriesData extends AbstractImmutableMappedData<String, CustomInventoryData, ImmutableCustomInventoriesData, CustomInventoriesData> {
    protected ImmutableCustomInventoriesData(Map<String, CustomInventoryData> value) {
        super(value, CustomItemLibraryKeys.CUSTOM_INVENTORIES);
    }

    @Override
    public CustomInventoriesData asMutable() {
        return new CustomInventoriesData(getValue());
    }

    @Override
    public int getContentVersion() {
        return 0;
    }
}
