package cz.creeper.customitemlibrary.util;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;

/**
 * Uniquely identifies a block across extents
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(exclude = "world")
public class Block {
    @NonNull
    private final UUID worldId;
    @NonNull
    private final Vector3i position;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<World> world;

    public static Block of(UUID worldId, Vector3i position) {
        Preconditions.checkArgument(Sponge.getServer().getChunkLayout().toChunk(position).isPresent(),
                "Invalid position, out of bounds.");
        return new Block(worldId, position);
    }

    public static Block of(World world, Vector3i position) {
        Block block = of(world.getUniqueId(), position);
        block.world = Optional.of(world);

        return block;
    }

    public static Block of(UUID worldId, Vector3i chunkPosition, Vector3i positionInChunk) {
        Vector3i chunkSize = Sponge.getServer().getChunkLayout().getChunkSize();
        Preconditions.checkArgument(
                positionInChunk.getX() >= 0
                        && positionInChunk.getY() >= 0
                        && positionInChunk.getZ() >= 0
                        && positionInChunk.getX() < chunkSize.getX()
                        && positionInChunk.getY() < chunkSize.getY()
                        && positionInChunk.getZ() < chunkSize.getZ(),
                "Position in chunk out of bounds."
        );

        return of(worldId, chunkPosition.mul(chunkSize).add(positionInChunk));
    }

    public static Block of(World world, Vector3i chunkPosition, Vector3i positionInChunk) {
        Block block = of(world.getUniqueId(), chunkPosition, positionInChunk);
        block.world = Optional.of(world);

        return block;
    }

    public static Block of(Location<World> location) {
        return of(location.getExtent(), location.getBlockPosition());
    }

    public Optional<Location<World>> getLocation() {
        return getWorld().map(extent -> extent.getLocation(position));
    }

    public Optional<World> initWorld() {
        return world = Sponge.getServer().getWorld(worldId);
    }

    public Optional<World> getWorld() {
        return world != null ? world : initWorld();
    }

    public Optional<Chunk> getChunk() {
        return getWorld().flatMap(extent -> extent.getChunk(getChunkPosition()));
    }

    public Vector3i getChunkPosition() {
        return Sponge.getServer().getChunkLayout().forceToChunk(position);
    }

    public Vector3i getPositionInChunk() {
        return position.sub(Sponge.getServer().getChunkLayout().getChunkSize().mul(getChunkPosition()));
    }

    public Block add(Vector3i relative) {
        return of(worldId, position.add(relative));
    }
}
