package cz.creeper.customitemlibrary.event;

import cz.creeper.customitemlibrary.feature.item.CustomItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

@ToString
@AllArgsConstructor
@Getter
public class CustomItemCreationEvent extends AbstractEvent {
    @NonNull
    private final CustomItem item;

    @NonNull
    private final Cause cause;
}
