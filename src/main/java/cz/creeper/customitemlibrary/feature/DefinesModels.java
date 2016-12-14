package cz.creeper.customitemlibrary.feature;

import java.util.Set;

public interface DefinesModels {
    /**
     * @return The default model assigned to newly created {@link ItemStack}s in the {@link #createItem(Cause)} method.
     */
    String getDefaultModel();

    /**
     * @return All available models
     */
    Set<String> getModels();
}
