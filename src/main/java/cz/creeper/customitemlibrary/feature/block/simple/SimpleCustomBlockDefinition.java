package cz.creeper.customitemlibrary.feature.block.simple;

import com.google.common.collect.ImmutableSet;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.feature.block.AbstractCustomBlockDefinition;
import cz.creeper.customitemlibrary.util.Block;
import cz.creeper.customitemlibrary.util.Util;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Getter
public class SimpleCustomBlockDefinition extends AbstractCustomBlockDefinition<SimpleCustomBlock> {
    private final ImmutableSet<String> assets;

    private SimpleCustomBlockDefinition(PluginContainer pluginContainer, String typeId, String defaultModel, Iterable<String> models, Iterable<String> additionalAssets) {
        super(pluginContainer, typeId, defaultModel, models);

        this.assets = ImmutableSet.<String>builder()
                .addAll(getModels().stream()
                        .map(SimpleCustomBlockDefinition::getModelPath)
                        .collect(Collectors.toSet()))
                .addAll(Util.removeNull(additionalAssets)
                        .collect(Collectors.toSet()))
                .build();
    }

    @Builder
    public static SimpleCustomBlockDefinition create(Object plugin, String typeId, String defaultModel, @Singular Iterable<String> models, @Singular Iterable<String> additionalAssets) {
        PluginContainer pluginContainer = Sponge.getPluginManager().fromInstance(plugin)
                .orElseThrow(() -> new IllegalArgumentException("Invalid plugin instance."));

        return new SimpleCustomBlockDefinition(pluginContainer, typeId, defaultModel, models, additionalAssets);
    }

    @Override
    public SimpleCustomBlock customizeBlock(Block block, ArmorStand armorStand, Cause cause) {
        return new SimpleCustomBlock(this, block, armorStand);
    }

    @Override
    protected Optional<SimpleCustomBlock> wrapBarrierIfPossible(Block block) {
        return CustomItemLibrary.getInstance().getService().getArmorStandAt(block)
                .map(armorStand -> new SimpleCustomBlock(this, block, armorStand));
    }
}
