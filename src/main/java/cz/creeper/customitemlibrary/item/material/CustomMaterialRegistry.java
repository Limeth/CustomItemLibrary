package cz.creeper.customitemlibrary.item.material;

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.item.CustomItemRegistry;
import cz.creeper.mineskinsponge.MineskinService;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.AssetManager;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

public class CustomMaterialRegistry implements CustomItemRegistry<CustomMaterial, CustomMaterialDefinition> {
    private final CompletableFuture<Void> allTexturesDownloaded = CompletableFuture.completedFuture((Void) null);
    private final Map<String, CompletableFuture<Path>> textureIdsToPaths = Maps.newHashMap();
    private final Executor asyncExecutor = Sponge.getScheduler().createAsyncExecutor(CustomItemLibrary.getInstance());

    @Override
    public void register(CustomMaterialDefinition definition) {
        MineskinService service = getMineskinService();

        definition.getTextures().stream()
                .filter(texture -> !textures.contains(texture))
                .forEach(texture -> {
                    allTexturesDownloaded = allTexturesDownloaded.thenAcceptBoth(CompletableFuture.);
                    textures.add(texture);
                });
    }

    private void CompletableFuture<Path> copyTexture(String texture) {
        // TODO
    }

    private Path getTexturePath() {
        // TODO
    }

    private String getTextureAsset() {
        // TODO
    }

    @Override
    public void load(Path directory) {
        // Not needed
    }

    @Override
    public void save(Path directory) {
        // Wait for all the textures to download
        if(!allTexturesDownloaded.isDone() || allTexturesDownloaded.isCancelled() || allTexturesDownloaded.isCompletedExceptionally()) {
            CustomItemLibrary.getInstance().getLogger().info("Downloading custom skins for skulls, please wait.");
            allTexturesDownloaded.join();
        }
    }

    @Override
    public void generateResourcePack(Path directory) {

    }

    private MineskinService getMineskinService() {
        return Sponge.getServiceManager().provide(MineskinService.class)
                .orElseThrow(() -> new IllegalStateException("Could not access the MineskinService."));
    }
}
