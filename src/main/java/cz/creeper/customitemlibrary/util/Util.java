package cz.creeper.customitemlibrary.util;

import cz.creeper.customitemlibrary.CustomItemLibrary;
import lombok.experimental.UtilityClass;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@UtilityClass
public class Util {
    public void spawnItem(Location<World> location, ItemStackSnapshot snapshot, Object notifier) {
        World world = location.getExtent();
        Item rejectedItem = (Item) world.createEntity(EntityTypes.ITEM, location.getPosition());

        Cause cause = Cause.source(
                EntitySpawnCause.builder()
                        .entity(rejectedItem)
                        .type(SpawnTypes.PLUGIN)
                        .build()
                )
                .owner(CustomItemLibrary.getInstance().getPluginContainer())
                .notifier(notifier)
                .build();

        rejectedItem.offer(Keys.REPRESENTED_ITEM, snapshot);
        world.spawnEntity(rejectedItem, cause);
    }

    public <T> Stream<T> removeNull(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), true)
                .filter(Objects::nonNull);
    }
}
