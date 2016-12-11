package cz.creeper.customitemlibrary.util;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;

/**
 * Uniquely identifies a block across extents
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
public class Block {
    @NonNull
    private final UUID extentId;
    @NonNull
    private final Vector3i position;

    public static Block of(UUID extentId, Vector3i position) {
        Preconditions.checkArgument(Sponge.getServer().getChunkLayout().toChunk(position).isPresent(),
                "Invalid position, out of bounds.");
        return new Block(extentId, position);
    }

    public static Block of(UUID extentId, Vector3i chunkPosition, Vector3i positionInChunk) {
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

        return of(extentId, chunkPosition.mul(chunkSize).add(positionInChunk));
    }

    public static Block of(Location<World> location) {
        return of(location.getExtent().getUniqueId(), location.getBlockPosition());
    }

    public Optional<Location<World>> getLocation() {
        return getExtent().map(extent -> extent.getLocation(position));
    }

    public Optional<World> getExtent() {
        return Sponge.getServer().getWorld(extentId);
    }

    public Vector3i getChunk() {
        return Sponge.getServer().getChunkLayout().forceToChunk(position);
    }

    public Vector3i getPositionInChunk() {
        return position.sub(Sponge.getServer().getChunkLayout().getChunkSize().mul(getChunk()));
    }

    public Block add(Vector3i relative) {
        return of(extentId, position.add(relative));
    }
}
