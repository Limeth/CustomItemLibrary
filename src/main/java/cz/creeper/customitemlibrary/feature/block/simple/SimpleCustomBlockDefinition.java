package cz.creeper.customitemlibrary.feature.block.simple;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.feature.block.AbstractCustomBlockDefinition;
import cz.creeper.customitemlibrary.feature.item.CustomItem;
import cz.creeper.customitemlibrary.util.Block;
import cz.creeper.customitemlibrary.util.Util;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.property.block.HardnessProperty;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Getter
public class SimpleCustomBlockDefinition extends AbstractCustomBlockDefinition<SimpleCustomBlock> {
    @NonNull
    private final ImmutableSet<String> assets;

    /**
     * Determines whether the tool used to break the block
     * should be used to destroy blocks of this {@link SimpleCustomBlockDefinition}.
     */
    @NonNull
    private final CorrectToolPredicate correctToolPredicate;

    /**
     * The duration this block takes to break.
     */
    private final double hardness;

    /**
     * A list of items dropped when the block is broken.
     */
    @NonNull
    private final DropProvider dropProvider;

    private SimpleCustomBlockDefinition(PluginContainer pluginContainer, String typeId,
                                        @NonNull CorrectToolPredicate correctToolPredicate, double hardness,
                                        @NonNull BlockState effectState, @NonNull DropProvider dropProvider,
                                        String defaultModel, Iterable<String> additionalModels,
                                        Iterable<String> additionalAssets, boolean rotateHorizontally) {
        super(pluginContainer, typeId, defaultModel, additionalModels, effectState, rotateHorizontally);

        this.assets = ImmutableSet.<String>builder()
                .addAll(getModels().stream()
                        .map(SimpleCustomBlockDefinition::getModelPath)
                        .collect(Collectors.toSet()))
                .addAll(Util.removeNull(additionalAssets)
                        .collect(Collectors.toSet()))
                .build();
        this.correctToolPredicate = correctToolPredicate;
        this.hardness = hardness;
        this.dropProvider = dropProvider;
    }

    @Builder
    public static SimpleCustomBlockDefinition create(Object plugin, String typeId,
                                                     CorrectToolPredicate correctToolPredicate,
                                                     Double hardness, BlockState effectState,
                                                     DropProvider dropProvider, String defaultModel,
                                                     @Singular Iterable<String> additionalModels,
                                                     @Singular Iterable<String> additionalAssets,
                                                     boolean rotateHorizontally) {
        PluginContainer pluginContainer = Sponge.getPluginManager().fromInstance(plugin)
                .orElseThrow(() -> new IllegalArgumentException("Invalid plugin instance."));
        if(effectState == null)
            effectState = BlockTypes.STONE.getDefaultState();

        if(correctToolPredicate == null)
            correctToolPredicate = CorrectToolPredicate.of(effectState.getType());

        if(hardness == null)
            hardness = effectState
                    .getProperty(HardnessProperty.class)
                    .orElseThrow(() -> new IllegalStateException("Could not access the HardnessProperty of the specified harvestingType. Define the hardness manually."))
                    .getValue();
        Preconditions.checkNotNull(hardness);
        Preconditions.checkArgument(hardness >= 0, "The hardness must be non-negative.");

        if(dropProvider == null)
            dropProvider = (a, b, cause) -> CustomItemLibrary.getInstance().getService().getItemDefinition(plugin, typeId)
                    .map(itemDefinition -> itemDefinition.createItem(cause))
                    .map(CustomItem::getDataHolder)
                    .map(ItemStack::createSnapshot)
                    .map(Collections::singletonList)
                    .orElseGet(Collections::emptyList);

        return new SimpleCustomBlockDefinition(pluginContainer, typeId, correctToolPredicate, hardness, effectState, dropProvider, defaultModel, additionalModels, additionalAssets, rotateHorizontally);
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
