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
import lombok.Builder;
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
    private CustomSlotDefinition[][] highPrioritySlots;
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

        highPrioritySlots = new CustomSlotDefinition[height][INVENTORY_SLOTS_WIDTH];

        for(int y = 0; y < highPrioritySlots.length; y++) {
            highPrioritySlots[y] = new CustomSlotDefinition[INVENTORY_SLOTS_WIDTH];

            for(int x = 0; x < highPrioritySlots[y].length; x++) {
                highPrioritySlots[y][x] = CustomSlotDefinition.createUnused(Vector2i.from(x, y));
            }
        }

        return this;
    }

    @Builder(
            builderMethodName = "backgroundBuilder",
            builderClassName = "SimpleCustomInventoryDefinitionBuilderBackgroundBuilder"
    )
    public SimpleCustomInventoryDefinitionBuilder background(@NonNull String slotId, boolean persistent, @NonNull GUIBackground defaultBackground, GUIBackground... additionalBackgrounds) {
        Preconditions.checkNotNull(pluginContainer, "The plugin must already be set.");
        Preconditions.checkNotNull(highPrioritySlots, "The height must already be set.");

        if(additionalBackgrounds == null)
            additionalBackgrounds = new GUIBackground[0];

        GUIFeature defaultFeature = getBackgroundFeature(defaultBackground);
        List<GUIFeature> additionalFeatureList = Arrays.stream(additionalBackgrounds)
                .map(this::getBackgroundFeature)
                .collect(Collectors.toList());
        GUIFeature[] additionalFeatures = additionalFeatureList.toArray(new GUIFeature[additionalFeatureList.size()]);

        return feature(slotId, persistent, defaultFeature, additionalFeatures);
    }

    public SimpleCustomInventoryDefinitionBuilder background(@NonNull String slotId, boolean persistent, @NonNull GUIBackground[] backgrounds) {
        GUIBackground defaultBackground = backgrounds[0];
        GUIBackground[] additionalBackground = new GUIBackground[backgrounds.length - 1];

        System.arraycopy(backgrounds, 1, additionalBackground, 0, additionalBackground.length);

        return background(slotId, persistent, defaultBackground, additionalBackground);
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

    @Builder(
            builderMethodName = "featureBuilder",
            builderClassName = "SimpleCustomInventoryDefinitionBuilderFeatureBuilder"
    )
    public SimpleCustomInventoryDefinitionBuilder feature(@NonNull String slotId, boolean persistent, @NonNull GUIFeature defaultFeature, GUIFeature... additionalFeatures) {
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
                persistent,
                defaultFeature,
                additionalFeatures != null ? ImmutableList.copyOf(additionalFeatures) : ImmutableList.of()
        ));

        return this;
    }

    public SimpleCustomInventoryDefinitionBuilder feature(@NonNull String slotId, boolean persistent, @NonNull GUIFeature[] features) {
        GUIFeature defaultFeature = features[0];
        GUIFeature[] additionalFeatures = new GUIFeature[features.length - 1];

        System.arraycopy(features, 1, additionalFeatures, 0, additionalFeatures.length);

        return feature(slotId, persistent, defaultFeature, additionalFeatures);
    }

    @Builder(
            builderMethodName = "slotBuilder",
            builderClassName = "SimpleCustomInventoryDefinitionBuilderSlotBuilder"
    )
    public SimpleCustomInventoryDefinitionBuilder slot(String slotId, Vector2i position, boolean persistent, AffectCustomSlotListener affectCustomSlotListener, @NonNull GUIFeature defaultFeature, GUIFeature... additionalFeatures) {
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
                CustomSlotDefinition.create(position, slotId, persistent, affectCustomSlotListener, defaultFeature, Arrays.asList(additionalFeatures));

        return this;
    }

    public SimpleCustomInventoryDefinitionBuilder slot(String slotId, Vector2i position, boolean persistent, AffectCustomSlotListener affectCustomSlotListener, @NonNull GUIFeature[] features) {
        GUIFeature defaultFeature = features[0];
        GUIFeature[] additionalFeatures = new GUIFeature[features.length - 1];

        System.arraycopy(features, 1, additionalFeatures, 0, additionalFeatures.length);

        return slot(slotId, position, persistent, affectCustomSlotListener, defaultFeature, additionalFeatures);
    }

    public SimpleCustomInventoryDefinitionBuilder slot(String slotId, int x, int y, boolean persistent, AffectCustomSlotListener affectCustomSlotListener, @NonNull GUIFeature defaultFeature, GUIFeature... additionalFeatures) {
        return slot(slotId, Vector2i.from(x, y), persistent, affectCustomSlotListener, defaultFeature, additionalFeatures);
    }

    public SimpleCustomInventoryDefinitionBuilder slot(String slotId, int x, int y, boolean persistent, AffectCustomSlotListener affectCustomSlotListener, @NonNull GUIFeature[] features) {
        return slot(slotId, Vector2i.from(x, y), persistent, affectCustomSlotListener, features);
    }

    @Builder(
            builderMethodName = "emptySlotBuilder",
            builderClassName = "SimpleCustomInventoryDefinitionBuilderEmptySlotBuilder"
    )
    public SimpleCustomInventoryDefinitionBuilder emptySlot(String slotId, Vector2i position, boolean persistent, AffectCustomSlotListener affectCustomSlotListener) {
        highPrioritySlots[position.getY()][position.getX()] =
                CustomSlotDefinition.createEmpty(position, slotId, persistent, affectCustomSlotListener != null ? affectCustomSlotListener
                        : (customSlot, slotTransaction, affectSlotEvent) -> {});

        return this;
    }

    public SimpleCustomInventoryDefinitionBuilder emptySlot(String slotId, int x, int y, boolean persistent, AffectCustomSlotListener affectCustomSlotListener) {
        return emptySlot(slotId, Vector2i.from(x, y), persistent, affectCustomSlotListener);
    }

    public SimpleCustomInventoryDefinition build() {
        CustomSlotDefinition[][] slots = new CustomSlotDefinition[highPrioritySlots.length][];
        List<GUIFeatures> currentLowPrioritySlots = Lists.newArrayList(lowerPrioritySlots);

        for(int y = 0; y < highPrioritySlots.length; y++) {
            slots[y] = new CustomSlotDefinition[highPrioritySlots[y].length];

            for(int x = 0; x < highPrioritySlots[y].length; x++) {
                CustomSlotDefinition slot = highPrioritySlots[y][x];

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
                    slot = CustomSlotDefinition.create(position, features.slotId, features.persistent, null, defaultFeature, additionalFeatures);
                }

                slots[y][x] = slot;
            }
        }

        if(!currentLowPrioritySlots.isEmpty())
            throw new IllegalStateException("Inventory too small for this amount of custom slots.");

        return new SimpleCustomInventoryDefinition(pluginContainer, typeId, slots);
    }

    @Value
    private static class GUIFeatures {
        String slotId;
        boolean persistent;
        GUIFeature defaultFeature;
        ImmutableList<GUIFeature> additionalFeatures;
    }
}
