package cz.creeper.customitemlibrary.item.material;

import cz.creeper.customitemlibrary.item.AbstractCustomItem;
import cz.creeper.customitemlibrary.util.Util;
import cz.creeper.mineskinsponge.SkinRecord;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;
import java.util.stream.Collectors;

public class CustomMaterial extends AbstractCustomItem<CustomMaterial, CustomMaterialDefinition> {
    public CustomMaterial(ItemStack itemStack, CustomMaterialDefinition definition) {
        super(itemStack, definition);
    }

    public String getTexture() {
        CustomMaterialRegistry registry = CustomMaterialRegistry.getInstance();
        SkinRecord skinRecord = SkinRecord.of(getItemStack())
                .orElseThrow(() -> new IllegalStateException("Could not create a SkinRecord out of a"
                        + " CustomMaterial ItemStack."));
        Optional<String> textureId = registry.getTextureId(skinRecord);

        textureResolution:
        if(textureId.isPresent()) {
            String pluginId = Util.getNamespaceFromId(textureId.get());

            if (!getDefinition().getPluginId().equals(pluginId))
                break textureResolution;

            String texture = Util.getValueFromId(textureId.get());

            if (!getDefinition().getTextures().contains(texture))
                break textureResolution;

            return texture;
        }

        // If the texture is invalid, change the texture to the default one.
        String defaultTexture = getDefinition().getDefaultTexture();

        setTexture(defaultTexture);

        return defaultTexture;
    }

    public void setTexture(String texture) {
        if(!getDefinition().getTextures().contains(texture))
            throw new IllegalArgumentException("This custom material has no texture called '" + texture
                + "'. Available, defined textures: "
                + getDefinition().getTextures().stream().collect(Collectors.joining(", ")));

        CustomMaterialRegistry registry = CustomMaterialRegistry.getInstance();
        String textureId = Util.getId(getDefinition().getPluginId(), texture);
        SkinRecord skin = registry.getSkin(textureId).orElseThrow(() -> new IllegalArgumentException(""));

        skin.apply(getItemStack());
    }
}
