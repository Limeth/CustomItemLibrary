package cz.creeper.customitemlibrary.data.mutable;

import com.google.common.collect.Maps;
import cz.creeper.customitemlibrary.data.CustomItemLibraryKeys;
import cz.creeper.customitemlibrary.data.immutable.ImmutableCustomInventoriesData;
import lombok.ToString;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractMappedData;
import org.spongepowered.api.data.merge.MergeFunction;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ToString
public class CustomInventoriesData extends AbstractMappedData<String, CustomInventoryData, CustomInventoriesData, ImmutableCustomInventoriesData> {
    public CustomInventoriesData(Map<String, CustomInventoryData> value) {
        super(value, CustomItemLibraryKeys.CUSTOM_INVENTORIES);
    }

    public CustomInventoriesData() {
        this(Maps.newHashMap());
    }

    @Override
    public Optional<CustomInventoryData> get(String key) {
        return Optional.ofNullable(getValue().get(key));
    }

    @Override
    public Set<String> getMapKeys() {
        return getValue().keySet();
    }

    @Override
    public CustomInventoriesData put(String key, CustomInventoryData value) {
        getValue().put(key, value);
        return this;
    }

    @Override
    public CustomInventoriesData putAll(Map<? extends String, ? extends CustomInventoryData> map) {
        getValue().putAll(map);
        return this;
    }

    @Override
    public CustomInventoriesData remove(String key) {
        getValue().remove(key);
        return this;
    }

    @Override
    public Optional<CustomInventoriesData> fill(DataHolder dataHolder, MergeFunction overlap) {
        // TODO
        return null;
    }

    @Override
    public Optional<CustomInventoriesData> from(DataContainer container) {
        // TODO
        return null;
    }

    @Override
    public CustomInventoriesData copy() {
        return new CustomInventoriesData(getValue());
    }

    @Override
    public ImmutableCustomInventoriesData asImmutable() {
        return new ImmutableCustomInventoriesData(getValue());
    }

    @Override
    public int getContentVersion() {
        return 0;
    }
}
