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
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import javax.annotation.Nonnull;
import java.util.Optional;

@ToString
public class RepresentedCustomItemSnapshotData extends AbstractData<RepresentedCustomItemSnapshotData, ImmutableRepresentedCustomItemSnapshotData> {
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    @NonNull
    private ItemStackSnapshot representedCustomItemSnapshot;

    public RepresentedCustomItemSnapshotData(ItemStackSnapshot representedCustomItemSnapshot) {
        this.representedCustomItemSnapshot = representedCustomItemSnapshot;
        registerGettersAndSetters();
    }

    public RepresentedCustomItemSnapshotData() {
        this(ItemStackSnapshot.NONE);
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(CustomItemLibraryKeys.REPRESENTED_CUSTOM_ITEM_SNAPSHOT, this::getRepresentedCustomItemSnapshot);
        registerFieldSetter(CustomItemLibraryKeys.REPRESENTED_CUSTOM_ITEM_SNAPSHOT, this::setRepresentedCustomItemSnapshot);
        registerKeyValue(CustomItemLibraryKeys.REPRESENTED_CUSTOM_ITEM_SNAPSHOT, this::representedCustomItemSnapshot);
    }

    public Value<ItemStackSnapshot> representedCustomItemSnapshot() {
        return representedCustomItemSnapshot(representedCustomItemSnapshot);
    }

    public static Value<ItemStackSnapshot> representedCustomItemSnapshot(ItemStackSnapshot representedCustomItemSnapshot) {
        return Sponge.getRegistry().getValueFactory().createValue(CustomItemLibraryKeys.REPRESENTED_CUSTOM_ITEM_SNAPSHOT, representedCustomItemSnapshot, ItemStackSnapshot.NONE);
    }

    @Override
    @Nonnull
    public Optional<RepresentedCustomItemSnapshotData> fill(@Nonnull DataHolder dataHolder, @Nonnull MergeFunction mergeFunction) {
        RepresentedCustomItemSnapshotData data = new RepresentedCustomItemSnapshotData();

        dataHolder.get(CustomItemLibraryKeys.REPRESENTED_CUSTOM_ITEM_SNAPSHOT)
                .ifPresent(itemStackSnapshot -> data.representedCustomItemSnapshot = itemStackSnapshot);

        return Optional.of(data);
    }

    @Override
    @Nonnull
    public Optional<RepresentedCustomItemSnapshotData> from(@Nonnull DataContainer dataContainer) {
        return dataContainer.getObject(CustomItemLibraryKeys.REPRESENTED_CUSTOM_ITEM_SNAPSHOT.getQuery(),
                                       ItemStackSnapshot.class)
                .map(RepresentedCustomItemSnapshotData::new);
    }

    @Override
    @Nonnull
    public RepresentedCustomItemSnapshotData copy() {
        return new RepresentedCustomItemSnapshotData(representedCustomItemSnapshot);
    }

    @Override
    @Nonnull
    public ImmutableRepresentedCustomItemSnapshotData asImmutable() {
        return new ImmutableRepresentedCustomItemSnapshotData(representedCustomItemSnapshot);
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
