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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.asset.AssetManager;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomMaterialRegistry implements CustomItemRegistry<CustomMaterial, CustomMaterialDefinition> {
    private static final CustomMaterialRegistry INSTANCE = new CustomMaterialRegistry();
    private final Map<String, CompletableFuture<SkinRecord>> textureIdsToSkins = Maps.newHashMap();
    private final BiMap<String, SkinRecord> textureIdsToReadySkins = HashBiMap.create();
    private final Executor asyncExecutor = Sponge.getScheduler().createAsyncExecutor(CustomItemLibrary.getInstance());

    @Override
    public void register(CustomMaterialDefinition definition) {
        MineskinService service = getMineskinService();

        definition.getTextureIds().stream()
                .filter(textureId -> !textureIdsToSkins.containsKey(textureId))
                .forEach(textureId -> {
                    CompletableFuture<SkinRecord> future = copyTexture(textureId).thenCompose(service::getSkinAsync);
                    textureIdsToSkins.put(textureId, future);
                });
    }

    private CompletableFuture<Path> copyTexture(String textureId) {
        return CompletableFuture.supplyAsync(() -> {
            AssetManager assetManager = Sponge.getAssetManager();
            PluginContainer owningPlugin = CustomMaterialDefinition.getPlugin(textureId);
            String texture = CustomMaterialDefinition.getTexture(textureId);
            String textureAsset = CustomMaterialDefinition.getTextureAsset(texture);
            Asset asset = assetManager.getAsset(owningPlugin.getInstance().get(), textureAsset)
                    .orElseThrow(() -> new IllegalStateException("Could not access asset '" + textureAsset + "' of plugin '" + owningPlugin.getId() + "'. Is it missing?"));
            Path texturePathRelative = CustomMaterialDefinition.getTexturePath(owningPlugin, texture);
            Path texturePath = getCacheDirectory().resolve(texturePathRelative);

            try {
                Files.createDirectories(texturePath.getParent());
                Files.deleteIfExists(texturePath);

                // TODO handle texture conversion to skin format
                asset.copyToFile(texturePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return texturePath;
        }, asyncExecutor);
    }

    public Path getCacheDirectory() {
        return CustomItemLibrary.getInstance().getConfigPath().getParent().resolve("cache");
    }

    public SkinRecord getSkin(String textureId) {
        return textureIdsToReadySkins.get(textureId);
    }

    public String getTextureId(SkinRecord skin) {
        return textureIdsToReadySkins.inverse().get(skin);
    }

    @Override
    public void load(Path directory) {
        // Not needed
    }

    @Override
    public void save(Path directory) {
        // Wait for all the textures to download
        textureIdsToSkins.entrySet().forEach(entry ->
                textureIdsToReadySkins.put(entry.getKey(), entry.getValue().join()));
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
