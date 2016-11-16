package cz.creeper.customitemlibrary;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.api.data.merge.MergeFunction;

import java.util.Optional;

public class CustomItemData extends AbstractData<CustomItemData, ImmutableCustomItemData> {
    @NonNull
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private String customItemId;

    @Override
    protected void registerGettersAndSetters() {

    }

    @Override
    public Optional<CustomItemData> fill(DataHolder dataHolder, MergeFunction mergeFunction) {
        return null;
    }

    @Override
    public Optional<CustomItemData> from(DataContainer dataContainer) {
        return null;
    }

    @Override
    public CustomItemData copy() {
        return null;
    }

    @Override
    public ImmutableCustomItemData asImmutable() {
        return null;
    }

    @Override
    public int getContentVersion() {
        return 0;
    }
}
