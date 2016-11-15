package cz.creeper.customitemlibrary;

import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;
import java.util.stream.Collectors;

public class CustomItemServiceImpl implements CustomItemService {
    private final CustomItemRegistryMap registryMap = new CustomItemRegistryMap();

    public CustomItemServiceImpl() {
        registryMap.put(CustomToolDefinition.class, new CustomToolRegistry());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I extends CustomItem, T extends CustomItemDefinition<I>> boolean register(T definition) {
        Optional<Boolean> result = registryMap.get(definition).map(registry -> registry.register(definition));

        if(result.isPresent())
            return result.get();
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

    }

    @Override
    public void saveDictionary() {

    }
}
