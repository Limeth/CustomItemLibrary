package cz.creeper.customitemlibrary.data.builder;

import cz.creeper.customitemlibrary.data.CustomItemLibraryKeys;
import cz.creeper.customitemlibrary.data.immutable.ImmutableRepresentedCustomItemSnapshotData;
import cz.creeper.customitemlibrary.data.mutable.RepresentedCustomItemSnapshotData;
import lombok.ToString;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.Optional;

import javax.annotation.Nonnull;

@ToString
public class RepresentedCustomItemSnapshotManipulatorBuilder extends AbstractDataBuilder<RepresentedCustomItemSnapshotData>
        implements DataManipulatorBuilder<RepresentedCustomItemSnapshotData, ImmutableRepresentedCustomItemSnapshotData> {
    public RepresentedCustomItemSnapshotManipulatorBuilder() {
        super(RepresentedCustomItemSnapshotData.class, 1);
    }

    @Override
    @Nonnull
    public RepresentedCustomItemSnapshotData create() {
        return new RepresentedCustomItemSnapshotData();
    }

    @Override
    @Nonnull
    public Optional<RepresentedCustomItemSnapshotData> createFrom(@Nonnull DataHolder dataHolder) {
        return dataHolder.get(CustomItemLibraryKeys.REPRESENTED_CUSTOM_ITEM_SNAPSHOT)
                .map(RepresentedCustomItemSnapshotData::new);
    }

    @Override
    @Nonnull
    protected Optional<RepresentedCustomItemSnapshotData> buildContent(@Nonnull DataView container) throws InvalidDataException {
        return container.getObject(CustomItemLibraryKeys.REPRESENTED_CUSTOM_ITEM_SNAPSHOT.getQuery(),
                                   ItemStackSnapshot.class)
                .map(RepresentedCustomItemSnapshotData::new);
    }
}
