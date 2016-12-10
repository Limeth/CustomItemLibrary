package cz.creeper.customitemlibrary.item.material;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.item.CustomItemRegistry;
import cz.creeper.mineskinsponge.MineskinService;
import cz.creeper.mineskinsponge.SkinRecord;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.asset.AssetManager;
import org.spongepowered.api.plugin.PluginContainer;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomMaterialRegistry implements CustomItemRegistry<CustomMaterial, CustomMaterialDefinition> {
    private static final CustomMaterialRegistry INSTANCE = new CustomMaterialRegistry();
    private final Map<String, Map<String, CompletableFuture<SkinRecord>>> pluginIdsToTexturesToSkins = Maps.newHashMap();
    private final Map<String, BiMap<String, SkinRecord>> pluginIdsToTexturesToReadySkins = Maps.newHashMap();

    @Override
    public void register(CustomMaterialDefinition definition) {
        PluginContainer pluginContainer = definition.getPluginContainer();
        val texturesToSkins = getTexturesToSkins(pluginContainer);

        definition.getModels().stream()
                .filter(texture -> !texturesToSkins.containsKey(texture))
                .forEach(texture -> {
                    MineskinService mineskinService = getMineskinService();
                    Asset asset = getAsset(pluginContainer, texture);
                    CompletableFuture<SkinRecord> future = mineskinService.getSkinAsync(asset);
                    texturesToSkins.put(texture, future);
                });
    }

    public static Path getCacheDirectory() {
        return CustomItemLibrary.getInstance().getConfigPath().getParent().resolve("cache");
    }

    public Optional<SkinRecord> getSkin(PluginContainer pluginContainer, String texture) {
        return Optional.ofNullable(getTexturesToReadySkins(pluginContainer).get(texture));
    }

    public Optional<String> getTexture(PluginContainer pluginContainer, SkinRecord skin) {
        return Optional.ofNullable(getTexturesToReadySkins(pluginContainer).inverse().get(skin));
    }

    private Asset getAsset(PluginContainer pluginContainer, String texture) {
        Object plugin = pluginContainer.getInstance()
                .orElseThrow(() -> new IllegalStateException("Could not access the plugin instance of plugin: " + pluginContainer.getId()));
        AssetManager assetManager = Sponge.getAssetManager();
        String assetPath = CustomMaterialDefinition.getTextureAsset(texture);

        return assetManager.getAsset(plugin, assetPath)
                .orElseThrow(() -> new IllegalArgumentException("Could not locate asset '" + assetPath + "' of plugin '" + pluginContainer.getId() + "'."));
    }

    @Override
    public void load(Path directory) {
        // Clear previously loaded
        pluginIdsToTexturesToSkins.clear();
        pluginIdsToTexturesToReadySkins.clear();
    }

    @Override
    public void save(Path directory) {
        // Wait for all the textures to download
        CustomItemLibrary.getInstance().getLogger()
                .info("Waiting for skins to finish being downloaded.");
        pluginIdsToTexturesToReadySkins.clear();
        pluginIdsToTexturesToSkins.entrySet().forEach(texturesToSkins ->
            texturesToSkins.getValue().entrySet().forEach(entry ->
                getTexturesToReadySkins(texturesToSkins.getKey())
                        .put(entry.getKey(), entry.getValue().join())
            )
        );
        CustomItemLibrary.getInstance().getLogger()
                .info("Skins ready.");
    }

    @Override
    public void generateResourcePack(Path directory) {
        // Not needed
    }

    private Map<String, CompletableFuture<SkinRecord>> getTexturesToSkins(PluginContainer pluginContainer) {
        return pluginIdsToTexturesToSkins.computeIfAbsent(pluginContainer.getId(), k -> Maps.newHashMap());
    }

    private BiMap<String, SkinRecord> getTexturesToReadySkins(PluginContainer pluginContainer) {
        return getTexturesToReadySkins(pluginContainer.getId());
    }

    private BiMap<String, SkinRecord> getTexturesToReadySkins(String pluginId) {
        return pluginIdsToTexturesToReadySkins.computeIfAbsent(pluginId, k -> HashBiMap.create());
    }

    private MineskinService getMineskinService() {
        return Sponge.getServiceManager().provide(MineskinService.class)
                .orElseThrow(() -> new IllegalStateException("Could not access the MineskinService."));
    }

    public static CustomMaterialRegistry getInstance() {
        return INSTANCE;
    }
}
