package cz.creeper.customitemlibrary.feature;

import com.google.common.collect.Maps;
import lombok.ToString;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@ToString
public class CustomFeatureRegistryMap {
    private final Map<Class<?>, CustomFeatureRegistry<?, ?>> map = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    public <I extends CustomFeature<T>, T extends CustomFeatureDefinition<I>> Optional<CustomFeatureRegistry<I, T>> get(T def) {
        return get((Class<T>) def.getClass());
    }

    @SuppressWarnings("unchecked")
    public <I extends CustomFeature<T>, T extends CustomFeatureDefinition<I>> Optional<CustomFeatureRegistry<I, T>> get(Class<T> defClass) {
        return Optional.ofNullable((CustomFeatureRegistry<I, T>) map.get(defClass));
    }

    @SuppressWarnings("unchecked")
    public <I extends CustomFeature<T>, T extends CustomFeatureDefinition<I>> Optional<CustomFeatureRegistry<I, T>> put(Class<T> defClass, CustomFeatureRegistry<I, T> registry) {
        return Optional.ofNullable((CustomFeatureRegistry<I, T>) map.put(defClass, registry));
    }

    public Collection<CustomFeatureRegistry<?, ?>> values() {
        return map.values();
    }
}
