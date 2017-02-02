package cz.creeper.customitemlibrary.feature;

import cz.creeper.customitemlibrary.util.Identifier;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;

@Getter
public class TextureId extends Identifier {
    private final PluginContainer pluginContainer;
    private final String directory;
    private final String fileName;

    private TextureId(PluginContainer pluginContainer, String directory, @NonNull String fileName) {
        super(pluginContainer != null ? pluginContainer.getId() : "minecraft",
                directory != null && !directory.isEmpty() ? (directory + '/' + fileName) : fileName);

        this.pluginContainer = pluginContainer;
        this.directory = directory;
        this.fileName = fileName;
    }

    @Builder
    public static TextureId of(Object plugin, String directory, @NonNull String fileName) {
        return new TextureId(plugin == null ? null : Sponge.getPluginManager().fromInstance(plugin)
                .orElseThrow(() -> new IllegalArgumentException("Could not access the plugin.")),
                directory, fileName);
    }

    public TextureId.TextureIdBuilder toBuilder() {
        return builder()
                .plugin(pluginContainer.getInstance().orElseThrow(() -> new IllegalStateException("Could not access the plugin instance.")))
                .directory(directory)
                .fileName(fileName);
    }

    public static TextureId parse(String textureId) {
        Identifier identifier = Identifier.parseOrDefaultNamespace(textureId, "minecraft");
        PluginContainer pluginContainer;

        if(identifier.getNamespace().equals("minecraft")) {
            pluginContainer = null;
        } else {
            pluginContainer = Sponge.getPluginManager().getPlugin(identifier.getNamespace())
                    .orElseThrow(() -> new IllegalArgumentException("Could not access plugin with id '"
                            + identifier.getNamespace() + "', is it loaded?"));
        }

        int lastFileNameSeparator = identifier.getValue().lastIndexOf('/');
        String directory;
        String fileName;

        if(lastFileNameSeparator == -1) {
            directory = null;
            fileName = identifier.getValue();
        } else {
            directory = identifier.getValue().substring(0, lastFileNameSeparator);
            fileName = identifier.getValue().substring(lastFileNameSeparator + 1);
        }

        return new TextureId(pluginContainer, directory, fileName);
    }

    public Optional<String> getDirectory() {
        return Optional.ofNullable(directory);
    }

    public String getPath() {
        return "textures/" + (directory != null && !directory.isEmpty() ? directory + '/'  : "") + fileName + ".png";
    }

    public boolean isVanilla() {
        return pluginContainer == null;
    }

    public Optional<PluginContainer> getPluginContainer() {
        return Optional.ofNullable(pluginContainer);
    }
}
