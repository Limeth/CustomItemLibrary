package cz.creeper.customitemlibrary.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;

@ToString
public class CustomBlockData extends AbstractData<CustomBlockData, ImmutableCustomBlockData> {
    public static final UUID UUID_MISSING = new UUID(0, 0);

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private UUID damageIndicatorArmorStandId;

    public CustomBlockData(UUID damageIndicatorArmorStandId) {
        this.damageIndicatorArmorStandId = damageIndicatorArmorStandId;
        registerGettersAndSetters();
    }

    public CustomBlockData() {
        this(UUID_MISSING);
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(CustomItemLibraryKeys.CUSTOM_BLOCK_DAMAGE_INDICATOR_ARMOR_STAND_ID, this::getDamageIndicatorArmorStandId);
        registerFieldSetter(CustomItemLibraryKeys.CUSTOM_BLOCK_DAMAGE_INDICATOR_ARMOR_STAND_ID, this::setDamageIndicatorArmorStandId);
        registerKeyValue(CustomItemLibraryKeys.CUSTOM_BLOCK_DAMAGE_INDICATOR_ARMOR_STAND_ID, this::damageIndicatorArmorStandId);
    }

    public Value<UUID> damageIndicatorArmorStandId() {
        return damageIndicatorArmorStandId(damageIndicatorArmorStandId);
    }

    public static Value<UUID> damageIndicatorArmorStandId(UUID damageIndicatorArmorStandId) {
        return Sponge.getRegistry().getValueFactory().createValue(CustomItemLibraryKeys.CUSTOM_BLOCK_DAMAGE_INDICATOR_ARMOR_STAND_ID, damageIndicatorArmorStandId, UUID_MISSING);
    }


    @Override
    @Nonnull
    public Optional<CustomBlockData> fill(@Nonnull DataHolder dataHolder, @Nonnull MergeFunction mergeFunction) {
        CustomBlockData data = new CustomBlockData();

        dataHolder.get(CustomItemLibraryKeys.CUSTOM_BLOCK_DAMAGE_INDICATOR_ARMOR_STAND_ID)
                .ifPresent(damageIndicatorArmorStandId -> data.damageIndicatorArmorStandId = damageIndicatorArmorStandId);

        return Optional.of(data);
    }

    @Override
    @Nonnull
    public Optional<CustomBlockData> from(@Nonnull DataContainer dataContainer) {
        Optional<UUID> damageIndicatorArmorStandId = dataContainer.getObject(CustomItemLibraryKeys.CUSTOM_BLOCK_DAMAGE_INDICATOR_ARMOR_STAND_ID.getQuery(), UUID.class);

        if(damageIndicatorArmorStandId.isPresent()) {
            return Optional.of(new CustomBlockData(damageIndicatorArmorStandId.get()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    @Nonnull
    public CustomBlockData copy() {
        return new CustomBlockData(damageIndicatorArmorStandId);
    }

    @Override
    @Nonnull
    public ImmutableCustomBlockData asImmutable() {
        return new ImmutableCustomBlockData(damageIndicatorArmorStandId);
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
