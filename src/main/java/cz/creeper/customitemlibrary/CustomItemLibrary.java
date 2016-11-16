package cz.creeper.customitemlibrary;

import com.google.inject.Inject;
import cz.creeper.customitemlibrary.data.CustomItemData;
import cz.creeper.customitemlibrary.data.CustomItemManipulatorBuilder;
import cz.creeper.customitemlibrary.data.ImmutableCustomItemData;
import cz.creeper.customitemlibrary.registry.CustomItemService;
import cz.creeper.customitemlibrary.registry.CustomItemServiceImpl;
import lombok.Getter;
import ninja.leaping.configurate.ConfigurationOptions;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.plugin.Plugin;

import java.nio.file.Path;

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
    private static CustomItemLibrary instance;
    @Getter @Inject
    private Logger logger;
    @Getter @Inject @DefaultConfig(sharedRoot = false)
    private Path configPath;
    @Getter
    private CustomItemService service;

    @Listener
    public void onGameConstruction(GameConstructionEvent event) {
        instance = this;
    }

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        Sponge.getDataManager().register(CustomItemData.class, ImmutableCustomItemData.class, new CustomItemManipulatorBuilder());
    }

    @Listener
    public void onGameInitialization(GameInitializationEvent event) {
        logger.info("Loading CustomItemLibrary...");

        service = new CustomItemServiceImpl();

        service.loadDictionary();
        Sponge.getServiceManager().setProvider(this, CustomItemService.class, service);

        logger.info("CustomItemLibrary loaded.");
    }

    @Listener
    public void onGamePostInitialization(GamePostInitializationEvent event) {
        // During this phase, plugins using this library should register their custom item definitions.
    }

    @Listener
    public void onGameLoadComplete(GameLoadCompleteEvent event) {
        logger.info("Saving CustomItemLibrary...");
        service.saveDictionary();
        logger.info("CustomItemLibrary saved.");
    }

    public static ConfigurationOptions getDefaultConfigurationOptions() {
        return ConfigurationOptions.defaults().setShouldCopyDefaults(true);
    }

    public static CustomItemLibrary getInstance() {
        return instance;
    }
}
