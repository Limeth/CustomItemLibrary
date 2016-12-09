package cz.creeper.customitemlibrary.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Util {
    public char ID_SEPARATOR = ':';

    public String getId(String namespace, String value) {
        return namespace + ID_SEPARATOR + value;
    }

    public String getNamespaceFromId(String typeId) {
        return typeId.substring(0, typeId.indexOf(ID_SEPARATOR));
    }

    public String getValueFromId(String typeId) {
        return typeId.substring(typeId.indexOf(ID_SEPARATOR) + 1);
    }
}
