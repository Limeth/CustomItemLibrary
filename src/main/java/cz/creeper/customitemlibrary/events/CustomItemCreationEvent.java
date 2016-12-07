package cz.creeper.customitemlibrary.events;

import cz.creeper.customitemlibrary.item.CustomItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;

@AllArgsConstructor
@Getter
public class CustomItemCreationEvent implements Event {
    @NonNull
    private final Cause cause;

    @NonNull
    private final CustomItem item;
}
