package cz.creeper.customitemlibrary.feature.inventory.simple;

import static cz.creeper.customitemlibrary.feature.inventory.simple.SimpleCustomInventoryDefinition.*;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector4d;
import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.feature.DurabilityRegistry;
import cz.creeper.customitemlibrary.feature.TextureId;
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

import java.util.Optional;

/**
 * Consists of data necessary to create a model file.
 */
@Value
public class GUIModel {
    public static final ItemType DEFAULT_ITEM_TYPE = ItemTypes.DIAMOND_LEGGINGS;
    public static final GUIModel EMPTY = GUIModel.builder()
            .plugin(CustomItemLibrary.getInstance())
            .textureId(TextureId.of(CustomItemLibrary.getInstance(), null, "empty"))
            .build();

    PluginContainer pluginContainer;
    TextureId textureId;
    ItemType itemType;
    Vector3d textureOffset;
    Vector2d textureSize;
    Vector4d uvRegion;
    @Getter(lazy = true)
    String modelName = initModelName();

    @Builder
    private GUIModel(@NonNull Object plugin, @NonNull TextureId textureId, ItemType itemType, Vector3d textureOffset, Vector2d textureSize, Vector4d uvRegion) {
        Preconditions.checkArgument(itemType == null || ItemStack.builder().itemType(itemType).build().get(Keys.ITEM_DURABILITY).isPresent(),
                "The specified type of itemStackSnapshot doesn't have a durability.");

        this.pluginContainer = Sponge.getPluginManager().fromInstance(plugin)
                .orElseThrow(() -> new IllegalArgumentException("Invalid plugin instance."));
        this.textureId = textureId.getDirectory().isPresent() ? textureId : textureId.toBuilder().directory(
                SimpleCustomInventoryRegistry.MODEL_DIRECTORY_NAME).build();
        this.itemType = itemType != null ? itemType : DEFAULT_ITEM_TYPE;
        this.textureOffset = textureOffset != null ? textureOffset : Vector3d.ZERO;
        this.textureSize = textureSize != null ? textureSize : Vector2d.from(16);
        this.uvRegion = uvRegion != null ? uvRegion : Vector4d.from(0, 0, this.textureSize.getX(), this.textureSize.getY());
    }

    private String initModelName() {
        return textureId.getFileName() + '_' + Integer.toHexString(hashCode());
    }

    public PluginContainer getPluginContainer() {
        return pluginContainer;
    }

    public String getModelFileIdentifier() {
        return DurabilityRegistry.getFileIdentifier(pluginContainer,
                textureId.getDirectory().orElseThrow(() -> new IllegalStateException("The texture directory should have already been set.")),
                getModelName());
    }

    public Optional<String> getTextureAssetPath() {
        if(textureId.isVanilla())
            return Optional.empty();

        return Optional.of(textureId.getPath());
    }

    public GUIModelBuilder toBuilder(@NonNull Object plugin) {
        return builder()
                .plugin(Sponge.getPluginManager().fromInstance(plugin)
                        .orElseThrow(() -> new IllegalStateException("Could not access the plugin.")))
                .textureId(textureId)
                .itemType(itemType)
                .textureOffset(textureOffset)
                .textureSize(textureSize)
                .uvRegion(uvRegion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GUIModel guiModel = (GUIModel) o;

        if (!pluginContainer.getId().equals(guiModel.pluginContainer.getId())) return false;
        if (!textureId.equals(guiModel.textureId)) return false;
        if (!itemType.getName().equals(guiModel.itemType.getName())) return false;
        if (!textureOffset.equals(guiModel.textureOffset)) return false;
        if (!uvRegion.equals(guiModel.uvRegion)) return false;
        return textureSize.equals(guiModel.textureSize);
    }

    @Override
    public int hashCode() {
        int result = pluginContainer.getId().hashCode();
        result = 31 * result + textureId.hashCode();
        result = 31 * result + itemType.getName().hashCode();
        result = 31 * result + textureOffset.hashCode();
        result = 31 * result + textureSize.hashCode();
        result = 31 * result + uvRegion.hashCode();
        return result;
    }

    public Vector2d getUvRegionSize() {
        return Vector2d.from(uvRegion.getZ() - uvRegion.getX(),
                uvRegion.getW() - uvRegion.getY());
    }

    /*
        Model generation ahead
     */

    public JsonObject createModelJson() {
        JsonObject root = new JsonObject();

        // Textures
        JsonObject textures = new JsonObject();
        String texture = textureId.toString();

        textures.add("0", new JsonPrimitive(texture));
        root.add("textures", textures);

        // Elements
        JsonArray elements = new JsonArray();
        JsonObject element = new JsonObject();
        JsonArray from = new JsonArray();

        from.add(new JsonPrimitive(-16));
        from.add(new JsonPrimitive(-16));
        from.add(new JsonPrimitive(32));
        element.add("from", from);

        JsonArray to = new JsonArray();

        to.add(new JsonPrimitive(32));
        to.add(new JsonPrimitive(32));
        to.add(new JsonPrimitive(32));
        element.add("to", to);

        JsonObject faces = new JsonObject();
        JsonObject south = new JsonObject();
        JsonArray uv = new JsonArray();
        Vector4d modelUv = getModelUV();

        uv.add(new JsonPrimitive(modelUv.getX()));
        uv.add(new JsonPrimitive(modelUv.getY()));
        uv.add(new JsonPrimitive(modelUv.getZ()));
        uv.add(new JsonPrimitive(modelUv.getW()));
        south.add("uv", uv);
        south.add("texture", new JsonPrimitive("#0"));
        faces.add("south", south);
        element.add("faces", faces);
        elements.add(element);
        root.add("elements", elements);

        // Display
        JsonObject display = new JsonObject();
        JsonObject gui = new JsonObject();
        JsonArray translation = new JsonArray();
        Vector3d modelTranslation = getModelTranslation();

        translation.add(new JsonPrimitive(modelTranslation.getX()));
        translation.add(new JsonPrimitive(modelTranslation.getY()));
        translation.add(new JsonPrimitive(modelTranslation.getZ()));
        gui.add("translation", translation);

        JsonArray scale = new JsonArray();
        Vector2d modelScale = getModelScale();

        scale.add(new JsonPrimitive(modelScale.getX()));
        scale.add(new JsonPrimitive(modelScale.getY()));
        scale.add(new JsonPrimitive(1));
        gui.add("scale", scale);
        display.add("gui", gui);
        root.add("display", display);

        return root;
    }

    public Vector4d getModelUV() {
        return uvRegion.div(textureSize.getX(), textureSize.getY(), textureSize.getX(), textureSize.getY())
                .mul(INVENTORY_TEXTURE_SLOT_SIZE);
    }

    public Vector2d getModelScale() {
        return getModelScaleRatio()
                // Magic number
                .mul(3.6666666666666666666666);
    }

    public Vector2d getModelScaleRatio() {
        double largerDimension = textureSize.getX() > textureSize.getY() ? textureSize.getX() : textureSize.getY();
        double inventoryTextureWidth = getInventoryTextureWidth();

        return Vector2d.from(largerDimension)
                .div(inventoryTextureWidth)
                // Align UV
                .mul(getUvRegionSize())
                .div(textureSize);
    }

    public Vector3d getModelTranslation() {
        // The texture must have a 1;1 aspect ratio, so it is expanded
        double largerDimension = Math.max(textureSize.getX(), textureSize.getY());
        Vector3d result = Vector3d.ZERO;

        // First, align with the upper left corner of the inventory slot
        result = result.add(
                Vector3d.from(
                        largerDimension - INVENTORY_TEXTURE_SLOT_SIZE - uvRegion.getX(),
                        largerDimension - INVENTORY_TEXTURE_SLOT_SIZE - uvRegion.getY(),
                        0
                ).div(2)
        );

        // Then, move it to the upper left corner of the inventory texture
        result = result.sub(
                Vector3d.from(
                        INVENTORY_TEXTURE_PADDING_LEFT,
                        INVENTORY_TEXTURE_PADDING_TOP,
                        0
                )
        );

        result = result.add(this.textureOffset);

        // Finally, flip the Y coordinate, because it's inverse in minecraft
        result = result.mul(1, -1, 1);

        return result;
    }

    public Vector3d getModelTranslationTODO() {
        // The texture must have a 1;1 aspect ratio, so it is expanded
        Vector2d uvRegionSize = getUvRegionSize();
        double largerDimension = Math.max(textureSize.getX(), textureSize.getY());
        Vector2d result2d = Vector2d.ZERO;

        // First, align with the upper left corner of the inventory slot
        //result2d = result2d.add(uvRegionSize);
        result2d = result2d.add(Vector2d.from(largerDimension));
        result2d = result2d.sub(uvRegion.getX(), uvRegion.getY());
        result2d = result2d.sub(Vector2d.from(INVENTORY_TEXTURE_SLOT_SIZE));
        result2d = result2d.div(2);

        // Then, move it to the upper left corner of the inventory texture
        result2d = result2d.sub(INVENTORY_TEXTURE_PADDING_LEFT, INVENTORY_TEXTURE_PADDING_TOP);

        // TODO
        //result2d = result2d.sub(Vector2d.from(Math.max(uvRegionSize.getX(), uvRegionSize.getY())).sub(uvRegionSize));

        // Apply custom offset
        Vector3d result3d = result2d.toVector3().add(this.textureOffset);

        // The translation is affected by scale, so we have to revert it
        //result3d = result3d.div(getModelScaleRatio().toVector3(1));

        // Finally, flip the Y coordinate, because it's inverse in minecraft
        result3d = result3d.mul(1, -1, 1);

        return result3d;
    }
}
