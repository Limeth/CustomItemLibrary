package cz.creeper.customitemlibrary.feature.block.simple;

import cz.creeper.customitemlibrary.feature.DurabilityRegistry;
import cz.creeper.customitemlibrary.feature.block.AbstractCustomBlock;
import cz.creeper.customitemlibrary.util.Block;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Getter
public class SimpleCustomBlock extends AbstractCustomBlock<SimpleCustomBlockDefinition> {
    public static final ItemType HELMET_ITEM_TYPE = ItemTypes.DIAMOND_SHOVEL;

    public SimpleCustomBlock(SimpleCustomBlockDefinition definition, Block block, UUID armorStandId) {
        super(definition, block, armorStandId);
    }

    public SimpleCustomBlock(SimpleCustomBlockDefinition definition, Block block, ArmorStand armorStand) {
        super(definition, block, armorStand);
    }

    public ItemStack createHelmet(String model) {
        return DurabilityRegistry.getInstance().createItemUnsafe(HELMET_ITEM_TYPE, getDefinition().getPluginContainer(), model);
    }

    @Override
    protected Optional<String> resolveCurrentModel() {
        ArmorStand armorStand = getDataHolder();
        PluginContainer pluginContainer = getDefinition().getPluginContainer();

        return armorStand.getHelmet()
                .flatMap(itemStack -> DurabilityRegistry.resolveCurrentModel(itemStack, pluginContainer));
    }

    @Override
    protected void applyModel(String model) {
        ArmorStand armorStand = getDataHolder();
        ItemStack itemStack = createHelmet(model);

        armorStand.setHelmet(itemStack);
    }
}
