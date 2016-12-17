package cz.creeper.customitemlibrary.feature.block;

import cz.creeper.customitemlibrary.feature.AbstractCustomFeatureDefinition;
import cz.creeper.customitemlibrary.util.Block;
import lombok.EqualsAndHashCode;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
public abstract class AbstractCustomBlockDefinition<T extends CustomBlock<? extends AbstractCustomBlockDefinition<T>>> extends AbstractCustomFeatureDefinition<T> implements CustomBlockDefinition<T> {
    public AbstractCustomBlockDefinition(PluginContainer pluginContainer, String typeId, String defaultModel, Iterable<String> models) {
        super(pluginContainer, typeId, defaultModel, models);
    }

    protected abstract Optional<T> wrapBarrierIfPossible(Block block);

    @Override
    public Optional<T> wrapIfPossible(Block block) {
        return block.getLocation()
                .filter(location -> location.getBlockType() == CustomBlock.BLOCK_TYPE_CUSTOM)
                .flatMap(location -> wrapBarrierIfPossible(block));
    }

    public Set<String> getModelAssets() {
        return getModels().stream()
                .map(AbstractCustomBlockDefinition::getModelPath)
                .collect(Collectors.toSet());
    }

    public static String getModelPath(String model) {
        return "models/blocks/" + model + ".json";
    }
}
