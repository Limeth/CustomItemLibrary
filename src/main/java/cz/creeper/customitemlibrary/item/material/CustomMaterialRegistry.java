package cz.creeper.customitemlibrary.item.material;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.item.CustomItemRegistry;
import cz.creeper.customitemlibrary.util.Util;
import cz.creeper.mineskinsponge.MineskinService;
import cz.creeper.mineskinsponge.SkinRecord;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.asset.AssetManager;
import org.spongepowered.api.plugin.PluginContainer;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomMaterialRegistry implements CustomItemRegistry<CustomMaterial, CustomMaterialDefinition> {
    private static final String URI_SCHEME = "cil-asset";
    private static final CustomMaterialRegistry INSTANCE = new CustomMaterialRegistry();
    private final Set<CustomMaterialDefinition> definitions = Sets.newHashSet();
    private final Map<String, CompletableFuture<SkinRecord>> textureIdsToSkins = Maps.newHashMap();
    private final BiMap<String, SkinRecord> textureIdsToReadySkins = HashBiMap.create();

    @Override
    public void register(CustomMaterialDefinition definition) {
        MineskinService service = getMineskinService();
        PluginContainer pluginContainer = definition.getPluginContainer();

        definition.getTextures().stream()
                .filter(texture -> {
                    String textureId = Util.getId(pluginContainer.getId(), texture);

                    return !textureIdsToSkins.containsKey(textureId);
                })
                .forEach(texture -> {
                    MineskinService mineskinService = getMineskinService();
                    String textureId = Util.getId(pluginContainer.getId(), texture);
                    Asset asset = getAsset(pluginContainer, texture);
                    CompletableFuture<SkinRecord> future = mineskinService.getSkinAsync(asset);
                    textureIdsToSkins.put(textureId, future);
                });

        definitions.add(definition);
    }

    public static Path getCacheDirectory() {
        return CustomItemLibrary.getInstance().getConfigPath().getParent().resolve("cache");
    }

    public Optional<SkinRecord> getSkin(String textureId) {
        return Optional.ofNullable(textureIdsToReadySkins.get(textureId));
    }

    public Optional<String> getTextureId(SkinRecord skin) {
        return Optional.ofNullable(textureIdsToReadySkins.inverse().get(skin));
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
        definitions.clear();
        textureIdsToSkins.clear();
        textureIdsToReadySkins.clear();
    }

    @Override
    public void save(Path directory) {
        // Wait for all the textures to download
        CustomItemLibrary.getInstance().getLogger()
                .info("Waiting for skins to finish being downloaded.");
        textureIdsToSkins.entrySet().forEach(entry ->
                textureIdsToReadySkins.put(entry.getKey(), entry.getValue().join()));
        CustomItemLibrary.getInstance().getLogger()
                .info("Skins ready.");
    }

    @Override
    public void generateResourcePack(Path directory) {
        // Not needed
    }

    private MineskinService getMineskinService() {
        return Sponge.getServiceManager().provide(MineskinService.class)
                .orElseThrow(() -> new IllegalStateException("Could not access the MineskinService."));
    }

    public static CustomMaterialRegistry getInstance() {
        return INSTANCE;
    }
}
