package cz.creeper.customitemlibrary.feature.inventory.simple;

import static cz.creeper.customitemlibrary.feature.inventory.simple.SimpleCustomInventoryDefinition.*;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector2i;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.NonNull;
import lombok.Value;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleCustomInventoryDefinitionBuilder {
    private PluginContainer pluginContainer;
    private String typeId;
    private CustomSlot[][] highPrioritySlots;
    private final List<LowPriorityGUIFeature> lowerPrioritySlots = Lists.newArrayList();

    public SimpleCustomInventoryDefinitionBuilder plugin(Object plugin) {
        this.pluginContainer = Sponge.getPluginManager().fromInstance(plugin)
                .orElseThrow(() -> new IllegalArgumentException("Could not access the plugin container."));

        return this;
    }

    public SimpleCustomInventoryDefinitionBuilder typeId(String typeId) {
        this.typeId = typeId;

        return this;
    }

    public SimpleCustomInventoryDefinitionBuilder height(int height) {
        Preconditions.checkArgument(height > 0, "The height must be positive.");

        highPrioritySlots = new CustomSlot[height][INVENTORY_SLOTS_WIDTH];

        for(int y = 0; y < highPrioritySlots.length; y++) {
            highPrioritySlots[y] = new CustomSlot[INVENTORY_SLOTS_WIDTH];

            for(int x = 0; x < highPrioritySlots[y].length; x++) {
                highPrioritySlots[y][x] = CustomSlot.UNUSED_SLOT;
            }
        }

        return this;
    }

    public SimpleCustomInventoryDefinitionBuilder background(@NonNull Object plugin, @NonNull String featureId, @NonNull String defaultTexture, String... additionalTextures) {
        if(additionalTextures == null)
            additionalTextures = new String[0];

        GUIModel defaultModel = getBackgroundModel(plugin, defaultTexture);
        List<GUIModel> additionalModelList = Arrays.stream(additionalTextures)
                .map(additionalTexture -> getBackgroundModel(plugin, additionalTexture))
                .collect(Collectors.toList());
        GUIModel[] additionalModels = additionalModelList.toArray(new GUIModel[additionalModelList.size()]);

        return model(featureId, defaultModel, additionalModels);
    }

    private static GUIModel getBackgroundModel(Object plugin, String texture) {
        return GUIModel.builder()
                .plugin(plugin)
                .textureName(texture)
                .textureSize(Vector2d.from(176, 132))
                .build();
    }

    public SimpleCustomInventoryDefinitionBuilder model(@NonNull String id, @NonNull GUIModel defaultModel, GUIModel... additionalModels) {

        lowerPrioritySlots.add(new LowPriorityGUIFeature(
                id,
                defaultModel,
                ImmutableList.copyOf(additionalModels)
        ));

        return this;
    }

    public SimpleCustomInventoryDefinitionBuilder slot(int x, int y, @NonNull GUIFeature defaultFeature, GUIFeature... additionalFeatures) {
        Preconditions.checkNotNull(highPrioritySlots, "The height must be set first.");

        highPrioritySlots[y][x] = new CustomSlot(defaultFeature, Arrays.asList(additionalFeatures));

        return this;
    }

    public SimpleCustomInventoryDefinitionBuilder slot(Vector2i location, @NonNull GUIFeature defaultFeature, GUIFeature... additionalFeatures) {
        return slot(location.getX(), location.getY(), defaultFeature, additionalFeatures);
    }

    public SimpleCustomInventoryDefinition build() {
        CustomSlot[][] slots = new CustomSlot[highPrioritySlots.length][];
        List<LowPriorityGUIFeature> currentLowPrioritySlots = Lists.newArrayList(lowerPrioritySlots);

        for(int y = 0; y < highPrioritySlots.length; y++) {
            slots[y] = new CustomSlot[highPrioritySlots[y].length];

            for(int x = 0; x < highPrioritySlots[y].length; x++) {
                CustomSlot slot = highPrioritySlots[y][x];

                if(slot.isUnused() && currentLowPrioritySlots.size() > 0) {
                    LowPriorityGUIFeature feature = currentLowPrioritySlots.remove(0);
                    GUIFeature defaultFeature = GUIFeature.builder()
                            .model(feature.defaultModel)
                            .id(feature.defaultModel.getTextureName())
                            .build();
                    Iterable<GUIFeature> additionalFeatures = feature.additionalModels.stream()
                            .map(additionalModel -> GUIFeature.builder()
                                    .model(additionalModel)
                                    .id(additionalModel.getTextureName())
                                    .build())
                            .collect(Collectors.toList());

                    slot = new CustomSlot(defaultFeature, additionalFeatures);
                }

                slots[y][x] = slot;
            }
        }

        if(!currentLowPrioritySlots.isEmpty())
            throw new IllegalStateException("Inventory too small for this amount of custom slots.");

        return new SimpleCustomInventoryDefinition(pluginContainer, typeId, slots);
    }

    private static GUIModel translateAbsolute(GUIModel model, int x, int y) {
        return model.toBuilder()
                .textureOffset(
                        model.getTextureOffset()
                        .sub(INVENTORY_TEXTURE_PADDING_LEFT, 0, INVENTORY_TEXTURE_PADDING_TOP)
                        .add((INVENTORY_TEXTURE_SLOT_SIZE + INVENTORY_TEXTURE_SLOT_GAP) * x, 0,
                                (INVENTORY_TEXTURE_SLOT_SIZE + INVENTORY_TEXTURE_SLOT_GAP) * y)
                )
                .build();
    }

    @Value
    private static class LowPriorityGUIFeature {
        String id;
        GUIModel defaultModel;
        ImmutableList<GUIModel> additionalModels;
    }
}
