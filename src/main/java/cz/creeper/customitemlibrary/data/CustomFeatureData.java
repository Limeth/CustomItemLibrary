package cz.creeper.customitemlibrary.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;

import javax.annotation.Nonnull;
import java.util.Optional;

@ToString
public class CustomFeatureData extends AbstractData<CustomFeatureData, ImmutableCustomFeatureData> {
    public static final String ID_UNINITIALIZED = "__UNINITIALIZED__";

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    @NonNull
    private String customFeaturePluginId;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    @NonNull
    private String customFeatureTypeId;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    @NonNull
    private String customFeatureModel;

    public CustomFeatureData(String customFeaturePluginId, String customFeatureTypeId, String customFeatureModel) {
        this.customFeaturePluginId = customFeaturePluginId;
        this.customFeatureTypeId = customFeatureTypeId;
        this.customFeatureModel = customFeatureModel;
        registerGettersAndSetters();
    }

    public CustomFeatureData() {
        this(ID_UNINITIALIZED, ID_UNINITIALIZED, ID_UNINITIALIZED);
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(CustomItemLibraryKeys.CUSTOM_FEATURE_PLUGIN_ID, this::getCustomFeaturePluginId);
        registerFieldSetter(CustomItemLibraryKeys.CUSTOM_FEATURE_PLUGIN_ID, this::setCustomFeaturePluginId);
        registerKeyValue(CustomItemLibraryKeys.CUSTOM_FEATURE_PLUGIN_ID, this::customFeaturePluginId);

        registerFieldGetter(CustomItemLibraryKeys.CUSTOM_FEATURE_TYPE_ID, this::getCustomFeatureTypeId);
        registerFieldSetter(CustomItemLibraryKeys.CUSTOM_FEATURE_TYPE_ID, this::setCustomFeatureTypeId);
        registerKeyValue(CustomItemLibraryKeys.CUSTOM_FEATURE_TYPE_ID, this::customFeatureTypeId);

        registerFieldGetter(CustomItemLibraryKeys.CUSTOM_FEATURE_MODEL, this::getCustomFeatureModel);
        registerFieldSetter(CustomItemLibraryKeys.CUSTOM_FEATURE_MODEL, this::setCustomFeatureModel);
        registerKeyValue(CustomItemLibraryKeys.CUSTOM_FEATURE_MODEL, this::customFeatureModel);
    }

    public Value<String> customFeaturePluginId() {
        return customFeaturePluginId(customFeaturePluginId);
    }

    public static Value<String> customFeaturePluginId(String customFeaturePluginId) {
        return Sponge.getRegistry().getValueFactory().createValue(CustomItemLibraryKeys.CUSTOM_FEATURE_PLUGIN_ID, customFeaturePluginId, CustomFeatureData.ID_UNINITIALIZED);
    }

    public Value<String> customFeatureTypeId() {
        return customFeatureTypeId(customFeatureTypeId);
    }

    public static Value<String> customFeatureTypeId(String customFeatureTypeId) {
        return Sponge.getRegistry().getValueFactory().createValue(CustomItemLibraryKeys.CUSTOM_FEATURE_TYPE_ID, customFeatureTypeId, CustomFeatureData.ID_UNINITIALIZED);
    }

    public Value<String> customFeatureModel() {
        return customFeatureModel(customFeatureModel);
    }

    public static Value<String> customFeatureModel(String customFeatureModel) {
        return Sponge.getRegistry().getValueFactory().createValue(CustomItemLibraryKeys.CUSTOM_FEATURE_MODEL, customFeatureModel, CustomFeatureData.ID_UNINITIALIZED);
    }

    @Override
    @Nonnull
    public Optional<CustomFeatureData> fill(@Nonnull DataHolder dataHolder, @Nonnull MergeFunction mergeFunction) {
        CustomFeatureData data = new CustomFeatureData();

        dataHolder.get(CustomItemLibraryKeys.CUSTOM_FEATURE_PLUGIN_ID)
                .ifPresent(pluginId -> data.customFeaturePluginId = pluginId);
        dataHolder.get(CustomItemLibraryKeys.CUSTOM_FEATURE_TYPE_ID)
                .ifPresent(typeId -> data.customFeatureTypeId = typeId);
        dataHolder.get(CustomItemLibraryKeys.CUSTOM_FEATURE_MODEL)
                .ifPresent(model -> data.customFeatureModel = model);

        return Optional.of(data);
    }

    @Override
    @Nonnull
    public Optional<CustomFeatureData> from(@Nonnull DataContainer dataContainer) {
        Optional<String> pluginId = dataContainer.getString(CustomItemLibraryKeys.CUSTOM_FEATURE_PLUGIN_ID.getQuery());
        Optional<String> typeId = dataContainer.getString(CustomItemLibraryKeys.CUSTOM_FEATURE_TYPE_ID.getQuery());
        Optional<String> model = dataContainer.getString(CustomItemLibraryKeys.CUSTOM_FEATURE_MODEL.getQuery());

        if(pluginId.isPresent() && typeId.isPresent() && model.isPresent()) {
            return Optional.of(new CustomFeatureData(pluginId.get(), typeId.get(), model.get()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    @Nonnull
    public CustomFeatureData copy() {
        return new CustomFeatureData(customFeaturePluginId, customFeatureTypeId, customFeatureModel);
    }

    @Override
    @Nonnull
    public ImmutableCustomFeatureData asImmutable() {
        return new ImmutableCustomFeatureData(customFeaturePluginId, customFeatureTypeId, customFeatureModel);
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    @Nonnull
    public DataContainer toContainer() {
        return super.toContainer()
                .set(CustomItemLibraryKeys.CUSTOM_FEATURE_PLUGIN_ID.getQuery(), customFeaturePluginId)
                .set(CustomItemLibraryKeys.CUSTOM_FEATURE_TYPE_ID.getQuery(), customFeatureTypeId)
                .set(CustomItemLibraryKeys.CUSTOM_FEATURE_MODEL.getQuery(), customFeatureModel);
    }
}
