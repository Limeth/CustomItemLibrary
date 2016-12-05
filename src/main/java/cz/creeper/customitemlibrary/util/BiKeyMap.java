package cz.creeper.customitemlibrary.util;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Set;

public interface BiKeyMap<K1, K2, V> {
    int size();
    boolean isEmpty();
    boolean containsKeyFirst(K1 key);
    boolean containsKeySecond(K2 key);
    boolean containsValue(V value);
    V getFirst(K1 key);
    V getSecond(K2 key);
    K1 getFirstKey(K2 secondKey);
    K2 getSecondKey(K1 firstKey);
    V put(K1 firstKey, K2 secondKey, V value);
    V removeFirst(K1 key);
    V removeSecond(K2 key);
    void clear();
    Pair<Set<K1>, Set<K2>> keySet();
    Collection<V> values();
    Set<Pair<Pair<K1, K2>, V>> entrySet();
}

