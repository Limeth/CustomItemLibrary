package cz.creeper.customitemlibrary.registry;

import com.google.common.collect.Maps;
import cz.creeper.customitemlibrary.CustomItem;
import lombok.ToString;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ToString
public class CustomItemServiceImpl implements CustomItemService {
    public static final String DIRECTORY_NAME = "registries";
    private final CustomItemRegistryMap registryMap = new CustomItemRegistryMap();
    private final HashMap<String, CustomItemDefinition> definitionMap = Maps.newHashMap();

    public CustomItemServiceImpl() {
        registryMap.put(CustomToolDefinition.class, CustomToolRegistry.getInstance());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I extends CustomItem, T extends CustomItemDefinition<I>> void register(T definition) {
        Optional<CustomItemRegistry<I, T>> registry = registryMap.get(definition);

        if(!registry.isPresent())
            throw new IllegalArgumentException("Invalid definition type.");

        String id = definition.getId();

        if(definitionMap.containsKey(id))
            throw new IllegalStateException("A custom item definition with ID \"" + id + "\" is already registered!");

        registry.get().register(definition);
        definitionMap.put(id, definition);
    }

    @Override
    public Map<String, CustomItemDefinition> getDefinitionMap() {
        return Collections.unmodifiableMap(definitionMap);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<CustomItem> getCustomItem(ItemStack itemStack) {
        return getDefinition(itemStack).flatMap(definition -> definition.wrapIfPossible(itemStack));
    }

    @Override
    public void loadDictionary() {
        registryMap.values().forEach(CustomItemRegistry::load);
    }

    @Override
    public void saveDictionary() {
        registryMap.values().forEach(CustomItemRegistry::save);
    }
}
