package cz.creeper.customitemlibrary.feature.block;

import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;
import java.util.Set;

public class SimpleCustomBlockDefinition implements CustomBlockDefinition<SimpleCustomBlock> {
    @Override
    public String getDefaultModel() {
        return null;
    }

    @Override
    public Set<String> getModels() {
        return null;
    }

    @Override
    public PluginContainer getPluginContainer() {
        return null;
    }

    @Override
    public String getTypeId() {
        return null;
    }

    @Override
    public SimpleCustomBlock createItem(Cause cause) {
        return null;
    }

    @Override
    public Optional<SimpleCustomBlock> wrapIfPossible(ItemStack itemStack) {
        return null;
    }
}
