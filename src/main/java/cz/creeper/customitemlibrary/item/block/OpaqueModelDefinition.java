package cz.creeper.customitemlibrary.item.block;

import cz.creeper.customitemlibrary.util.Block;
import org.spongepowered.api.event.cause.Cause;

import java.util.Set;
import java.util.UUID;

/**
 * A model simulating the look of a regular Minecraft block,
 * made by 8 skulls with custom skins in each corner of a block.
 */
public class OpaqueModelDefinition extends AbstractCustomBlockModelDefinition {
    public OpaqueModelDefinition(String name) {
        super(name);
    }

    @Override
    public Set<UUID> buildAppearance(Block block, Cause cause) {
        return null;  // TODO
    }
}
