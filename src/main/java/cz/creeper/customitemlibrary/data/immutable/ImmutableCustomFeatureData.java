package cz.creeper.customitemlibrary.data.immutable;

import cz.creeper.customitemlibrary.data.mutable.CustomFeatureData;
import cz.creeper.customitemlibrary.data.CustomItemLibraryKeys;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

import javax.annotation.Nonnull;

@ToString
public class ImmutableCustomFeatureData extends AbstractImmutableData<ImmutableCustomFeatureData, CustomFeatureData> {
    @NonNull
    @Getter(AccessLevel.PRIVATE)
    private String customFeaturePluginId;

    @NonNull
    @Getter(AccessLevel.PRIVATE)
    private String customFeatureTypeId;

    @NonNull
    @Getter(AccessLevel.PRIVATE)
    private String customFeatureModel;

    public ImmutableCustomFeatureData(String customFeaturePluginId, String customFeatureTypeId, String customFeatureModel) {
        this.customFeaturePluginId = customFeaturePluginId;
        this.customFeatureTypeId = customFeatureTypeId;
        this.customFeatureModel = customFeatureModel;
        registerGetters();
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(CustomItemLibraryKeys.CUSTOM_FEATURE_PLUGIN_ID, this::getCustomFeaturePluginId);
        registerKeyValue(CustomItemLibraryKeys.CUSTOM_FEATURE_PLUGIN_ID, this::customFeaturePluginId);

        registerFieldGetter(CustomItemLibraryKeys.CUSTOM_FEATURE_TYPE_ID, this::getCustomFeatureTypeId);
        registerKeyValue(CustomItemLibraryKeys.CUSTOM_FEATURE_TYPE_ID, this::customFeatureTypeId);

        registerFieldGetter(CustomItemLibraryKeys.CUSTOM_FEATURE_MODEL, this::getCustomFeatureModel);
        registerKeyValue(CustomItemLibraryKeys.CUSTOM_FEATURE_MODEL, this::customFeatureModel);
    }

    public ImmutableValue<String> customFeaturePluginId() {
        return CustomFeatureData.customFeaturePluginId(customFeaturePluginId)
                .asImmutable();
    }

    public ImmutableValue<String> customFeatureTypeId() {
        return CustomFeatureData.customFeatureTypeId(customFeatureTypeId)
                .asImmutable();
    }

    public ImmutableValue<String> customFeatureModel() {
        return CustomFeatureData.customFeatureModel(customFeatureModel)
                .asImmutable();
    }

    @Override
    @Nonnull
    public CustomFeatureData asMutable() {
        return new CustomFeatureData(customFeaturePluginId, customFeatureTypeId, customFeatureModel);
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
