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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
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

                BufferedImage inputImage = ImageIO.read(asset.getUrl());
                int width = inputImage.getWidth();
                int height = inputImage.getHeight();
                BufferedImage outputImage;

                if(width == 64 && height == 16) {
                    outputImage = new BufferedImage(64, 64, inputImage.getType());
                    Graphics2D outputGraphics = outputImage.createGraphics();

                    outputGraphics.setComposite(AlphaComposite.Src);
                    outputGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    outputGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    outputGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                    outputGraphics.drawImage(inputImage, 0, 0, 64, 16, new Color(0, 0, 0, 0), null); // TODO not really transparent
                    outputGraphics.dispose();
                } else if(width == 64 && height == 64) {
                    outputImage = inputImage;
                } else {
                    throw new IllegalArgumentException("The texture must either be a skin (64x64),"
                            + " png file) or just the upper part of the skin, head only (64x16, png file).");
                }

                ImageIO.write(outputImage, "png", texturePath.toFile());
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
