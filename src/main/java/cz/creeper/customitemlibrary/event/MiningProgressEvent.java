package cz.creeper.customitemlibrary.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

@AllArgsConstructor
@Getter
public class MiningProgressEvent extends AbstractEvent implements Cancellable {
    @NonNull
    private final Player player;

    @NonNull
    private final BlockSnapshot snapshot;

    private final int durationTicks;

    @NonNull
    private final Cause cause;

    @Setter
    private boolean cancelled;

    /**
     * @return The duration in seconds
     */
    public double getDuration() {
        return ((double) durationTicks) / Sponge.getServer().getTicksPerSecond();
    }
}
