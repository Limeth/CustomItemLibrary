package cz.creeper.customitemlibrary.data;

import lombok.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

@ToString
public class ImmutableCustomItemData extends AbstractImmutableData<ImmutableCustomItemData, CustomItemData> {
    @NonNull
    @Getter(AccessLevel.PRIVATE)
    private String customItemPluginId;

    @NonNull
    @Getter(AccessLevel.PRIVATE)
    private String customItemTypeId;

    public ImmutableCustomItemData(String customItemPluginId, String customItemTypeId) {
        this.customItemPluginId = customItemPluginId;
        this.customItemTypeId = customItemTypeId;
        registerGetters();
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(CustomItemLibraryKeys.CUSTOM_ITEM_PLUGIN_ID, this::getCustomItemPluginId);
        registerKeyValue(CustomItemLibraryKeys.CUSTOM_ITEM_PLUGIN_ID, this::customItemPluginId);

        registerFieldGetter(CustomItemLibraryKeys.CUSTOM_ITEM_TYPE_ID, this::getCustomItemTypeId);
        registerKeyValue(CustomItemLibraryKeys.CUSTOM_ITEM_TYPE_ID, this::customItemTypeId);
    }

    public ImmutableValue<String> customItemPluginId() {
        return Sponge.getRegistry().getValueFactory().createValue(CustomItemLibraryKeys.CUSTOM_ITEM_PLUGIN_ID, this.customItemPluginId, CustomItemData.ID_UNINITIALIZED).asImmutable();
    }

    public ImmutableValue<String> customItemTypeId() {
        return Sponge.getRegistry().getValueFactory().createValue(CustomItemLibraryKeys.CUSTOM_ITEM_TYPE_ID, this.customItemTypeId, CustomItemData.ID_UNINITIALIZED).asImmutable();
    }

    @Override
    public CustomItemData asMutable() {
        return new CustomItemData(customItemPluginId, customItemTypeId);
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(CustomItemLibraryKeys.CUSTOM_ITEM_PLUGIN_ID.getQuery(), customItemPluginId)
                .set(CustomItemLibraryKeys.CUSTOM_ITEM_TYPE_ID.getQuery(), customItemTypeId);
    }
}
