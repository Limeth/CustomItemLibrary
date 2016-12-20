package cz.creeper.customitemlibrary.managers;

import com.google.common.collect.Maps;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.event.MiningProgressEvent;
import cz.creeper.customitemlibrary.util.Wrapper;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.Value;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.item.EfficiencyProperty;
import org.spongepowered.api.data.property.item.HarvestingProperty;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.Enchantments;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Handles mining progress on the server-side.
 */
public class MiningManager {
    // A duration when the player can't mine other blocks after they broke one.
    public static final int BREAK_DELAY_TICKS = 5;
    private final Map<UUID, Mining> playerToMining = Maps.newHashMap();
    private final Map<UUID, Integer> playerToBlockBreak = Maps.newHashMap();

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
        int durationTicks;

        // BlockSnapshot.NONE signifies, that the mouse button is being held on the clicked block
        if(BlockSnapshot.NONE.equals(snapshot)) {
            if(mining == null) {
                return;
            }
        } else {
            Integer blockBreakTick = playerToBlockBreak.get(playerId);
            int delay;

            if(blockBreakTick != null) {
                int blockBreakDiff = currentTick - blockBreakTick;
                delay = blockBreakDiff < BREAK_DELAY_TICKS ? BREAK_DELAY_TICKS - blockBreakDiff : 0;
            } else {
                delay = 0;
            }

            mining = new Mining(currentTick + delay, snapshot);

            playerToMining.put(playerId, mining);
        }

        durationTicks = currentTick - mining.tickStarted;

        if(durationTicks < 0)
            durationTicks = 0;

        MiningProgressEvent miningEvent = new MiningProgressEvent(player, mining.snapshot, durationTicks,
                Cause.source(CustomItemLibrary.getInstance().getPluginContainer()).notifier(player).build(), false);

        Sponge.getEventManager().post(miningEvent);

        if (miningEvent.isCancelled()) {
            playerToMining.remove(playerId);
        }
    }

    @Listener(order = Order.POST)
    public void onChangeBlockBreak(ChangeBlockEvent.Break event, @First Player player) {
        UUID playerId = player.getUniqueId();
        int currentTick = Sponge.getServer().getRunningTimeTicks();

        playerToBlockBreak.put(playerId, currentTick);
    }

    @AllArgsConstructor
    @ToString
    private static class Mining {
        private final int tickStarted;
        private BlockSnapshot snapshot;
    }

    public static MiningDuration computeDuration(Player player, BlockType harvestingType, double hardness) {
        Optional<ItemStack> itemInHand = player.getItemInHand(HandTypes.MAIN_HAND);
        // TODO: possibly generalize
        boolean correctToolUsed = itemInHand.flatMap(itemStack -> itemStack.getProperty(HarvestingProperty.class))
                .map(HarvestingProperty::getValue)
                .map(harvestingTypes -> harvestingTypes.contains(harvestingType))
                .orElse(true);
        // java, plz.
        final Wrapper<Double> breakDuration = Wrapper.of(hardness);

        if(correctToolUsed) {
            breakDuration.setValue(breakDuration.getValue() * 1.5);

            itemInHand.ifPresent(itemStack -> {
                final Wrapper<Double> totalEfficiency = Wrapper.of(1.0);

                itemStack.getProperty(EfficiencyProperty.class)
                        .map(EfficiencyProperty::getValue)
                        .ifPresent(efficiency ->
                                totalEfficiency.setValue(totalEfficiency.getValue() * efficiency)
                        );

                itemStack.get(Keys.ITEM_ENCHANTMENTS)
                        .ifPresent(itemEnchantments -> {
                            itemEnchantments.forEach(itemEnchantment -> {
                                Enchantment type = itemEnchantment.getEnchantment();
                                int level = itemEnchantment.getLevel();

                                if(type == Enchantments.EFFICIENCY) {
                                    totalEfficiency.setValue(totalEfficiency.getValue()
                                            + level * level + 1);
                                }
                            });
                        });

                breakDuration.setValue(breakDuration.getValue() / totalEfficiency.getValue());
            });
        } else {
            breakDuration.setValue(breakDuration.getValue() * 5);
        }

        player.get(Keys.POTION_EFFECTS).ifPresent(potionEffects -> {
            potionEffects.forEach(potionEffect -> {
                PotionEffectType type = potionEffect.getType();
                int amplifier = potionEffect.getAmplifier();

                if(type == PotionEffectTypes.MINING_FATIGUE) {
                    breakDuration.setValue(breakDuration.getValue()
                            * (1 - Math.pow(0.3, amplifier)));
                } else if(type == PotionEffectTypes.HASTE) {
                    breakDuration.setValue(breakDuration.getValue()
                            * (1.2 * amplifier));
                }
            });
        });

        return new MiningDuration(correctToolUsed, breakDuration.getValue());
    }

    @Value
    public static class MiningDuration {
        private boolean correctToolUsed;
        private double breakDuration;
    }
}
