package cz.creeper.customitemlibrary.event;

import cz.creeper.customitemlibrary.util.Util;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.world.World;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CustomBlockBreakItemDropEvent extends AbstractEvent implements DropItemEvent.Destruct {
    private final List<Entity> entities;

    private final World targetWorld;

    private final Cause cause;

    @Setter
    private boolean cancelled;

    public CustomBlockBreakItemDropEvent(@NonNull List<Item> entities, @NonNull World targetWorld, @NonNull Cause cause) {
        this.entities = Util.removeNull(entities).collect(Collectors.toList());
        this.targetWorld = targetWorld;
        this.cause = cause;
    }

    @Nonnull
    @Override
    public List<EntitySnapshot> getEntitySnapshots() throws IllegalStateException {
        return getEntities().stream().map(Entity::createSnapshot).collect(Collectors.toList());
    }

    public List<Item> getItems() {
        return getEntities().stream().map(Item.class::cast).collect(Collectors.toList());
    }
}
