package cz.creeper.customitemlibrary.feature.block;

import cz.creeper.customitemlibrary.feature.DurabilityRegistry;
import cz.creeper.customitemlibrary.util.Block;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.spongepowered.api.data.key.Keys;
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
        super(definition, block, armorStand.getUniqueId());
    }

    public ItemStack createHelmet(String model) {
        PluginContainer pluginContainer = getDefinition().getPluginContainer();
        int durability = DurabilityRegistry.getInstance().getDurability(HELMET_ITEM_TYPE, pluginContainer, model)
                .orElseThrow(() -> new IllegalStateException("The model should have been registered by now, is the definition actually being registered?"));

        ItemStack itemStack = ItemStack.of(HELMET_ITEM_TYPE, 1);

        itemStack.offer(Keys.ITEM_DURABILITY, durability);

        return itemStack;
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
