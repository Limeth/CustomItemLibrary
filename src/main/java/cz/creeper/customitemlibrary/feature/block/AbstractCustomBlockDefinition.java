package cz.creeper.customitemlibrary.feature.block;

import cz.creeper.customitemlibrary.feature.AbstractCustomModelledFeatureDefinition;
import cz.creeper.customitemlibrary.util.Block;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Getter
public abstract class AbstractCustomBlockDefinition<T extends CustomBlock<? extends AbstractCustomBlockDefinition<T>>> extends AbstractCustomModelledFeatureDefinition<T> implements CustomBlockDefinition<T> {
    @NonNull
    private final BlockState effectState;

    private final boolean rotateHorizontally;

    private final boolean generateDamageIndicatorModels;

    private final Consumer<T> onUpdate;

    public AbstractCustomBlockDefinition(PluginContainer pluginContainer, String typeId, String defaultModel, Iterable<String> additionalModels, BlockState effectState, boolean rotateHorizontally, boolean generateDamageIndicatorModels, Consumer<T> onUpdate) {
        super(pluginContainer, typeId, defaultModel, additionalModels);

        this.effectState = effectState;
        this.rotateHorizontally = rotateHorizontally;
        this.generateDamageIndicatorModels = generateDamageIndicatorModels;
        this.onUpdate = onUpdate != null ? onUpdate : block -> {};
    }

    protected abstract Optional<T> wrapBarrierIfPossible(Block block);

    @Override
    public void update(T block) {
        this.onUpdate.accept(block);
    }

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
