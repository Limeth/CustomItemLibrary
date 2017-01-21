package cz.creeper.customitemlibrary.feature.inventory.simple;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import cz.creeper.customitemlibrary.util.Util;
import lombok.NonNull;
import lombok.Value;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.function.Function;
import java.util.stream.Collectors;

@Value
public class CustomSlot {
    public static final CustomSlot UNUSED_SLOT = new CustomSlot();
    private String defaultFeatureId;
    private BiMap<String, GUIFeature> features;

    public CustomSlot(@NonNull GUIFeature defaultFeature, Iterable<GUIFeature> additionalFeatures) {
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

    private CustomSlot() {
        this(GUIFeature.EMPTY, null);
    }

    public GUIFeature getDefaultFeature() {
        return features.get(defaultFeatureId);
    }

    public String getDefaultFeatureId() {
        return defaultFeatureId;
    }

    public boolean isUnused() {
        return this == UNUSED_SLOT || (getDefaultFeature() == GUIFeature.EMPTY && features.size() == 1);
    }

    public ItemStack createDefaultItemStack() {
        return getDefaultFeature().createItemStack();
    }
}
