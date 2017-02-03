package cz.creeper.customitemlibrary.feature.inventory.simple;

import com.flowpowered.math.vector.Vector2d;
import cz.creeper.customitemlibrary.feature.TextureId;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;

@ToString
@EqualsAndHashCode
@Value
public class GUIBackground {
    TextureId textureId;
    Vector2d uvTopLeft;

    @Builder
    private GUIBackground(@NonNull TextureId textureId, Vector2d uvTopLeft) {
        this.textureId = textureId;
        this.uvTopLeft = uvTopLeft != null ? uvTopLeft : Vector2d.ZERO;
    }
}
