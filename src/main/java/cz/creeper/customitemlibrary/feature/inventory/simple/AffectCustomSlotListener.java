package cz.creeper.customitemlibrary.feature.inventory.simple;

import org.spongepowered.api.data.property.item.BurningFuelProperty;
import org.spongepowered.api.event.item.inventory.AffectSlotEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;

import java.util.function.Predicate;

/**
 * Called when an {@link AffectSlotEvent} affects a {@link CustomSlotDefinition}.
 */
public interface AffectCustomSlotListener {
    void onAffectCustomSlot(CustomSlot customSlot, SlotTransaction slotTransaction, AffectSlotEvent affectSlotEvent);

    static AffectCustomSlotListener cancelAll() {
        return (customSlot, slotTransaction, affectSlotEvent) -> affectSlotEvent.setCancelled(true);
    }

    static AffectCustomSlotListener whitelist(Predicate<ItemStackSnapshot> isAllowed) {
        return (customSlot, slotTransaction, affectSlotEvent) -> {
            if(slotTransaction.getFinal() == ItemStackSnapshot.NONE)
                return;

            if(!isAllowed.test(slotTransaction.getFinal()))
                affectSlotEvent.setCancelled(true);
        };
    }

    static AffectCustomSlotListener blacklist(Predicate<ItemStackSnapshot> isDenied) {
        return whitelist(isDenied.negate());
    }

    static AffectCustomSlotListener fuelOnly() {
        return whitelist(itemStackSnapshot ->
                itemStackSnapshot.getProperty(BurningFuelProperty.class).isPresent());
    }

    static AffectCustomSlotListener output() {
        return (customSlot, slotTransaction, affectSlotEvent) -> {
            if(slotTransaction.getFinal() == ItemStackSnapshot.NONE)
                return;

            ItemStack finalStackOne = slotTransaction.getFinal().createStack();
            ItemStack originalStackOne = slotTransaction.getOriginal().createStack();

            finalStackOne.setQuantity(1);
            originalStackOne.setQuantity(1);

            if(!finalStackOne.equals(originalStackOne)) {
                affectSlotEvent.setCancelled(true);
                return;
            }

            if(slotTransaction.getFinal().getCount() > slotTransaction.getOriginal().getCount())
                affectSlotEvent.setCancelled(true);
        };
    }
}
