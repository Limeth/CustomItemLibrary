package cz.creeper.customitemlibrary.feature;

import cz.creeper.customitemlibrary.util.Identifier;
import lombok.Getter;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;

@Getter
public class AssetId extends Identifier {
    private final PluginContainer pluginContainer;

    public AssetId(PluginContainer pluginContainer, String path) {
        super(pluginContainer.getId(), path);

        this.pluginContainer = pluginContainer;
    }

    public Optional<Asset> getAsset() {
        return pluginContainer.getAsset(getValue());
    }
}
