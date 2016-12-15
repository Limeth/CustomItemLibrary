package cz.creeper.customitemlibrary.feature.block;

import com.google.common.base.Preconditions;
import cz.creeper.customitemlibrary.data.CustomItemLibraryKeys;
import cz.creeper.customitemlibrary.feature.AbstractCustomFeature;
import cz.creeper.customitemlibrary.feature.item.tool.CustomTool;
import cz.creeper.customitemlibrary.util.Block;
import lombok.Getter;
import lombok.NonNull;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.extent.Extent;

import java.util.Optional;
import java.util.UUID;

@Getter
public class SimpleCustomBlock extends AbstractCustomFeature<SimpleCustomBlockDefinition> implements CustomBlock<SimpleCustomBlockDefinition> {
    @NonNull
    private final Block block;

    @NonNull
    private final UUID dataHolderId;

    public SimpleCustomBlock(SimpleCustomBlockDefinition definition, Block block, UUID dataHolderId) {
        super(definition);
        Preconditions.checkArgument(block.getExtent().isPresent(), "Invalid extent.");
        Preconditions.checkArgument(getExtent().getEntity(dataHolderId).isPresent(), "The data holder is not accessible.");

        this.block = block;
        this.dataHolderId = dataHolderId;
    }

    public SimpleCustomBlock(SimpleCustomBlockDefinition definition, Block block, Entity dataHolder) {
        this(definition, block, dataHolder.getUniqueId());
    }

    @Override
    public Entity getDataHolder() {
        return getExtent().getEntity(dataHolderId)
                .orElseThrow(() -> new IllegalStateException("Could not access the data holder. Did the entity disappear?"));
    }

    public Extent getExtent() {
        return block.getExtent()
                .orElseThrow(() -> new IllegalStateException("Could not access the extent of the block."));
    }

    @Override
    protected Optional<String> resolveCurrentModel() {
        return Optional.of(getDataHolder())
                .filter(ArmorStand.class::isInstance)
                .map(ArmorStand.class::cast)
                .flatMap(armorStand -> {
                    ItemStack itemStack = armorStand.getInventory()
                    CustomTool.resolveCurrentModel()
                });
    }

    @Override
    protected void applyModel(String model) {

    }
}
