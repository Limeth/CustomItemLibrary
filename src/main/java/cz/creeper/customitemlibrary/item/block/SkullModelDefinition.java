package cz.creeper.customitemlibrary.item.block;

import com.flowpowered.math.imaginary.Quaterniond;
import com.flowpowered.math.vector.Vector3d;
import cz.creeper.customitemlibrary.util.Block;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
import org.spongepowered.api.event.cause.Cause;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * A block model made by several retextured skulls, which can be freely positioned and rotated.
 */
public class SkullModelDefinition extends AbstractCustomBlockModelDefinition {
    public SkullModelDefinition(String name, Collection<Skull> skulls) {
        super(name);
        Quaterniond q;
    }

    @Override
    public Set<UUID> buildAppearance(Block block, Cause cause) {
        return null;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Wither
    private static class Skull {
        private Vector3d position;
        private Vector3d axesAnglesDeg;
        private String textureName;
    }
}
