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
import cz.creeper.customitemlibrary.feature.CustomFeatureDefinition;
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
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.property.block.HardnessProperty;
import org.spongepowered.api.effect.sound.SoundType;
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
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Getter
public class SimpleCustomBlockDefinition extends AbstractCustomBlockDefinition<SimpleCustomBlock> {
    public static final double MODEL_OFFSET_DAMAGE_INDICATOR = 0.0005;

    @NonNull
    private final ImmutableSet<String> assets;

    /**
     * The block type this block imitates. Determines, whether items are dropped.
     */
    @NonNull
    private final BlockType harvestingType;

    /**
     * The duration this block takes to break.
     */
    private final double hardness;

    /**
     * The {@link BlockState} used to figure out which particle effect texture to use.
     */
    @NonNull
    private final BlockState breakEffectState;

    /**
     * A list of items dropped when the block is broken.
     */
    @NonNull
    private final DropProvider dropProvider;

    private SimpleCustomBlockDefinition(PluginContainer pluginContainer, String typeId,
                                        @NonNull BlockType harvestingType, double hardness,
                                        @NonNull BlockState breakEffectState, @NonNull DropProvider dropProvider,
                                        String defaultModel, Iterable<String> additionalModels,
                                        Iterable<String> additionalAssets, SoundType soundPlace,
                                        boolean rotateHorizontally, boolean generateDamageIndicatorModels) {
        super(pluginContainer, typeId, defaultModel, additionalModels, soundPlace, rotateHorizontally, generateDamageIndicatorModels);

        this.assets = ImmutableSet.<String>builder()
                .addAll(getModels().stream()
                        .map(SimpleCustomBlockDefinition::getModelPath)
                        .collect(Collectors.toSet()))
                .addAll(Util.removeNull(additionalAssets)
                        .collect(Collectors.toSet()))
                .build();
        this.harvestingType = harvestingType;
        this.hardness = hardness;
        this.breakEffectState = breakEffectState;
        this.dropProvider = dropProvider;
    }

    @Builder
    public static SimpleCustomBlockDefinition create(Object plugin, String typeId, BlockType harvestingType,
                                                     Double hardness, BlockState breakEffectState,
                                                     DropProvider dropProvider, String defaultModel,
                                                     @Singular Iterable<String> additionalModels,
                                                     @Singular Iterable<String> additionalAssets,
                                                     SoundType soundPlace, boolean rotateHorizontally,
                                                     Boolean generateDamageIndicatorModels) {
        PluginContainer pluginContainer = Sponge.getPluginManager().fromInstance(plugin)
                .orElseThrow(() -> new IllegalArgumentException("Invalid plugin instance."));
        if(harvestingType == null)
            harvestingType = BlockTypes.STONE;

        if(hardness == null)
            hardness = harvestingType.getDefaultState()
                    .getProperty(HardnessProperty.class)
                    .orElseThrow(() -> new IllegalStateException("Could not access the HardnessProperty of the specified harvestingType. Define the hardness manually."))
                    .getValue();
        Preconditions.checkNotNull(hardness);
        Preconditions.checkArgument(hardness >= 0, "The hardness must be non-negative.");

        if(breakEffectState == null)
            breakEffectState = harvestingType.getDefaultState();

        if(dropProvider == null)
            dropProvider = (a, b, cause) -> CustomItemLibrary.getInstance().getService().getItemDefinition(plugin, typeId)
                    .map(itemDefinition -> itemDefinition.createItem(cause))
                    .map(CustomItem::getDataHolder)
                    .map(ItemStack::createSnapshot)
                    .map(Collections::singletonList)
                    .orElseGet(Collections::emptyList);

        if(soundPlace == null)
            soundPlace = harvestingType.getSoundGroup().getPlaceSound();

        if(generateDamageIndicatorModels == null)
            generateDamageIndicatorModels = false;

        return new SimpleCustomBlockDefinition(pluginContainer, typeId, harvestingType, hardness, breakEffectState, dropProvider, defaultModel, additionalModels, additionalAssets, soundPlace, rotateHorizontally, generateDamageIndicatorModels);
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
                String filePath = CustomFeatureDefinition.getFilePath(pluginContainer, assetPath);
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
