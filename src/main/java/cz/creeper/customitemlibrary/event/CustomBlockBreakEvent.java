package cz.creeper.customitemlibrary.event;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cz.creeper.customitemlibrary.feature.block.CustomBlock;
import cz.creeper.customitemlibrary.feature.block.CustomBlockDefinition;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collections;
import java.util.List;

@ToString
@Getter
public class CustomBlockBreakEvent extends AbstractEvent implements ChangeBlockEvent.Break {
    private final CustomBlock<? extends CustomBlockDefinition> customBlock;
    private final List<Transaction<BlockSnapshot>> transactions;
    private final World targetWorld;
    private final Cause cause;

    @Setter
    private boolean cancelled;

    public CustomBlockBreakEvent(@NonNull CustomBlock<? extends CustomBlockDefinition> customBlock,
            @NonNull List<Transaction<BlockSnapshot>> transactions, @NonNull World targetWorld, @NonNull Cause cause,
            boolean cancelled) {
        this.customBlock = customBlock;
        this.transactions = ImmutableList.copyOf(transactions);
        this.targetWorld = targetWorld;
        this.cause = cause;
        this.cancelled = cancelled;
    }

    public static CustomBlockBreakEvent of(CustomBlock<? extends CustomBlockDefinition> customBlock, Cause cause) {
        Preconditions.checkArgument(customBlock.getExtent() instanceof World, "The custom block must be in a World extent.");;

        World extent = (World) customBlock.getExtent();
        Vector3i position = customBlock.getBlock().getPosition();
        Location<World> location = extent.getLocation(position);
        BlockSnapshot original = extent.createSnapshot(position);
        BlockSnapshot defaultReplacement = BlockTypes.AIR.getDefaultState().snapshotFor(location);
        Transaction<BlockSnapshot> transaction = new Transaction<>(original, defaultReplacement);

        return new CustomBlockBreakEvent(customBlock, Collections.singletonList(transaction), extent, cause, false);
    }
}
