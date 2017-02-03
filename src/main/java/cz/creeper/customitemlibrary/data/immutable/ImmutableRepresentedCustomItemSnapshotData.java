package cz.creeper.customitemlibrary.data.immutable;

import cz.creeper.customitemlibrary.data.CustomItemLibraryKeys;
import cz.creeper.customitemlibrary.data.mutable.RepresentedCustomItemSnapshotData;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import javax.annotation.Nonnull;

@ToString
public class ImmutableRepresentedCustomItemSnapshotData extends AbstractImmutableData<ImmutableRepresentedCustomItemSnapshotData, RepresentedCustomItemSnapshotData> {
    @Getter(AccessLevel.PRIVATE)
    @NonNull
    private ItemStackSnapshot representedCustomItemSnapshot;

    public ImmutableRepresentedCustomItemSnapshotData(ItemStackSnapshot representedCustomItemSnapshot) {
        this.representedCustomItemSnapshot = representedCustomItemSnapshot;
        registerGetters();
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(CustomItemLibraryKeys.REPRESENTED_CUSTOM_ITEM_SNAPSHOT, this::getRepresentedCustomItemSnapshot);
        registerKeyValue(CustomItemLibraryKeys.REPRESENTED_CUSTOM_ITEM_SNAPSHOT, this::representedCustomItemSnapshot);
    }

    public ImmutableValue<ItemStackSnapshot> representedCustomItemSnapshot() {
        return RepresentedCustomItemSnapshotData.representedCustomItemSnapshot(representedCustomItemSnapshot)
                .asImmutable();
    }

    @Override
    @Nonnull
    public RepresentedCustomItemSnapshotData asMutable() {
        return new RepresentedCustomItemSnapshotData(representedCustomItemSnapshot);
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    @Nonnull
    public DataContainer toContainer() {
        return super.toContainer()
                .set(CustomItemLibraryKeys.REPRESENTED_CUSTOM_ITEM_SNAPSHOT.getQuery(), representedCustomItemSnapshot);
    }
}
