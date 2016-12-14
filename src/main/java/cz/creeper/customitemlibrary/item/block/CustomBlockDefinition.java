package cz.creeper.customitemlibrary.item.block;

import cz.creeper.customitemlibrary.item.DefinesModels;

import java.util.Map;
import java.util.Set;

/**
 * Apply to models:

 "display": {
   "head": {
   "translation": [0, -43.225, 0],
   "scale": [1.6, 1.6, 1.6]
   }
 }
 */
public interface CustomBlockDefinition extends DefinesModels {
    Map<String, CustomBlockModelDefinition> getModelDefinitions();

    @Override
    default Set<String> getModels() {
        return getModelDefinitions().keySet();
    }
}
