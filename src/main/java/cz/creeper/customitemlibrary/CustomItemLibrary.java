package cz.creeper.customitemlibrary;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;

/**
 * In order to register custom items, use the {@link CustomItemService}
 */
@Plugin(
        id = "customitemlibrary",
        name = "CustomItemLibrary",
        description = "Create custom items with client-side resource packs and server-side behavior!",
        authors = {
                "Limeth"
        }
)
public class CustomItemLibrary {
    @Inject
    private Logger logger;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        logger.info("Loading CustomItemLibrary...");
        logger.info("CustomItemLibrary loaded.");
    }
}
