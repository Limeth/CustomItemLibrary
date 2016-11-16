package cz.creeper.customitemlibrary.data;

import lombok.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

@AllArgsConstructor
@ToString
public class ImmutableCustomItemData extends AbstractImmutableData<ImmutableCustomItemData, CustomItemData> {
    @NonNull
    @Getter(AccessLevel.PRIVATE)
    private String customItemId;

    @Override
    protected void registerGetters() {
        registerFieldGetter(CustomItemLibraryKeys.CUSTOM_ITEM_ID, this::getCustomItemId);
        registerKeyValue(CustomItemLibraryKeys.CUSTOM_ITEM_ID, this::customItemId);
    }

    public ImmutableValue<String> customItemId() {
        return Sponge.getRegistry().getValueFactory().createValue(CustomItemLibraryKeys.CUSTOM_ITEM_ID, this.customItemId, null).asImmutable();
    }

    @Override
    public CustomItemData asMutable() {
        return new CustomItemData(customItemId);
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
