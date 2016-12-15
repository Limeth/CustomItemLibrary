package cz.creeper.customitemlibrary.feature.item.tool;

import com.google.common.collect.Sets;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.CustomItemServiceImpl;
import cz.creeper.customitemlibrary.feature.CustomFeatureRegistry;
import cz.creeper.customitemlibrary.feature.DurabilityRegistry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.nio.file.Path;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class CustomToolRegistry implements CustomFeatureRegistry<CustomTool, CustomToolDefinition> {
    public static final String FILE_NAME = "toolRegistry.conf";
    public static final String NODE_MODEL_IDS = "modelIds";
    private static final CustomToolRegistry INSTANCE = new CustomToolRegistry();
    private final Set<CustomToolDefinition> definitions = Sets.newHashSet();

    @Override
    public void register(CustomToolDefinition definition) {
        DurabilityRegistry.getInstance().register(definition.getItemStackSnapshot().getType(), definition);
    }

    @Override
    public void prepare() {
        // Empty
    }

    @Override
    public void generateResourcePack(Path directory) {
        CustomItemLibrary.getInstance().getService().getDefinitions().stream()
                .filter(CustomToolDefinition.class::isInstance)
                .map(CustomToolDefinition.class::cast)
                .forEach(definition ->
                    definition.getAssets().forEach(asset ->
                        CustomItemServiceImpl.copyAsset(definition.getPluginContainer(), asset))
                );
    }

    public static CustomToolRegistry getInstance() {
        return INSTANCE;
    }
}
