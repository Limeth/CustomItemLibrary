package cz.creeper.customitemlibrary.item.block;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public abstract class AbstractCustomBlockModelDefinition implements CustomBlockModelDefinition {
    @NonNull
    private final String name;
}
