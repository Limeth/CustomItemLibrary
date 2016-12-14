package cz.creeper.customitemlibrary.feature;

/**
 * Access to the current model of the instance.
 */
public interface HasModels {
    /**
     * Fixes the model and returns it.
     *
     * @return The model
     */
    String getModel();

    /**
     * @param model The model to use
     * @throws IllegalArgumentException when an undefined model is provided
     */
    void setModel(String model);
}
