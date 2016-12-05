package cz.creeper.customitemlibrary.events;

import cz.creeper.customitemlibrary.item.CustomItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;

@AllArgsConstructor
public class CustomItemCreationEvent implements Event {
    @Getter @NonNull
    private final Cause cause;

    @Getter @NonNull
    private final CustomItem item;
}
