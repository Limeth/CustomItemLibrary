package cz.creeper.customitemlibrary.registry;

import cz.creeper.customitemlibrary.CustomItem;
import lombok.ToString;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;
import java.util.stream.Collectors;

@ToString
public class CustomItemServiceImpl implements CustomItemService {
    public static final String DIRECTORY_NAME = "registries";
    private final CustomItemRegistryMap registryMap = new CustomItemRegistryMap();

    public CustomItemServiceImpl() {
        registryMap.put(CustomToolDefinition.class, CustomToolRegistry.getInstance());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I extends CustomItem, T extends CustomItemDefinition<I>> void register(T definition) {
        Optional<CustomItemRegistry<I, T>> registry = registryMap.get(definition);

        if(registry.isPresent())
            registry.get().register(definition);
        else
            throw new IllegalArgumentException("Invalid definition type.");
    }

    @Override
    public Optional<CustomItem> getCustomItem(ItemStack itemStack) {
        return registryMap.values().stream().map(registry -> registry.wrapIfPossible(itemStack)).filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            if(list.size() == 0)
                                return Optional.empty();
                            else if(list.size() == 1)
                                return Optional.of(list.get(0));
                            else
                                throw new IllegalStateException("Found more than 1 applicable registries for the following ItemStack: " + itemStack);
                        }
                ));
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
