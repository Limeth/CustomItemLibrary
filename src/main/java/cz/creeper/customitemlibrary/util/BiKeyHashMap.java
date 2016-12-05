package cz.creeper.customitemlibrary.util;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class BiKeyHashMap<K1, K2, V> implements BiKeyMap<K1, K2, V> {
    private final HashMap<K1, Pair<K2, V>> firstMap = new HashMap<>();
    private final HashMap<K2, Pair<K1, V>> secondMap = new HashMap<>();

    public int size() {
        return firstMap.size();
    }

    public boolean isEmpty() {
        return firstMap.isEmpty();
    }

    public boolean containsKeyFirst(K1 key) {
        return firstMap.containsKey(key);
    }

    public boolean containsKeySecond(K2 key) {
        return secondMap.containsKey(key);
    }

    public boolean containsValue(V value) {
        return firstMap.containsValue(value);
    }

    public V getFirst(K1 key) {
        return firstMap.get(key).getValue();
    }

    public V getSecond(K2 key) {
        return secondMap.get(key).getValue();
    }

    public K1 getFirstKey(K2 secondKey) {
        return secondMap.get(secondKey).getKey();
    }

    public K2 getSecondKey(K1 firstKey) {
        return firstMap.get(firstKey).getKey();
    }

    public V put(K1 firstKey, K2 secondKey, V value) {
        V firstValue = firstMap.get(firstKey).getValue();

        Preconditions.checkArgument(firstValue == secondMap.get(secondKey).getValue(), "Key collision.");
        firstMap.put(firstKey, Pair.of(secondKey, value));
        secondMap.put(secondKey, Pair.of(firstKey, value));

        return firstValue;
    }

    public V removeFirst(K1 key) {
        Pair<K2, V> firstPair = firstMap.remove(key);

        secondMap.remove(firstPair.getKey());

        return firstPair.getValue();
    }

    public V removeSecond(K2 key) {
        Pair<K1, V> secondPair = secondMap.remove(key);

        firstMap.remove(secondPair.getKey());

        return secondPair.getValue();
    }

    public void clear() {
        firstMap.clear();
        secondMap.clear();
    }

    public Pair<Set<K1>, Set<K2>> keySet() {
        return Pair.of(firstMap.keySet(), secondMap.keySet());
    }

    public Collection<V> values() {
        return firstMap.values().stream().map(Pair::getRight).collect(Collectors.toSet());
    }

    public Set<Pair<Pair<K1, K2>, V>> entrySet() {
        return firstMap.entrySet().stream().map((entry) ->
                Pair.of(Pair.of(entry.getKey(), entry.getValue().getKey()), entry.getValue().getValue())
        ).collect(Collectors.toSet());
    }
}
