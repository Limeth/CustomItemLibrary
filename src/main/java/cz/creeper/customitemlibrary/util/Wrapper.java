package cz.creeper.customitemlibrary.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Wrapper<T> {
    private T value;

    public static <T> Wrapper<T> of(T value) {
        return new Wrapper<>(value);
    }
}
