package cz.creeper.customitemlibrary.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@AllArgsConstructor
@Getter
public class Identifier {
    public static char ID_SEPARATOR = ':';
    private final String namespace;
    private final String value;

    public static Identifier parse(String string) {
        return new Identifier(getNamespaceFromIdString(string), getValueFromIdString(string));
    }

    @Override
    public String toString() {
        return toString(namespace, value);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == getClass() && Objects.toString(obj).equals(toString());
    }

    public static boolean isParseable(String id) {
        return id.indexOf(ID_SEPARATOR) != -1;
    }

    public static String toString(String namespace, String value) {
        return namespace + ID_SEPARATOR + value;
    }

    public static String getNamespaceFromIdString(String string) {
        return string.substring(0, string.indexOf(ID_SEPARATOR));
    }

    public static String getValueFromIdString(String string) {
        return string.substring(string.indexOf(ID_SEPARATOR) + 1);
    }
}
