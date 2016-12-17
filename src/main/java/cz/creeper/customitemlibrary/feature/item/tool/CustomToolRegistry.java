package cz.creeper.customitemlibrary.feature.item.tool;

import com.google.common.collect.Sets;
import cz.creeper.customitemlibrary.feature.CustomFeatureRegistry;
import cz.creeper.customitemlibrary.feature.DurabilityRegistry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;

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

    public static CustomToolRegistry getInstance() {
        return INSTANCE;
    }
}
