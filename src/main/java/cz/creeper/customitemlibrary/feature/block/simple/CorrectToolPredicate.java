package cz.creeper.customitemlibrary.feature.block.simple;

import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.property.item.HarvestingProperty;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;

public interface CorrectToolPredicate {
    /**
     * @param itemInHand The {@link ItemStack} the player is holding in their {@link HandTypes#MAIN_HAND}.
     * @return the list of items to be dropped, when a proper tool is used on a {@link SimpleCustomBlock}.
     */
    boolean isCorrectTool(@Nullable ItemStack itemInHand);

    static CorrectToolPredicate of(BlockType harvestingType) {
        return itemInHand -> Optional.ofNullable(itemInHand)
                .flatMap(itemStack -> itemStack.getProperty(HarvestingProperty.class))
                .map(HarvestingProperty::getValue)
                .map(harvestingTypes -> harvestingTypes.contains(harvestingType))
                .orElse(false);
    }

    static CorrectToolPredicate any() {
        return itemInHand -> true;
    }

    static CorrectToolPredicate none() {
        return itemInHand -> false;
    }
}
