package cz.creeper.customitemlibrary.data;

import lombok.ToString;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;

@ToString
public class CustomBlockManipulatorBuilder extends AbstractDataBuilder<CustomBlockData>
        implements DataManipulatorBuilder<CustomBlockData, ImmutableCustomBlockData> {
    public CustomBlockManipulatorBuilder() {
        super(CustomBlockData.class, 1);
    }

    @Override
    @Nonnull
    public CustomBlockData create() {
        return new CustomBlockData();
    }

    @Override
    @Nonnull
    public Optional<CustomBlockData> createFrom(@Nonnull DataHolder dataHolder) {
        return dataHolder.get(CustomItemLibraryKeys.CUSTOM_BLOCK_DAMAGE_INDICATOR_ARMOR_STAND_ID)
                .map(CustomBlockData::new);
    }

    @Override
    @Nonnull
    protected Optional<CustomBlockData> buildContent(@Nonnull DataView container) throws InvalidDataException {
        return container.getObject(CustomItemLibraryKeys.CUSTOM_BLOCK_DAMAGE_INDICATOR_ARMOR_STAND_ID.getQuery(), UUID.class)
                .map(CustomBlockData::new);
    }
}
