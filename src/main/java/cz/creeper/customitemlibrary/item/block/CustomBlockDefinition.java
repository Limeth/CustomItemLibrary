package cz.creeper.customitemlibrary.item.block;

import cz.creeper.customitemlibrary.item.DefinesModels;

import java.util.Map;
import java.util.Set;

public interface CustomBlockDefinition extends DefinesModels {
    Map<String, CustomBlockModelDefinition> getModelDefinitions();

    @Override
    default Set<String> getModels() {
        return getModelDefinitions().keySet();
    }
}
