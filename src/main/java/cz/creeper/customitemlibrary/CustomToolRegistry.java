package cz.creeper.customitemlibrary;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;

public class CustomToolRegistry implements CustomItemRegistry<CustomTool, CustomToolDefinition> {
    private static final CustomToolRegistry INSTANCE = new CustomToolRegistry();
    private final Map<Integer, CustomToolDefinition> durabilityToDefinition = HashBiMap.create();
    private final BiMap<Integer, String> durabilityToTexture = HashBiMap.create();

    private CustomToolRegistry() {}

    @Override
    public void register(CustomToolDefinition definition) {
        for(String texture : definition.getTextures())
            if(!durabilityToTexture.containsValue(texture)) {
                int index = durabilityToTexture.size();

                durabilityToDefinition.put(index, definition);
                durabilityToTexture.put(index, texture);
            }
    }

    @Override
    public Optional<CustomTool> wrapIfPossible(ItemStack itemStack) {
        if(itemStack.getItem() != CustomToolDefinition.getItemType())
            return Optional.empty();

        int durability = itemStack.get(Keys.ITEM_DURABILITY)
                .orElseThrow(() -> new IllegalStateException("Odd, the shears don't have a durability value."));

        if(!durabilityToDefinition.containsKey(durability))
            return Optional.empty();

        return Optional.of(durabilityToDefinition.get(durability).createItem());
    }

    public Optional<Integer> getDurability(String texture) {
        return Optional.ofNullable(durabilityToTexture.inverse().get(texture));
    }

    public Optional<CustomToolDefinition> getDefinition(int durability) {
        return Optional.ofNullable(durabilityToDefinition.get(durability));
    }

    public Optional<String> getTexture(int durability) {
        return Optional.ofNullable(durabilityToTexture.get(durability));
    }

    public static CustomToolRegistry getInstance() {
        return INSTANCE;
    }
}
