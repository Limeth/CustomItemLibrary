package cz.creeper.customitemlibrary.feature.block;

import com.google.common.base.Preconditions;
import cz.creeper.customitemlibrary.util.Block;
import lombok.Getter;
import lombok.NonNull;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.extent.Extent;

import java.util.UUID;

@Getter
public class SimpleCustomBlock implements CustomBlock<SimpleCustomBlockDefinition> {
    @NonNull
    private final SimpleCustomBlockDefinition definition;

    @NonNull
    private final Block block;

    @NonNull
    private final UUID dataHolderId;

    public SimpleCustomBlock(SimpleCustomBlockDefinition definition, Block block, UUID dataHolderId) {
        Preconditions.checkArgument(block.getExtent().isPresent(), "Invalid extent.");
        Preconditions.checkArgument(getExtent().getEntity(dataHolderId).isPresent(), "The data holder is not accessible.");

        this.definition = definition;
        this.block = block;
        this.dataHolderId = dataHolderId;
    }

    public SimpleCustomBlock(SimpleCustomBlockDefinition definition, Block block, Entity dataHolder) {
        this(definition, block, dataHolder.getUniqueId());
    }

    @Override
    public String getModel() {
        return null;
    }

    @Override
    public void setModel(String model) {

    }

    @Override
    public DataHolder getDataHolder() {
        return getExtent().getEntity(dataHolderId)
                .orElseThrow(() -> new IllegalStateException("Could not access the data holder. Did the entity disappear?"));
    }

    public Extent getExtent() {
        return block.getExtent()
                .orElseThrow(() -> new IllegalStateException("Could not access the extent of the block."));
    }
}
