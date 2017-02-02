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
@Builder
@Value
public class GUIBackground {
    @NonNull
    TextureId textureId;

    @NonNull
    Vector2d textureSize;
}
