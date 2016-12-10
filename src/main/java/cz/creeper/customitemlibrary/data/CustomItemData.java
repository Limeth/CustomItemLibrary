package cz.creeper.customitemlibrary.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;

import javax.annotation.Nonnull;
import java.util.Optional;

@ToString
public class CustomItemData extends AbstractData<CustomItemData, ImmutableCustomItemData> {
    public static final String ID_UNINITIALIZED = "__UNINITIALIZED__";

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    @NonNull
    private String customItemPluginId;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    @NonNull
    private String customItemTypeId;

    public CustomItemData(String customItemPluginId, String customItemTypeId) {
        this.customItemPluginId = customItemPluginId;
        this.customItemTypeId = customItemTypeId;
        registerGettersAndSetters();
    }

    public CustomItemData() {
        this(ID_UNINITIALIZED, ID_UNINITIALIZED);
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(CustomItemLibraryKeys.CUSTOM_ITEM_PLUGIN_ID, this::getCustomItemPluginId);
        registerFieldSetter(CustomItemLibraryKeys.CUSTOM_ITEM_PLUGIN_ID, this::setCustomItemPluginId);
        registerKeyValue(CustomItemLibraryKeys.CUSTOM_ITEM_PLUGIN_ID, this::customItemPluginId);

        registerFieldGetter(CustomItemLibraryKeys.CUSTOM_ITEM_TYPE_ID, this::getCustomItemTypeId);
        registerFieldSetter(CustomItemLibraryKeys.CUSTOM_ITEM_TYPE_ID, this::setCustomItemTypeId);
        registerKeyValue(CustomItemLibraryKeys.CUSTOM_ITEM_TYPE_ID, this::customItemTypeId);
    }

    public Value<String> customItemPluginId() {
        return Sponge.getRegistry().getValueFactory().createValue(CustomItemLibraryKeys.CUSTOM_ITEM_PLUGIN_ID, this.customItemPluginId, ID_UNINITIALIZED);
    }

    public Value<String> customItemTypeId() {
        return Sponge.getRegistry().getValueFactory().createValue(CustomItemLibraryKeys.CUSTOM_ITEM_TYPE_ID, this.customItemTypeId, ID_UNINITIALIZED);
    }

    @Override
    @Nonnull
    public Optional<CustomItemData> fill(@Nonnull DataHolder dataHolder, @Nonnull MergeFunction mergeFunction) {
        throw new NotImplementedException("NYI");  // TODO
    }

    @Override
    @Nonnull
    public Optional<CustomItemData> from(@Nonnull DataContainer dataContainer) {
        Optional<String> pluginId = dataContainer.getString(CustomItemLibraryKeys.CUSTOM_ITEM_PLUGIN_ID.getQuery());
        Optional<String> typeId = dataContainer.getString(CustomItemLibraryKeys.CUSTOM_ITEM_TYPE_ID.getQuery());

        if(pluginId.isPresent() && typeId.isPresent()) {
            return Optional.of(new CustomItemData(pluginId.get(), typeId.get()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    @Nonnull
    public CustomItemData copy() {
        return new CustomItemData(customItemPluginId, customItemTypeId);
    }

    @Override
    @Nonnull
    public ImmutableCustomItemData asImmutable() {
        return new ImmutableCustomItemData(customItemPluginId, customItemTypeId);
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
