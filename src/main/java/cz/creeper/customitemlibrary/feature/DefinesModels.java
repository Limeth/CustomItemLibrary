package cz.creeper.customitemlibrary.feature;

import com.google.common.collect.ImmutableSet;

public interface DefinesModels {
    /**
     * @return The default model which is used by default
     */
    String getDefaultModel();

    /**
     * @return All available models
     */
    ImmutableSet<String> getModels();
}
