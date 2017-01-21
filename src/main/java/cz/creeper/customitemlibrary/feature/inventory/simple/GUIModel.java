package cz.creeper.customitemlibrary.feature.inventory.simple;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Preconditions;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.util.Util;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;

import java.nio.ByteBuffer;

@Value
public class GUIModel {
    public static final ItemType DEFAULT_ITEM_TYPE = ItemTypes.DIAMOND_LEGGINGS;
    public static final GUIModel EMPTY = GUIModel.builder()
            .plugin(CustomItemLibrary.getInstance()).textureName("empty").build();

    PluginContainer pluginContainer;
    String textureName;
    ItemType itemType;
    Vector3d textureOffset;
    Vector2d textureSize;
    @Getter(lazy = true)
    String modelName = initModelName();

    @Builder
    private GUIModel(@NonNull Object plugin, @NonNull String textureName, ItemType itemType, Vector3d textureOffset, Vector2d textureSize) {
        Preconditions.checkArgument(itemType == null || ItemStack.builder().itemType(itemType).build().get(Keys.ITEM_DURABILITY).isPresent(),
                "The specified type of itemStackSnapshot doesn't have a durability.");

        this.pluginContainer = Sponge.getPluginManager().fromInstance(plugin)
                .orElseThrow(() -> new IllegalArgumentException("Invalid plugin instance."));
        this.textureName = textureName;
        this.itemType = itemType != null ? itemType : DEFAULT_ITEM_TYPE;
        this.textureOffset = textureOffset != null ? textureOffset : Vector3d.ZERO;
        this.textureSize = textureSize != null ? textureSize : Vector2d.from(16);
    }

    private String initModelName() {
        return textureName + '_' + Util.md5(ByteBuffer.allocate(4).putInt(hashCode()).array());
    }

    public GUIModelBuilder toBuilder() {
        return builder()
                .plugin(pluginContainer.getInstance())
                .textureName(textureName)
                .itemType(itemType)
                .textureOffset(textureOffset)
                .textureSize(textureSize);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GUIModel guiModel = (GUIModel) o;

        if (!pluginContainer.getId().equals(guiModel.pluginContainer.getId())) return false;
        if (!textureName.equals(guiModel.textureName)) return false;
        if (!itemType.getName().equals(guiModel.itemType.getName())) return false;
        if (!textureOffset.equals(guiModel.textureOffset)) return false;
        return textureSize.equals(guiModel.textureSize);
    }

    @Override
    public int hashCode() {
        int result = pluginContainer.getId().hashCode();
        result = 31 * result + textureName.hashCode();
        result = 31 * result + itemType.getName().hashCode();
        result = 31 * result + textureOffset.hashCode();
        result = 31 * result + textureSize.hashCode();
        return result;
    }
}
