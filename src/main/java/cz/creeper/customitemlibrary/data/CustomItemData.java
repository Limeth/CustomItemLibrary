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

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    @NonNull
    private String customItemModel;

    public CustomItemData(String customItemPluginId, String customItemTypeId, String customItemModel) {
        this.customItemPluginId = customItemPluginId;
        this.customItemTypeId = customItemTypeId;
        this.customItemModel = customItemModel;
        registerGettersAndSetters();
    }

    public CustomItemData() {
        this(ID_UNINITIALIZED, ID_UNINITIALIZED, ID_UNINITIALIZED);
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(CustomItemLibraryKeys.CUSTOM_ITEM_PLUGIN_ID, this::getCustomItemPluginId);
        registerFieldSetter(CustomItemLibraryKeys.CUSTOM_ITEM_PLUGIN_ID, this::setCustomItemPluginId);
        registerKeyValue(CustomItemLibraryKeys.CUSTOM_ITEM_PLUGIN_ID, this::customItemPluginId);

        registerFieldGetter(CustomItemLibraryKeys.CUSTOM_ITEM_TYPE_ID, this::getCustomItemTypeId);
        registerFieldSetter(CustomItemLibraryKeys.CUSTOM_ITEM_TYPE_ID, this::setCustomItemTypeId);
        registerKeyValue(CustomItemLibraryKeys.CUSTOM_ITEM_TYPE_ID, this::customItemTypeId);

        registerFieldGetter(CustomItemLibraryKeys.CUSTOM_ITEM_MODEL, this::getCustomItemModel);
        registerFieldSetter(CustomItemLibraryKeys.CUSTOM_ITEM_MODEL, this::setCustomItemModel);
        registerKeyValue(CustomItemLibraryKeys.CUSTOM_ITEM_MODEL, this::customItemModel);
    }

    public Value<String> customItemPluginId() {
        return customItemPluginId(customItemPluginId);
    }

    public static Value<String> customItemPluginId(String customItemPluginId) {
        return Sponge.getRegistry().getValueFactory().createValue(CustomItemLibraryKeys.CUSTOM_ITEM_PLUGIN_ID, customItemPluginId, CustomItemData.ID_UNINITIALIZED);
    }

    public Value<String> customItemTypeId() {
        return customItemTypeId(customItemTypeId);
    }

    public static Value<String> customItemTypeId(String customItemTypeId) {
        return Sponge.getRegistry().getValueFactory().createValue(CustomItemLibraryKeys.CUSTOM_ITEM_TYPE_ID, customItemTypeId, CustomItemData.ID_UNINITIALIZED);
    }

    public Value<String> customItemModel() {
        return customItemModel(customItemModel);
    }

    public static Value<String> customItemModel(String customItemModel) {
        return Sponge.getRegistry().getValueFactory().createValue(CustomItemLibraryKeys.CUSTOM_ITEM_MODEL, customItemModel, CustomItemData.ID_UNINITIALIZED);
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
        Optional<String> model = dataContainer.getString(CustomItemLibraryKeys.CUSTOM_ITEM_MODEL.getQuery());

        if(pluginId.isPresent() && typeId.isPresent() && model.isPresent()) {
            return Optional.of(new CustomItemData(pluginId.get(), typeId.get(), model.get()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    @Nonnull
    public CustomItemData copy() {
        return new CustomItemData(customItemPluginId, customItemTypeId, customItemModel);
    }

    @Override
    @Nonnull
    public ImmutableCustomItemData asImmutable() {
        return new ImmutableCustomItemData(customItemPluginId, customItemTypeId, customItemModel);
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
                .set(CustomItemLibraryKeys.CUSTOM_ITEM_TYPE_ID.getQuery(), customItemTypeId)
                .set(CustomItemLibraryKeys.CUSTOM_ITEM_MODEL.getQuery(), customItemModel);
    }
}
