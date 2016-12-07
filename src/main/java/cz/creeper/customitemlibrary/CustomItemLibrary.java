package cz.creeper.customitemlibrary;

import com.google.inject.Inject;
import cz.creeper.customitemlibrary.data.CustomItemData;
import cz.creeper.customitemlibrary.data.CustomItemManipulatorBuilder;
import cz.creeper.customitemlibrary.data.ImmutableCustomItemData;
import cz.creeper.customitemlibrary.item.CustomItem;
import cz.creeper.customitemlibrary.item.CustomItemDefinition;
import lombok.Getter;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.nio.file.Path;
import java.util.function.Function;

/**
 * In order to register custom items, use the {@link CustomItemService}
 */
@Plugin(
        id = "customitemlibrary",
        name = "CustomItemLibrary",
        description = "Create custom items with client-side resource packs and server-side behavior!",
        authors = {
                "Limeth"
        },
        dependencies = {
                @Dependency(
                        id = "mineskinsponge",
                        version = "[1.2.1,)"
                )
        }
)
public class CustomItemLibrary {
    private static CustomItemLibrary instance;
    @Getter @Inject
    private Logger logger;
    @Getter @Inject
    private GuiceObjectMapperFactory objectMapperFactory;
    @Getter @Inject @DefaultConfig(sharedRoot = false)
    private Path configPath;
    @Getter
    private CustomItemServiceImpl service;

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
        setupService();
        setupCommands();
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

    private void setupService() {
        service = new CustomItemServiceImpl();

        service.loadDictionary();
        Sponge.getServiceManager().setProvider(this, CustomItemService.class, service);
    }

    private void setupCommands() {
        CommandManager manager = Sponge.getCommandManager();
        CommandSpec give = CommandSpec.builder()
                .description(Text.of("Gives the player a custom item."))
                .permission("customitemlibrary.command.customitemlibrary.give")
                .arguments(
                        GenericArguments.player(Text.of("Target")),
                        GenericArguments.choices(Text.of("Item"), () -> service.getDefinitionMap().keySet(),
                                Function.identity(), true),
                        GenericArguments.optional(GenericArguments.integer(Text.of("Quantity")))
                )
                .executor((CommandSource src, CommandContext args) -> {
                    Object rawTarget = args.getOne("Target").orElse(src);

                    if(!(rawTarget instanceof Player)) {
                        src.sendMessage(Text.builder()
                                .color(TextColors.RED)
                                .append(Text.of("Only players may run this command."))
                                .build());
                        return CommandResult.empty();
                    }

                    Player target = (Player) rawTarget;
                    String customItemId = args.<String>getOne("Item")
                            .orElseThrow(() -> new IllegalStateException("The item should have been specified."));

                    if(!service.getDefinitionMap().containsKey(customItemId)) {
                        src.sendMessage(Text.builder()
                                .color(TextColors.RED)
                                .append(Text.of("Invalid item id: " + customItemId))
                                .build());
                        return CommandResult.empty();
                    }

                    int quantity = args.<Integer>getOne("Quantity").orElse(1);

                    if(quantity <= 0) {
                        src.sendMessage(Text.builder()
                                .color(TextColors.RED)
                                .append(Text.of("Invalid item quantity: " + quantity))
                                .build());
                        return CommandResult.empty();
                    }

                    CustomItemDefinition definition = service.getDefinitionMap().get(customItemId);
                    CustomItem item = definition.createItem(Cause.builder()
                            .named(NamedCause.source(src))
                            .build());

                    target.getInventory().offer(item.getItemStack());
                    src.sendMessage(Text.builder()
                            .color(TextColors.GREEN)
                            .append(Text.of("Gave " + target.getName() + " " + quantity + "x " + customItemId))
                            .build());

                    return CommandResult.affectedItems(quantity);
                })
                .build();

        CommandSpec resourcepack = CommandSpec.builder()
                .description(Text.of("Creates the resourcepack."))
                .permission("customitemlibrary.command.customitemlibrary.resourcepack")
                .executor((CommandSource src, CommandContext args) -> {
                    service.generateResourcePack();
                    return CommandResult.success();
                })
                .build();

        CommandSpec customItemLibrary = CommandSpec.builder()
                .description(Text.of("CustomItemLibrary commands."))
                .permission("customitemlibrary.command.customitemlibrary")
                .child(give, "give", "g")
                .child(resourcepack, "resourcepack", "rp", "r")
                .build();

        manager.register(this, customItemLibrary, "customitemlibrary", "cil");
    }

    public ConfigurationOptions getDefaultConfigurationOptions() {
        return ConfigurationOptions.defaults().setShouldCopyDefaults(true)
                .setObjectMapperFactory(objectMapperFactory);
    }

    public static CustomItemLibrary getInstance() {
        return instance;
    }
}
