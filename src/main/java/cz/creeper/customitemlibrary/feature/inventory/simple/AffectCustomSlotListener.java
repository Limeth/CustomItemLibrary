package cz.creeper.customitemlibrary.feature.inventory.simple;

import org.spongepowered.api.event.item.inventory.AffectSlotEvent;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;

/**
 * Called when an {@link AffectSlotEvent} affects a {@link CustomSlotDefinition}.
 */
public interface AffectCustomSlotListener {
    void onAffectCustomSlot(CustomSlot customSlot, SlotTransaction slotTransaction, AffectSlotEvent affectSlotEvent);
}
