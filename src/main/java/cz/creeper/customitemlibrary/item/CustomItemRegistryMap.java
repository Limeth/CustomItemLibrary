package cz.creeper.customitemlibrary.item;

import com.google.common.collect.Maps;
import lombok.ToString;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@ToString
public class CustomItemRegistryMap {
    private final Map<Class<?>, CustomItemRegistry<?, ?>> map = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    public <I extends CustomItem, T extends CustomItemDefinition<I>> Optional<CustomItemRegistry<I, T>> get(T def) {
        return get((Class<T>) def.getClass());
    }

    @SuppressWarnings("unchecked")
    public <I extends CustomItem, T extends CustomItemDefinition<I>> Optional<CustomItemRegistry<I, T>> get(Class<T> defClass) {
        return Optional.ofNullable((CustomItemRegistry<I, T>) map.get(defClass));
    }

    @SuppressWarnings("unchecked")
    public <I extends CustomItem, T extends CustomItemDefinition<I>> Optional<CustomItemRegistry<I, T>> put(Class<T> defClass, CustomItemRegistry<I, T> registry) {
        return Optional.ofNullable((CustomItemRegistry<I, T>) map.put(defClass, registry));
    }

    public Collection<CustomItemRegistry<?, ?>> values() {
        return map.values();
    }
}
