package cz.creeper.customitemlibrary.feature.inventory.simple;

import com.flowpowered.math.vector.Vector2i;
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
public class CustomSlot {
    private Vector2i position;
    private String id;
    private String defaultFeatureId;
    private BiMap<String, GUIFeature> features;

    public CustomSlot(@NonNull Vector2i position, String id, @NonNull GUIFeature defaultFeature, Iterable<GUIFeature> additionalFeatures) {
        this.position = position;
        this.id = id;
        defaultFeatureId = defaultFeature.getId();
        features = ImmutableBiMap.<String, GUIFeature>builder()
                .put(defaultFeature.getId(), defaultFeature)
                .putAll(Util.removeNull(additionalFeatures)
                        .collect(Collectors.toMap(
                                GUIFeature::getId,
                                Function.identity(),
                                (first, second) -> {
                                    throw new IllegalArgumentException("A CustomSlot cannot contain two GUIFeatures with the same ID.");
                                }
                        )))
                .build();
    }

    private CustomSlot(Vector2i position) {
        this(position, null, GUIFeature.EMPTY, null);
    }

    public static CustomSlot unusedSlot(Vector2i position) {
        return new CustomSlot(position);
    }

    public Optional<String> getId() {
        return Optional.ofNullable(id);
    }

    public GUIFeature getDefaultFeature() {
        return features.get(defaultFeatureId);
    }

    public String getDefaultFeatureId() {
        return defaultFeatureId;
    }

    public Optional<GUIFeature> getFeature(String featureId) {
        return Optional.ofNullable(features.get(featureId));
    }

    public boolean isUnused() {
        return getDefaultFeature() == GUIFeature.EMPTY && features.size() == 1;
    }

    public ItemStack createDefaultItemStack() {
        return getDefaultFeature().createItemStack();
    }
}
