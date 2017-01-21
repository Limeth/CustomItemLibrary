package cz.creeper.customitemlibrary.feature.inventory.simple;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.feature.CustomFeatureRegistry;
import cz.creeper.customitemlibrary.feature.DurabilityRegistry;
import lombok.AccessLevel;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

public class SimpleCustomInventoryRegistry implements CustomFeatureRegistry<SimpleCustomInventory, SimpleCustomInventoryDefinition> {
    public static final String MODEL_DIRECTORY_NAME = "gui";
    private static final SimpleCustomInventoryRegistry INSTANCE = new SimpleCustomInventoryRegistry();

    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Set<SimpleCustomInventoryDefinition> definitions = Sets.newHashSet();

    @Override
    public void register(SimpleCustomInventoryDefinition definition) {
        Preconditions.checkNotNull(definition, "definition");
        Preconditions.checkArgument(!definitions.contains(definition), "This definiton has already been registered.");
        List<GUIModel> models = definition.getSlotStream()
                .flatMap(def -> def.getFeatures().values().stream())
                .map(GUIFeature::getModel)
                .distinct()
                .collect(Collectors.toList());
        definition.getSlotStream()
                .flatMap(def -> def.getFeatures().values().stream())
                .map(GUIFeature::getModel)
                .distinct()
                .forEach(SimpleCustomInventoryRegistry::registerGUIModelDurability);
        definitions.add(definition);
    }

    private static void registerGUIModelDurability(GUIModel model) {
        DurabilityRegistry.getInstance().register(
                model.getItemType(),
                model.getPluginContainer(),
                Collections.singleton(model.getModelName()),
                MODEL_DIRECTORY_NAME
        );
    }

    @Override
    public void generateResourcePackFiles(Path resourcePackDirectory) {
        Path guiDirectory = resourcePackDirectory.resolve("assets")
                .resolve(CustomItemLibrary.getInstance().getPluginContainer().getId())
                .resolve("models").resolve(MODEL_DIRECTORY_NAME);

        if(!Files.isDirectory(guiDirectory)) {
            try {
                Files.createDirectories(guiDirectory);
            } catch (IOException e) {
                CustomItemLibrary.getInstance().getLogger().error("Could not create the blocks directory.", e);
            }
        }

        definitions.stream()
                .flatMap(SimpleCustomInventoryDefinition::getSlotStream)
                .flatMap(slot -> slot.getFeatures().values().stream())
                .map(GUIFeature::getModel)
                .distinct()
                .forEach(guiModel -> {
                    Path modelFile = guiDirectory.resolve(guiModel.getModelName() + ".json");
                    JsonObject root = new JsonObject();

                    try(Writer writer = Files.newBufferedWriter(modelFile)) {
                        getGson().toJson(root, writer);
                    } catch (IOException e) {
                        CustomItemLibrary.getInstance().getLogger().error("Could not create model file: " + modelFile, e);
                    }
                });

        generateEmptyTexture(resourcePackDirectory);
    }

    private void generateEmptyTexture(Path resourcePackDirectory) {
        Path guiDirectory = resourcePackDirectory.resolve("assets")
                .resolve(CustomItemLibrary.getInstance().getPluginContainer().getId())
                .resolve("textures").resolve(MODEL_DIRECTORY_NAME);
        Path texturePath = guiDirectory.resolve(GUIModel.EMPTY.getTextureName() + ".png");

        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

        try {
            Files.createDirectories(guiDirectory);
            Files.deleteIfExists(texturePath);
            Files.createFile(texturePath);
            ImageIO.write(image, "PNG", texturePath.toFile());
        } catch (IOException e) {
            CustomItemLibrary.getInstance().getLogger().error("Could not create the empty texture.", e);
        }
    }

    public static SimpleCustomInventoryRegistry getInstance() {
        return INSTANCE;
    }
}
