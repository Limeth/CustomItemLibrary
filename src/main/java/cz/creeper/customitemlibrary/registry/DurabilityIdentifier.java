package cz.creeper.customitemlibrary.registry;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.Optional;

public class DurabilityIdentifier {
    public static final char DURABILITY_SEPARATOR = '@';
    @Getter
    private final ItemType itemType;
    @Getter
    private final int durability;

    public DurabilityIdentifier(ItemStackSnapshot itemStackSnapshot) {
        itemType = itemStackSnapshot.getType();
        durability = itemStackSnapshot.get(Keys.ITEM_DURABILITY)
                .orElseThrow(() -> new IllegalArgumentException("This item does not have a durability."));
    }

    public DurabilityIdentifier(ItemStack itemStack) {
        itemType = itemStack.getItem();
        durability = itemStack.get(Keys.ITEM_DURABILITY)
            .orElseThrow(() -> new IllegalArgumentException("This item does not have a durability."));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public DurabilityIdentifier(ItemType itemType, int durability, boolean checkArguments) {
        // FIXME remove checkArguments
        if(checkArguments) {
            Optional<Integer> numberOfUses = CustomToolDefinition.getNumberOfUses(ItemStack.of(itemType, 1));
            Preconditions.checkArgument(numberOfUses.isPresent(), "This item type does not have a durability.");
            Preconditions.checkArgument(durability >= 0 && durability < numberOfUses.get(),
                    "Durability out of bounds. Min: 0; Max: " + (numberOfUses.get() - 1) + "; Provided: " + durability);
        }

        this.itemType = itemType;
        this.durability = durability;
    }

    public static DurabilityIdentifier parse(String string) {
        int separatorIndex = string.lastIndexOf(DURABILITY_SEPARATOR);
        String id = string.substring(0, separatorIndex);
        String rawDurability = string.substring(separatorIndex + 1);
        ItemType itemType = Sponge.getRegistry().getType(ItemType.class, id)
                .orElseThrow(() -> new IllegalArgumentException("Could not find an ItemType for id '" + id + "'."));
        int durability = Integer.valueOf(rawDurability);

        return new DurabilityIdentifier(itemType, durability, false);
    }

    @Override
    public String toString() {
        return itemType.getName() + DURABILITY_SEPARATOR + durability;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return getClass().equals(obj.getClass()) && toString().equals(obj.toString());
    }
}
