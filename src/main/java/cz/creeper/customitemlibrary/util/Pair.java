package cz.creeper.customitemlibrary.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Pair<A, B> {
    @Getter private final A key;
    @Getter private final B value;
}
