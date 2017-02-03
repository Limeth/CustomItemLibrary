package cz.creeper.customitemlibrary.feature.inventory.simple;

import com.flowpowered.math.vector.Vector2i;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import cz.creeper.customitemlibrary.util.Util;
import lombok.NonNull;
import lombok.Value;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Value
public class CustomSlotDefinition {
    Vector2i position;
    String id;
    boolean persistent;
    AffectCustomSlotListener affectCustomSlotListener;
    String defaultFeatureId;
    BiMap<String, GUIFeature> features;

    private CustomSlotDefinition(@NonNull Vector2i position, String id, boolean persistent, AffectCustomSlotListener affectCustomSlotListener, GUIFeature defaultFeature, Iterable<GUIFeature> additionalFeatures) {
        Preconditions.checkArgument(id != null || !persistent, "The slot ID must be specified, if the slot is supposed to be persistent.");

        this.position = position;
        this.id = id;

        if(defaultFeature == null) {
            this.defaultFeatureId = null;
            this.features = ImmutableBiMap.of();
        } else {
            defaultFeatureId = defaultFeature.getId();
            features = ImmutableBiMap.<String, GUIFeature>builder()
                    .put(defaultFeature.getId(), defaultFeature)
                    .putAll(Util.removeNull(additionalFeatures)
                            .collect(Collectors.toMap(
                                    GUIFeature::getId,
                                    Function.identity(),
                                    (first, second) -> {
                                        throw new IllegalArgumentException("A CustomSlotDefinition cannot contain two GUIFeatures with the same ID.");
                                    }
                            )))
                    .build();
        }

        this.persistent = persistent;
        this.affectCustomSlotListener = affectCustomSlotListener != null ? affectCustomSlotListener
                : (customSlot, slotTransaction, affectSlotEvent) -> affectSlotEvent.setCancelled(true);
    }

    public static CustomSlotDefinition create(@NonNull Vector2i position, String id, boolean persistent, AffectCustomSlotListener affectCustomSlotListener, @NonNull GUIFeature defaultFeature, Iterable<GUIFeature> additionalFeatures) {
        return new CustomSlotDefinition(position, id, persistent, affectCustomSlotListener, defaultFeature, additionalFeatures);
    }

    public static CustomSlotDefinition createUnused(Vector2i position) {
        return new CustomSlotDefinition(position, null, false, null, GUIFeature.EMPTY, null);
    }

    public static CustomSlotDefinition createEmpty(Vector2i position, String id, boolean persistent, AffectCustomSlotListener affectCustomSlotListener) {
        return new CustomSlotDefinition(position, id, persistent, affectCustomSlotListener, null, null);
    }

    public Optional<String> getId() {
        return Optional.ofNullable(id);
    }

    public Optional<GUIFeature> getDefaultFeature() {
        return getDefaultFeatureId().flatMap(defaultFeatureId ->
                Optional.of(features.get(defaultFeatureId)));
    }

    public Optional<String> getDefaultFeatureId() {
        return Optional.ofNullable(defaultFeatureId);
    }

    public Optional<GUIFeature> getFeature(String featureId) {
        return Optional.ofNullable(features.get(featureId));
    }

    public boolean isUnused() {
        return getDefaultFeature().map(defaultFeature -> defaultFeature == GUIFeature.EMPTY).orElse(false) && features.size() == 1;
    }

    public boolean isEmpty() {
        return !getDefaultFeature().isPresent();
    }

    public ItemStack createDefaultItemStack() {
        return getDefaultFeature().map(GUIFeature::createItemStack).orElse(null);
    }
}
