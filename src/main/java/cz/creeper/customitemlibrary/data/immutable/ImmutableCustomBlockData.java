package cz.creeper.customitemlibrary.data.immutable;

import cz.creeper.customitemlibrary.data.mutable.CustomBlockData;
import cz.creeper.customitemlibrary.data.CustomItemLibraryKeys;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

import java.util.UUID;

import javax.annotation.Nonnull;

@ToString
public class ImmutableCustomBlockData
        extends AbstractImmutableData<ImmutableCustomBlockData, CustomBlockData> {
    @NonNull
    @Getter(AccessLevel.PRIVATE)
    private UUID damageIndicatorArmorStandId;

    public ImmutableCustomBlockData(UUID damageIndicatorArmorStandId) {
        this.damageIndicatorArmorStandId = damageIndicatorArmorStandId;
        registerGetters();
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(CustomItemLibraryKeys.CUSTOM_BLOCK_DAMAGE_INDICATOR_ARMOR_STAND_ID, this::getDamageIndicatorArmorStandId);
        registerKeyValue(CustomItemLibraryKeys.CUSTOM_BLOCK_DAMAGE_INDICATOR_ARMOR_STAND_ID, this::damageIndicatorArmorStandId);
    }

    public ImmutableValue<UUID> damageIndicatorArmorStandId() {
        return CustomBlockData.damageIndicatorArmorStandId(damageIndicatorArmorStandId)
                .asImmutable();
    }

    @Override
    @Nonnull
    public CustomBlockData asMutable() {
        return new CustomBlockData(damageIndicatorArmorStandId);
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    @Nonnull
    public DataContainer toContainer() {
        return super.toContainer()
                .set(CustomItemLibraryKeys.CUSTOM_BLOCK_DAMAGE_INDICATOR_ARMOR_STAND_ID.getQuery(), damageIndicatorArmorStandId);
    }
}
