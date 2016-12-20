package cz.creeper.customitemlibrary.feature.block;

import com.flowpowered.math.vector.Vector3d;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.feature.CustomFeatureDefinition;
import cz.creeper.customitemlibrary.feature.item.CustomItem;
import cz.creeper.customitemlibrary.feature.item.DefinesDurabilityModels;
import cz.creeper.customitemlibrary.util.Block;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * An immutable definition of a custom block type.
 * Place these blocks using the {@link #placeBlock(Block, Cause)} method
 * and provide a {@link Cause} with a {@link Player}, if you want to set them as the owner.
 */
public interface CustomBlockDefinition<T extends CustomBlock<? extends CustomBlockDefinition<T>>> extends CustomFeatureDefinition<T>, DefinesDurabilityModels {
    String MODEL_DIRECTORY_NAME = "blocks";

    /**
     * TODO: Resolve and document the existence of this field in {@link CustomFeatureDefinition#simpleBlockBuilder()}.
     * @deprecated Experimental, maybe there will be a different way of selecting the sound.
     * @return The sound that is played when the block is placed.
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    SoundType getSoundPlace();

    /**
     * Apply additional customizations to the placed block and wrap it.
     *
     * @param block Where to place the block
     * @param armorStand The created armor stand
     * @param cause The {@link Cause} that was used as a parameter when calling {@link #placeBlock(Block, Cause)}
     * @return The wrapped block
     */
    T customizeBlock(Block block, ArmorStand armorStand, Cause cause);

    /**
     * Make sure to provide a {@link Cause} with a {@link Player}, when calling {@link #placeBlock(Block, Cause)}.
     *
     * @return {@code true}, if the model should be rotated towards the player when
     *         this {@link CustomBlock} is placed, {@code false} otherwise
     */
    boolean isRotateHorizontally();

    /**
     * Constructs a custom block and places it in the world.
     *
     * @param block The block location
     * @param cause The cause
     * @return The wrapped block
     */
    default T placeBlock(Block block, Cause cause) {
        Location<World> location = block.getLocation()
                .orElseThrow(() -> new IllegalStateException("Could not access the location of the provided block."));
        World world = location.getExtent();

        // Remove the previous custom block
        CustomItemLibrary.getInstance().getService().removeArmorStandsAt(block);

        location.setBlockType(CustomBlock.BLOCK_TYPE_CUSTOM, cause);

        Vector3d armorStandPosition = block.getPosition().toDouble().add(Vector3d.ONE.mul(0.5));
        ArmorStand armorStand = (ArmorStand) world.createEntity(EntityTypes.ARMOR_STAND, armorStandPosition);
        Vector3d rotation = Vector3d.ZERO;

        if(isRotateHorizontally()) {
            Optional<Player> player = cause.first(Player.class);

            if(player.isPresent()) {
                Vector3d headRotation = player.get().getHeadRotation();
                double angle = Math.floorMod(180 + (int) Math.floor(headRotation.getY()), 360);
                angle += 45;
                angle = Math.floorMod((int) Math.floor(angle), 360);
                angle = (int) (angle / 90);
                angle = angle * 90;
                rotation = Vector3d.from(0, angle, 0);
            }
        }

        armorStand.offer(Keys.INVISIBLE, true);
        armorStand.offer(Keys.ARMOR_STAND_MARKER, true);
        armorStand.offer(Keys.HAS_GRAVITY, false);
        armorStand.offer(Keys.PERSISTS, true);
        armorStand.offer(createDefaultCustomFeatureData());
        armorStand.setRotation(rotation);
        armorStand.setHeadRotation(Vector3d.ZERO);

        world.spawnEntity(armorStand, cause);

        T result = customizeBlock(block, armorStand, cause);

        result.setModel(getDefaultModel());

        return result;
    }

    /**
     * Wraps the {@link Block} in a helper class extending {@link CustomItem},
     * if the {@link Block} is representing an actual custom block
     * created by the {@link CustomBlockDefinition#placeBlock(Block, Cause)} method.
     *
     * @param block The block to wrap
     * @return The wrapped {@link Block}, if the block actually is a custom block
     */
    Optional<T> wrapIfPossible(Block block);

    @Override
    default String getModelDirectoryName() {
        return MODEL_DIRECTORY_NAME;
    }
}
