package cz.creeper.customitemlibrary.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

import javax.annotation.Nonnull;

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
        return CustomItemData.customItemPluginId(customItemPluginId)
                .asImmutable();
    }

    public ImmutableValue<String> customItemTypeId() {
        return CustomItemData.customItemTypeId(customItemTypeId)
                .asImmutable();
    }

    @Override
    @Nonnull
    public CustomItemData asMutable() {
        return new CustomItemData(customItemPluginId, customItemTypeId);
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    @Nonnull
    public DataContainer toContainer() {
        return super.toContainer()
                .set(CustomItemLibraryKeys.CUSTOM_ITEM_PLUGIN_ID.getQuery(), customItemPluginId)
                .set(CustomItemLibraryKeys.CUSTOM_ITEM_TYPE_ID.getQuery(), customItemTypeId);
    }
}
