package cz.creeper.customitemlibrary.feature.block;

import com.google.common.base.Preconditions;
import cz.creeper.customitemlibrary.feature.AbstractCustomFeature;
import cz.creeper.customitemlibrary.util.Block;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.spongepowered.api.entity.living.ArmorStand;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Getter
public abstract class AbstractCustomBlock<T extends CustomBlockDefinition<? extends AbstractCustomBlock<T>>> extends AbstractCustomFeature<T> implements CustomBlock<T> {
    @NonNull
    private final Block block;

    @NonNull
    private final UUID armorStandId;

    public AbstractCustomBlock(T definition, Block block, UUID armorStandId) {
        super(definition);
        Preconditions.checkArgument(block.getExtent().isPresent(), "Invalid extent.");

        this.block = block;

        Preconditions.checkArgument(getExtent().getEntity(armorStandId)
                .filter(ArmorStand.class::isInstance).isPresent(), "The armor stand is not accessible.");

        this.armorStandId = armorStandId;
    }

    public AbstractCustomBlock(T definition, Block block, ArmorStand armorStand) {
        this(definition, block, armorStand.getUniqueId());
    }
}
