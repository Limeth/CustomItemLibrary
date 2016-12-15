package cz.creeper.customitemlibrary.feature.block;

import cz.creeper.customitemlibrary.util.Block;
import lombok.EqualsAndHashCode;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
public class SimpleCustomBlockDefinition extends AbstractCustomBlockDefinition<SimpleCustomBlock> {
    public SimpleCustomBlockDefinition(PluginContainer pluginContainer, String typeId, String defaultModel, Iterable<String> models) {
        super(pluginContainer, typeId, defaultModel, models);
    }

    @Override
    public SimpleCustomBlock placeBlock(Block block, Cause cause) {
        return null;
    }

    @Override
    protected Optional<SimpleCustomBlock> wrapBarrierIfPossible(Block block) {
        return null;
    }
}
