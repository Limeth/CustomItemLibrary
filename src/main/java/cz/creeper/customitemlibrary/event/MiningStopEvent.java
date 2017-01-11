package cz.creeper.customitemlibrary.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

import java.util.UUID;

/**
 * Called when the player has stopped mining a block.
 */
@ToString
@AllArgsConstructor
@Getter
public class MiningStopEvent extends AbstractEvent {
    @NonNull
    private final UUID playerId;

    @NonNull
    private final BlockSnapshot snapshot;

    private final int durationTicks;

    @NonNull
    private final Cause cause;

    private Reason miningProgressEventCancelled;

    /**
     * @return The duration in seconds
     */
    public double getDuration() {
        return ((double) durationTicks) / Sponge.getServer().getTicksPerSecond();
    }

    public enum Reason {
        MINING_PROGRESS_EVENT_CANCELLED, STARTED_MINING_OTHER_BLOCK, BUTTON_RELEASED
    }
}
