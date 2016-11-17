package cz.creeper.customitemlibrary.data;

import lombok.*;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;

@ToString
public class CustomItemData extends AbstractData<CustomItemData, ImmutableCustomItemData> {
    public static final String ID_UNINITIALIZED = "__UNINITIALIZED__";
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    @NonNull
    private String customItemId;

    public CustomItemData(String customItemId) {
        this.customItemId = customItemId;
        registerGettersAndSetters();
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(CustomItemLibraryKeys.CUSTOM_ITEM_ID, this::getCustomItemId);
        registerFieldSetter(CustomItemLibraryKeys.CUSTOM_ITEM_ID, this::setCustomItemId);
        registerKeyValue(CustomItemLibraryKeys.CUSTOM_ITEM_ID, this::customItemId);
    }

    public Value<String> customItemId() {
        return Sponge.getRegistry().getValueFactory().createValue(CustomItemLibraryKeys.CUSTOM_ITEM_ID, this.customItemId, ID_UNINITIALIZED);
    }

    @Override
    public Optional<CustomItemData> fill(DataHolder dataHolder, MergeFunction mergeFunction) {
        throw new NotImplementedException("NYI");  // TODO
    }

    @Override
    public Optional<CustomItemData> from(DataContainer dataContainer) {
        return dataContainer.getString(CustomItemLibraryKeys.CUSTOM_ITEM_ID.getQuery())
                .map(CustomItemData::new);
    }

    @Override
    public CustomItemData copy() {
        return new CustomItemData(customItemId);
    }

    @Override
    public ImmutableCustomItemData asImmutable() {
        return new ImmutableCustomItemData(customItemId);
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(CustomItemLibraryKeys.CUSTOM_ITEM_ID.getQuery(), customItemId);
    }
}
