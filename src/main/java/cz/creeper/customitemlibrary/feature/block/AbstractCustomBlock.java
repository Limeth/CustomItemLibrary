package cz.creeper.customitemlibrary.feature.block;

import com.google.common.base.Preconditions;
import cz.creeper.customitemlibrary.CustomItemServiceImpl;
import cz.creeper.customitemlibrary.feature.AbstractCustomModelledFeature;
import cz.creeper.customitemlibrary.util.Block;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.spongepowered.api.entity.living.ArmorStand;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Getter
public abstract class AbstractCustomBlock<T extends CustomBlockDefinition<? extends AbstractCustomBlock<T>>> extends AbstractCustomModelledFeature<T> implements CustomBlock<T> {
    @NonNull
    private final Block block;

    @NonNull
    private final UUID armorStandId;

    public AbstractCustomBlock(T definition, Block block, UUID armorStandId) {
        super(definition);
        Preconditions.checkArgument(block.getWorld().isPresent(), "Invalid extent.");

        this.block = block;

        Preconditions.checkArgument(getExtent().getEntity(armorStandId)
                .filter(ArmorStand.class::isInstance).isPresent(), "The armor stand is not accessible.");

        this.armorStandId = armorStandId;
    }

    public AbstractCustomBlock(T definition, Block block, ArmorStand armorStand) {
        this(definition, block, armorStand.getUniqueId());
    }

    @Override
    public boolean isAccessible() {
        return block.getLocation().map(location ->
                location.getExtent().getEntity(armorStandId)
                        .map(CustomItemServiceImpl::isCustomBlockArmorStand).orElse(false))
                .orElse(false);
    }
}
