package cz.creeper.customitemlibrary.feature.inventory.simple;

import static cz.creeper.customitemlibrary.feature.inventory.simple.SimpleCustomInventoryDefinition.*;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector4d;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import cz.creeper.customitemlibrary.feature.TextureId;
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
    private final List<GUIFeatures> lowerPrioritySlots = Lists.newArrayList();

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
                highPrioritySlots[y][x] = CustomSlot.unusedSlot(Vector2i.from(x, y));
            }
        }

        return this;
    }

    public SimpleCustomInventoryDefinitionBuilder background(@NonNull String slotId, @NonNull GUIBackground defaultBackground, GUIBackground... additionalBackgrounds) {
        Preconditions.checkNotNull(pluginContainer, "The plugin must already be set.");
        Preconditions.checkNotNull(highPrioritySlots, "The height must already be set.");

        if(additionalBackgrounds == null)
            additionalBackgrounds = new GUIBackground[0];

        GUIFeature defaultFeature = getBackgroundFeature(defaultBackground);
        List<GUIFeature> additionalFeatureList = Arrays.stream(additionalBackgrounds)
                .map(this::getBackgroundFeature)
                .collect(Collectors.toList());
        GUIFeature[] additionalFeatures = additionalFeatureList.toArray(new GUIFeature[additionalFeatureList.size()]);

        return feature(slotId, defaultFeature, additionalFeatures);
    }

    public SimpleCustomInventoryDefinitionBuilder background(@NonNull String slotId, @NonNull GUIBackground[] backgrounds) {
        GUIBackground defaultBackground = backgrounds[0];
        GUIBackground[] additionalBackground = new GUIBackground[backgrounds.length - 1];

        System.arraycopy(backgrounds, 1, additionalBackground, 0, additionalBackground.length);

        return background(slotId, defaultBackground, additionalBackground);
    }

    private GUIFeature getBackgroundFeature(GUIBackground background) {
        TextureId id = background.getTextureId();

        return GUIFeature.builder()
                .id(id.toString())
                .model(GUIModel.builder()
                        .plugin(pluginContainer.getInstance()
                                .orElseThrow(() -> new IllegalStateException("Could not access the plugin instance.")))
                        .textureId(id)
                        .textureSize(Vector2d.from(getInventoryTextureWidth(), getInventoryTextureHeight(highPrioritySlots.length)))
                        .textureOffset(Vector3d.from(0, 0, -100))
                        .uvRegion(Vector4d.from(
                                background.getUvTopLeft().getX(),
                                background.getUvTopLeft().getY(),
                                background.getUvTopLeft().getX() + getInventoryTextureWidth(),
                                background.getUvTopLeft().getY() + getInventoryTextureHeight(highPrioritySlots.length)
                        ))
                        .build())
                .build();
    }

    public SimpleCustomInventoryDefinitionBuilder feature(@NonNull String slotId, @NonNull GUIFeature defaultFeature, GUIFeature... additionalFeatures) {
        if(pluginContainer == null) {
            pluginContainer = defaultFeature.getModel().getPluginContainer();
        } else {
            Preconditions.checkArgument(defaultFeature.getModel().getPluginContainer() == pluginContainer,
                    "The plugin in the GUIModel is different from the plugin in this builder.");
        }

        if(additionalFeatures != null) {
            for(GUIFeature additionalFeature : additionalFeatures) {
                Preconditions.checkArgument(additionalFeature.getModel().getPluginContainer() == pluginContainer,
                        "The plugin in the GUIModel is different from the plugin in this builder.");
            }
        }

        lowerPrioritySlots.add(new GUIFeatures(
                slotId,
                defaultFeature,
                additionalFeatures != null ? ImmutableList.copyOf(additionalFeatures) : ImmutableList.of()
        ));

        return this;
    }

    public SimpleCustomInventoryDefinitionBuilder feature(@NonNull String slotId, @NonNull GUIFeature[] features) {
        GUIFeature defaultFeature = features[0];
        GUIFeature[] additionalFeatures = new GUIFeature[features.length - 1];

        System.arraycopy(features, 1, additionalFeatures, 0, additionalFeatures.length);

        return feature(slotId, defaultFeature, additionalFeatures);
    }

    public SimpleCustomInventoryDefinitionBuilder slot(String slotId, Vector2i position, AffectCustomSlotListener affectCustomSlotListener, @NonNull GUIFeature defaultFeature, GUIFeature... additionalFeatures) {
        Preconditions.checkNotNull(highPrioritySlots, "The height must be set first.");

        if(pluginContainer == null) {
            pluginContainer = defaultFeature.getModel().getPluginContainer();
        } else {
            Preconditions.checkArgument(defaultFeature.getModel().getPluginContainer() == pluginContainer,
                    "The plugin in the GUIModel is different from the plugin in this builder.");
        }

        if(additionalFeatures != null) {
            for(GUIFeature additionalFeature : additionalFeatures) {
                Preconditions.checkArgument(additionalFeature.getModel().getPluginContainer() == pluginContainer,
                        "The plugin in the GUIModel is different from the plugin in this builder.");
            }
        }

        highPrioritySlots[position.getY()][position.getX()] =
                new CustomSlot(position, slotId, defaultFeature, Arrays.asList(additionalFeatures), affectCustomSlotListener);

        return this;
    }

    public SimpleCustomInventoryDefinitionBuilder slot(String slotId, Vector2i position, AffectCustomSlotListener affectCustomSlotListener, @NonNull GUIFeature[] features) {
        GUIFeature defaultFeature = features[0];
        GUIFeature[] additionalFeatures = new GUIFeature[features.length - 1];

        System.arraycopy(features, 1, additionalFeatures, 0, additionalFeatures.length);

        return slot(slotId, position, affectCustomSlotListener, defaultFeature, additionalFeatures);
    }

    public SimpleCustomInventoryDefinitionBuilder slot(String slotId, int x, int y, AffectCustomSlotListener affectCustomSlotListener, @NonNull GUIFeature defaultFeature, GUIFeature... additionalFeatures) {
        return slot(slotId, Vector2i.from(x, y), affectCustomSlotListener, defaultFeature, additionalFeatures);
    }

    public SimpleCustomInventoryDefinitionBuilder slot(String slotId, int x, int y, AffectCustomSlotListener affectCustomSlotListener, @NonNull GUIFeature[] features) {
        return slot(slotId, Vector2i.from(x, y), affectCustomSlotListener, features);
    }

    public SimpleCustomInventoryDefinition build() {
        CustomSlot[][] slots = new CustomSlot[highPrioritySlots.length][];
        List<GUIFeatures> currentLowPrioritySlots = Lists.newArrayList(lowerPrioritySlots);

        for(int y = 0; y < highPrioritySlots.length; y++) {
            slots[y] = new CustomSlot[highPrioritySlots[y].length];

            for(int x = 0; x < highPrioritySlots[y].length; x++) {
                CustomSlot slot = highPrioritySlots[y][x];

                if(slot.isUnused() && currentLowPrioritySlots.size() > 0) {
                    GUIFeatures features = currentLowPrioritySlots.remove(0);
                    Vector3d slotOffset = Vector3d.from(INVENTORY_TEXTURE_SLOT_SIZE + INVENTORY_TEXTURE_SLOT_GAP)
                            .mul(x, y, 0).mul(-1);
                    GUIFeature defaultFeature = features.defaultFeature.toBuilder()
                            .model(features.defaultFeature.getModel().toBuilder(pluginContainer.getInstance()
                                            .orElseThrow(() -> new IllegalStateException("Could not access the plugin instance.")))
                                    .textureOffset(features.defaultFeature.getModel().getTextureOffset().add(slotOffset).sub(INVENTORY_TEXTURE_PADDING_LEFT, INVENTORY_TEXTURE_PADDING_TOP, 0))
                                    .build())
                            .build();
                    Iterable<GUIFeature> additionalFeatures = features.additionalFeatures.stream()
                            .map(additionalFeature -> additionalFeature.toBuilder()
                                    .model(additionalFeature.getModel().toBuilder(pluginContainer.getInstance()
                                                    .orElseThrow(() -> new IllegalStateException("Could not access the plugin instance.")))
                                            .textureOffset(additionalFeature.getModel().getTextureOffset().add(slotOffset).sub(INVENTORY_TEXTURE_PADDING_LEFT, INVENTORY_TEXTURE_PADDING_TOP, 0))
                                            .build())
                                    .build())
                            .collect(Collectors.toList());

                    Vector2i position = Vector2i.from(x, y);
                    slot = new CustomSlot(position, features.slotId, defaultFeature, additionalFeatures, null);
                }

                slots[y][x] = slot;
            }
        }

        if(!currentLowPrioritySlots.isEmpty())
            throw new IllegalStateException("Inventory too small for this amount of custom slots.");

        return new SimpleCustomInventoryDefinition(pluginContainer, typeId, slots);
    }

    /**
     * Translates the {@link GUIModel} to the first, upper left slot
     */
    private GUIModel translateAbsolute(GUIModel model, int x, int y) {
        return model.toBuilder(pluginContainer.getInstance()
                        .orElseThrow(() -> new IllegalStateException("Could not access the plugin instance.")))
                .textureOffset(
                        model.getTextureOffset()
                        .sub(INVENTORY_TEXTURE_PADDING_LEFT, 0, INVENTORY_TEXTURE_PADDING_TOP)
                        .add((INVENTORY_TEXTURE_SLOT_SIZE + INVENTORY_TEXTURE_SLOT_GAP) * x, 0,
                                (INVENTORY_TEXTURE_SLOT_SIZE + INVENTORY_TEXTURE_SLOT_GAP) * y)
                )
                .build();
    }

    @Value
    private static class GUIFeatures {
        String slotId;
        GUIFeature defaultFeature;
        ImmutableList<GUIFeature> additionalFeatures;
    }
}
