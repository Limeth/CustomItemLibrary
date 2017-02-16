package cz.creeper.customitemlibrary.feature.block.simple;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.feature.AssetId;
import cz.creeper.customitemlibrary.feature.CustomFeatureRegistry;
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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Getter
public class SimpleCustomBlockDefinition extends AbstractCustomBlockDefinition<SimpleCustomBlock> {
    public static final double MODEL_OFFSET_DAMAGE_INDICATOR = 0.0005;

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
                                        Iterable<String> additionalAssets, boolean rotateHorizontally,
                                        boolean generateDamageIndicatorModels, Consumer<SimpleCustomBlock> onUpdate) {
        super(pluginContainer, typeId, defaultModel, additionalModels, effectState, rotateHorizontally, generateDamageIndicatorModels, onUpdate);

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
                                                     boolean rotateHorizontally,
                                                     Boolean generateDamageIndicatorModels,
                                                     Consumer<SimpleCustomBlock> onUpdate) {
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

        if(generateDamageIndicatorModels == null)
            generateDamageIndicatorModels = false;

        return new SimpleCustomBlockDefinition(pluginContainer, typeId, correctToolPredicate, hardness, effectState, dropProvider, defaultModel, additionalModels, additionalAssets, rotateHorizontally, generateDamageIndicatorModels, onUpdate);
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

    @Override
    public void generateResourcePackFiles(Path resourcePackDirectory) {
        super.generateResourcePackFiles(resourcePackDirectory);

        if(isGenerateDamageIndicatorModels()) {
            PluginContainer pluginContainer = getPluginContainer();

            getModels().forEach(model -> {
                String assetPath = SimpleCustomBlockDefinition.getModelPath(model);
                String filePath = CustomFeatureRegistry.getFilePath(pluginContainer, assetPath);
                Path inputFile = resourcePackDirectory.resolve(filePath);

                for(int stage = 0; stage < 10; stage++) {
                    String outputFileName = SimpleCustomBlockRegistry.getDamageIndicatorModel(model, stage) + ".json";
                    Path outputFile = inputFile.resolveSibling(outputFileName);

                    try {
                        createDamageIndicatorFile(inputFile, outputFile, stage);
                    } catch (IOException e) {
                        CustomItemLibrary.getInstance().getLogger()
                                .error("Could not create damage indicator file '" + outputFile + "'.", e);
                    }
                }
            });
        }
    }

    @Override
    public Set<AssetId> getAssets() {
        return assets.stream()
                .map(assetPath -> new AssetId(getPluginContainer(), assetPath))
                .collect(Collectors.toSet());
    }

    public static void createDamageIndicatorFile(Path inputFile, Path outputFile, int stage) throws IOException {
        JsonParser parser = new JsonParser();
        JsonObject root;

        try(Reader reader = Files.newBufferedReader(inputFile)) {
            root = parser.parse(reader).getAsJsonObject();
        }

        JsonObject textures = new JsonObject();

        textures.addProperty("0", "minecraft:blocks/destroy_stage_" + stage);
        root.add("textures", textures);

        JsonArray elements = root.getAsJsonArray("elements");

        elements.forEach(element -> {
            JsonObject elementObject = element.getAsJsonObject();
            JsonArray from = elementObject.getAsJsonArray("from");
            JsonArray to = elementObject.getAsJsonArray("to");
            JsonArray newFrom = new JsonArray();
            JsonArray newTo = new JsonArray();

            for(int i = 0; i < 3; i++) {
                int sign = from.get(i).getAsDouble() < to.get(i).getAsDouble() ? 1 : -1;
                newFrom.add(new JsonPrimitive(Math.max(-16, Math.min(32, from.get(i).getAsDouble() - sign * MODEL_OFFSET_DAMAGE_INDICATOR))));
                newTo.add(new JsonPrimitive(Math.max(-16, Math.min(32, to.get(i).getAsDouble() + sign * MODEL_OFFSET_DAMAGE_INDICATOR))));
            }

            elementObject.add("from", newFrom);
            elementObject.add("to", newTo);
            elementObject.getAsJsonObject("faces").entrySet()
                    .forEach(entry -> {
                        String face = entry.getKey();
                        JsonObject options = entry.getValue().getAsJsonObject();

                        options.addProperty("texture", "#0");
                        options.remove("uv");  // Stretch texture automatically
                    });
        });

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try(Writer writer = Files.newBufferedWriter(outputFile)) {
            gson.toJson(root, writer);
        }
    }
}
