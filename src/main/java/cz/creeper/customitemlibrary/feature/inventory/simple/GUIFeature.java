package cz.creeper.customitemlibrary.feature.inventory.simple;

import cz.creeper.customitemlibrary.feature.DurabilityRegistry;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.function.Consumer;

@Value
public class GUIFeature {
    public static final String ID_EMPTY = "empty";
    public static final GUIFeature EMPTY = GUIFeature.builder()
            .id(ID_EMPTY).model(GUIModel.EMPTY).build();
    String id;
    GUIModel model;
    Consumer<ItemStack> customizeItemStack;

    @Builder(toBuilder = true)
    private GUIFeature(@NonNull String id, @NonNull GUIModel model, Consumer<ItemStack> customizeItemStack) {
        this.id = id;
        this.model = model;
        this.customizeItemStack = customizeItemStack != null ? customizeItemStack
                : descriptionlessConsumer();
    }

    public ItemStack createItemStack() {
        return adjustItemStack(model, customizeItemStack);
    }

    public static ItemStack adjustItemStack(GUIModel model, Consumer<ItemStack> consumer) {
        ItemType itemType = model.getItemType();
        ItemStack itemStack = DurabilityRegistry.getInstance()
                .createItemUnsafe(itemType, model.getPluginContainer(), model.getModelName());

        consumer.accept(itemStack);

        return itemStack;
    }

    public static Consumer<ItemStack> descriptionlessConsumer() {
        return itemStack -> {
            itemStack.offer(Keys.DISPLAY_NAME, Text.EMPTY);
            itemStack.offer(Keys.HIDE_ATTRIBUTES, true);
            itemStack.offer(Keys.HIDE_UNBREAKABLE, true);
        };
    }
}
