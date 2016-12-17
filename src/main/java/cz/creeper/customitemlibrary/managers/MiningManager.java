package cz.creeper.customitemlibrary.managers;

import com.google.common.collect.Maps;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.event.MiningProgressEvent;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.filter.cause.First;

import java.util.Map;
import java.util.UUID;

public class MiningManager {
    private final Map<UUID, Mining> playerToMining = Maps.newHashMap();

    public MiningManager start() {
        Sponge.getEventManager().registerListeners(CustomItemLibrary.getInstance(), this);

        return this;
    }

    public void stop() {
        Sponge.getEventManager().unregisterListeners(this);
    }

    @Listener(order = Order.BEFORE_POST)
    public void onInteractBlock(InteractBlockEvent.Primary.MainHand event, @First Player player) {
        UUID playerId = player.getUniqueId();
        int currentTick = Sponge.getServer().getRunningTimeTicks();
        BlockSnapshot snapshot = event.getTargetBlock();
        Mining mining = playerToMining.get(playerId);

        if(mining != null && BlockSnapshot.NONE.equals(snapshot)) {
            int durationTicks = currentTick - mining.tickStarted;
            MiningProgressEvent miningEvent = new MiningProgressEvent(player, mining.snapshot, durationTicks,
                    Cause.source(CustomItemLibrary.getInstance().getPluginContainer()).build(), false);

            Sponge.getEventManager().post(miningEvent);

            if (miningEvent.isCancelled()) {
                playerToMining.remove(playerId);
            }
        } else {
            playerToMining.put(playerId, new Mining(currentTick, snapshot));
        }
    }

    @AllArgsConstructor
    @ToString
    private static class Mining {
        private final int tickStarted;
        private BlockSnapshot snapshot;
    }
}
