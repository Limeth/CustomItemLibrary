package cz.creeper.customitemlibrary.event;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.world.World;

import java.util.List;

@Getter
public class CustomBlockPlaceEvent extends AbstractEvent implements ChangeBlockEvent.Place {
    private final List<Transaction<BlockSnapshot>> transactions;

    private final World targetWorld;

    private final Cause cause;

    @Setter
    private boolean cancelled;

    public CustomBlockPlaceEvent(List<Transaction<BlockSnapshot>> transactions, World targetWorld, Cause cause, boolean cancelled) {
        this.transactions = ImmutableList.copyOf(transactions);
        this.targetWorld = targetWorld;
        this.cause = cause;
        this.cancelled = cancelled;
    }
}
