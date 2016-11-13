package cz.creeper.customitemlibrary;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;

@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CustomItemRecord {
    @Getter
    private final CustomItemService service;
    @Getter
    private final String id;
    @Getter
    private final int durability;
    @Getter(lazy = true)
    private final String pluginId = initPluginId();
    @Getter(lazy = true)
    private final Optional<PluginContainer> pluginContainer = initPluginContainer();
    @Getter(lazy = true)
    private final String typeId = initTypeId();

    private String initPluginId() {
        return id.substring(0, id.indexOf(CustomItemServiceImpl.ID_SEPARATOR));
    }

    private Optional<PluginContainer> initPluginContainer() {
        return Sponge.getPluginManager().getPlugin(getPluginId());
    }

    private String initTypeId() {
        return id.substring(id.indexOf(CustomItemServiceImpl.ID_SEPARATOR) + 1);
    }
}
