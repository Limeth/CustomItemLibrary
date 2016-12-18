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
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public interface CustomBlockDefinition<T extends CustomBlock<? extends CustomBlockDefinition<T>>> extends CustomFeatureDefinition<T>, DefinesDurabilityModels {
    String MODEL_DIRECTORY_NAME = "blocks";

    SoundType getSoundPlace();
    T customizeBlock(Block block, ArmorStand armorStand, Cause cause);

    default T placeBlock(Block block, Cause cause) {
        Location<World> location = block.getLocation()
                .orElseThrow(() -> new IllegalStateException("Could not access the location of the provided block."));
        World world = location.getExtent();

        // Remove the previous custom block
        CustomItemLibrary.getInstance().getService().removeArmorStandsAt(block);

        location.setBlockType(CustomBlock.BLOCK_TYPE_CUSTOM, cause);

        Vector3d armorStandPosition = block.getPosition().toDouble().add(Vector3d.ONE.mul(0.5));
        ArmorStand armorStand = (ArmorStand) world.createEntity(EntityTypes.ARMOR_STAND, armorStandPosition);

        armorStand.offer(Keys.INVISIBLE, true);
        armorStand.offer(Keys.ARMOR_STAND_MARKER, true);
        armorStand.offer(Keys.HAS_GRAVITY, false);
        armorStand.offer(Keys.PERSISTS, true);
        armorStand.offer(createDefaultCustomFeatureData());
        armorStand.setRotation(Vector3d.ZERO);
        armorStand.setHeadRotation(Vector3d.ZERO);

        world.spawnEntity(armorStand, cause);

        T result = customizeBlock(block, armorStand, cause);

        result.setModel(getDefaultModel());

        return result;
    }

    /**
     * Wraps the {@link Location} in a helper class extending {@link CustomItem},
     * if the {@link Location} is representing an actual custom feature
     * created by the {@link CustomBlockDefinition#placeBlock(Block, Cause)} method.
     *
     * @param block The block to wrap
     * @return The wrapped {@link Block}, if the feature actually represents this definition
     */
    Optional<T> wrapIfPossible(Block block);

    @Override
    default String getModelDirectoryName() {
        return MODEL_DIRECTORY_NAME;
    }
}
