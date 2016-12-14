package cz.creeper.customitemlibrary;

import com.google.inject.Inject;
import cz.creeper.customitemlibrary.data.CustomFeatureData;
import cz.creeper.customitemlibrary.data.CustomFeatureManipulatorBuilder;
import cz.creeper.customitemlibrary.data.ImmutableCustomFeatureData;
import cz.creeper.customitemlibrary.data.ImmutableRepresentedCustomItemSnapshotData;
import cz.creeper.customitemlibrary.data.RepresentedCustomItemSnapshotData;
import cz.creeper.customitemlibrary.data.RepresentedCustomItemSnapshotManipulatorBuilder;
import cz.creeper.customitemlibrary.feature.CustomFeature;
import cz.creeper.customitemlibrary.feature.CustomFeatureDefinition;
import cz.creeper.customitemlibrary.feature.item.CustomItem;
import cz.creeper.customitemlibrary.feature.item.CustomItemDefinition;
import cz.creeper.customitemlibrary.util.Identifier;
import cz.creeper.customitemlibrary.util.Util;
import lombok.Getter;
import lombok.val;
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
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameLoadCompleteEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
                        version = "[1.3.0,)"
                )
        }
)
@Getter
public class CustomItemLibrary {
    private static CustomItemLibrary instance;
    @Inject
    private Logger logger;
    @Inject
    private GuiceObjectMapperFactory objectMapperFactory;
    @Inject @DefaultConfig(sharedRoot = false)
    private Path configPath;
    private CustomItemServiceImpl service;

    @Listener
    public void onGameConstruction(GameConstructionEvent event) {
        instance = this;
    }

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        Sponge.getDataManager().register(CustomFeatureData.class, ImmutableCustomFeatureData.class, new CustomFeatureManipulatorBuilder());
        Sponge.getDataManager().register(RepresentedCustomItemSnapshotData.class, ImmutableRepresentedCustomItemSnapshotData.class, new RepresentedCustomItemSnapshotManipulatorBuilder());
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
        service.saveRegistry();
        logger.info("CustomItemLibrary saved.");

        service.finalize();
    }

    private void setupService() {
        service = new CustomItemServiceImpl();

        service.loadRegistry();
        Sponge.getServiceManager().setProvider(this, CustomItemService.class, service);
    }

    private void setupCommands() {
        CommandManager manager = Sponge.getCommandManager();
        CommandSpec give = CommandSpec.builder()
                .description(Text.of("Gives the player a custom item."))
                .permission("customitemlibrary.command.customitemlibrary.give")
                .arguments(
                        GenericArguments.player(Text.of("Target")),
                        GenericArguments.choices(Text.of("Item"), () -> service.getDefinitions().stream()
                                        .map(definition -> Identifier.toString(definition.getPluginContainer().getId(),
                                                definition.getTypeId())).collect(Collectors.toSet()),
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
                    Optional<CustomFeatureDefinition<? extends CustomFeature>> rawDefinition;

                    if(!Identifier.isParseable(customItemId)
                            || !(rawDefinition = service.getDefinition(Identifier.getNamespaceFromIdString(customItemId),
                                    Identifier.getValueFromIdString(customItemId))).isPresent()) {
                        src.sendMessage(Text.of(TextColors.RED, "Invalid item id: " + customItemId));
                        return CommandResult.empty();
                    }

                    if(!(rawDefinition.get() instanceof CustomItemDefinition)) {
                        src.sendMessage(Text.of(TextColors.RED, "The requested id is assigned to a"
                                + " feature which is not an item that could be given: " + customItemId));
                        return CommandResult.empty();
                    }

                    CustomItemDefinition<? extends CustomItem> definition = (CustomItemDefinition<? extends CustomItem>) rawDefinition.get();

                    int quantity = args.<Integer>getOne("Quantity").orElse(1);

                    if(quantity <= 0) {
                        src.sendMessage(Text.builder()
                                .color(TextColors.RED)
                                .append(Text.of("Invalid item quantity: " + quantity))
                                .build());
                        return CommandResult.empty();
                    }

                    CustomItem item = definition.createItem(Cause.builder()
                            .named(NamedCause.source(src))
                            .build());

                    val result = target.getInventory().offer(item.getItemStack());

                    result.getRejectedItems().forEach(rejectedSnapshot -> {
                        Location<World> location = target.getLocation();

                        Util.spawnItem(location, rejectedSnapshot, src);
                    });

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
                    src.sendMessage(Text.of(TextColors.GRAY, "Generating resourcepack..."));
                    Path path = service.generateResourcePack();
                    src.sendMessage(Text.of(TextColors.GRAY, "Resourcepack generated: " + path));
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

    public PluginContainer getPluginContainer() {
        return Sponge.getPluginManager().fromInstance(this)
                .orElseThrow(() -> new IllegalStateException("Could not access the plugin container of CustomItemLibrary."));
    }

    public static CustomItemLibrary getInstance() {
        return instance;
    }
}
