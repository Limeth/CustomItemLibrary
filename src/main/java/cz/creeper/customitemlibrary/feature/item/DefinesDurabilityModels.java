package cz.creeper.customitemlibrary.feature.item;

import cz.creeper.customitemlibrary.feature.DefinesModels;

public interface DefinesDurabilityModels extends DefinesModels {
    /**
     * The "<modelDirectoryName>" part in {@code "assets/<pluginId>/models/<modelDirectoryName>/<model>.json"}
     *
     * @return The name of the directory where the models are stored.
     */
    String getModelDirectoryName();
}
