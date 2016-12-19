package cz.creeper.customitemlibrary.feature.item.material;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.CustomItemService;
import cz.creeper.customitemlibrary.event.CustomBlockPlaceEvent;
import cz.creeper.customitemlibrary.feature.CustomFeatureRegistry;
import cz.creeper.customitemlibrary.feature.block.CustomBlock;
import cz.creeper.mineskinsponge.MineskinService;
import cz.creeper.mineskinsponge.SkinRecord;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.asset.AssetManager;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.block.ReplaceableProperty;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.effect.sound.SoundCategories;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomMaterialRegistry implements CustomFeatureRegistry<CustomMaterial, CustomMaterialDefinition> {
    private static final CustomMaterialRegistry INSTANCE = new CustomMaterialRegistry();
    private final Map<String, Map<String, CompletableFuture<SkinRecord>>> pluginIdsToTexturesToSkins = Maps.newHashMap();
    private final Map<String, BiMap<String, SkinRecord>> pluginIdsToTexturesToReadySkins = Maps.newHashMap();
    private final Map<UUID, Map<HandType, ItemStackSnapshot>> lastUsedItem = Maps.newHashMap();

    @Override
    public void register(CustomMaterialDefinition definition) {
        PluginContainer pluginContainer = definition.getPluginContainer();
        Map<String, CompletableFuture<SkinRecord>> texturesToSkins = getTexturesToSkins(pluginContainer);

        definition.getModels().stream()
                .filter(texture -> !texturesToSkins.containsKey(texture))
                .forEach(texture -> {
                    MineskinService mineskinService = getMineskinService();
                    Asset asset = getAsset(pluginContainer, texture);
                    CompletableFuture<SkinRecord> future = mineskinService.getSkinAsync(asset);
                    texturesToSkins.put(texture, future);
                });
    }

    public static Path getCacheDirectory() {
        return CustomItemLibrary.getInstance().getConfigPath().getParent().resolve("cache");
    }

    public Optional<SkinRecord> getSkin(PluginContainer pluginContainer, String texture) {
        return Optional.ofNullable(getTexturesToReadySkins(pluginContainer).get(texture));
    }

    public Optional<String> getTexture(PluginContainer pluginContainer, SkinRecord skin) {
        return Optional.ofNullable(getTexturesToReadySkins(pluginContainer).inverse().get(skin));
    }

    private Asset getAsset(PluginContainer pluginContainer, String texture) {
        Object plugin = pluginContainer.getInstance()
                .orElseThrow(() -> new IllegalStateException("Could not access the plugin instance of plugin: " + pluginContainer.getId()));
        AssetManager assetManager = Sponge.getAssetManager();
        String assetPath = CustomMaterialDefinition.getTextureAsset(texture);

        return assetManager.getAsset(plugin, assetPath)
                .orElseThrow(() -> new IllegalArgumentException("Could not locate asset '" + assetPath + "' of plugin '" + pluginContainer.getId() + "'."));
    }

    @Override
    public void prepare() {
        // Wait for all the textures to download
        CustomItemLibrary.getInstance().getLogger()
                .info("Waiting for skins to finish being downloaded.");
        pluginIdsToTexturesToReadySkins.clear();
        pluginIdsToTexturesToSkins.entrySet().forEach(texturesToSkins ->
            texturesToSkins.getValue().entrySet().forEach(entry ->
                getTexturesToReadySkins(texturesToSkins.getKey())
                        .put(entry.getKey(), entry.getValue().join())
            )
        );
        CustomItemLibrary.getInstance().getLogger()
                .info("Skins ready.");
    }

    @Listener
    public void onClientConnectionDisconnect(ClientConnectionEvent.Disconnect event) {
        lastUsedItem.remove(event.getTargetEntity().getUniqueId());
    }

    @Listener(order = Order.LATE)
    public void onInteractItemSecondary(InteractBlockEvent.Secondary event, @Root Player player) {
        CustomItemService service = CustomItemLibrary.getInstance().getService();

        player.getItemInHand(event.getHandType())
                .flatMap(service::getItem)
                .filter(CustomMaterial.class::isInstance)
                .map(CustomMaterial.class::cast)
                .ifPresent(customMaterial -> {
                    event.setCancelled(true);

                    if(event.getTargetBlock() == BlockSnapshot.NONE)
                        return;

                    CustomMaterialDefinition definition = customMaterial.getDefinition();
                    PlaceProvider placeProvider = definition.getPlaceProvider();
                    BlockSnapshot targetBlock = event.getTargetBlock();
                    Direction targetSide = event.getTargetSide();
                    Location<World> targetLocation = targetBlock.getLocation()
                            .orElseThrow(() -> new IllegalStateException("Could not access the location of the clicked block."));
                    BlockState targetState = targetLocation.getBlock();
                    boolean targetReplaceable = targetState.getType().getProperty(ReplaceableProperty.class).map(ReplaceableProperty::getValue).orElse(false);
                    Location<World> placeLocation;

                    if(targetReplaceable) {
                        placeLocation = targetLocation;
                    } else {
                        placeLocation = targetLocation.getRelative(targetSide);
                        BlockState placeState = placeLocation.getBlock();
                        boolean placeReplaceable = placeState.getType().getProperty(ReplaceableProperty.class).map(ReplaceableProperty::getValue).orElse(false);

                        if(!placeReplaceable)
                            return;
                    }

                    Cause placeCause = Cause.source(CustomItemLibrary.getInstance().getPluginContainer())
                            .notifier(player)
                            .addAll(event.getCause().getNamedCauses().entrySet().stream()
                                    .filter(entry -> !entry.getKey().equals(NamedCause.SOURCE)
                                                        && !entry.getKey().equals(NamedCause.NOTIFIER))
                                    .map(entry -> NamedCause.of(entry.getKey(), entry.getValue()))
                                    .collect(Collectors.toList()))
                            .build();

                    placeProvider.provideBlock(customMaterial, player, placeLocation, placeCause)
                            .ifPresent(blockSnapshot -> {
                                World world = placeLocation.getExtent();
                                BlockSnapshot original = world.createSnapshot(placeLocation.getBlockPosition());
                                Transaction<BlockSnapshot> transaction = new Transaction<>(original, blockSnapshot);
                                CustomBlockPlaceEvent placeEvent = new CustomBlockPlaceEvent(Collections.singletonList(transaction), world, placeCause, false);
                                boolean cancelled = Sponge.getEventManager().post(placeEvent);

                                if(!cancelled) {
                                    ItemStack itemInHand = customMaterial.getDataHolder();

                                    itemInHand.setQuantity(itemInHand.getQuantity() - 1);
                                    placeProvider.afterBlockPlace(customMaterial, player, placeLocation, placeCause);
                                    player.setItemInHand(event.getHandType(), itemInHand);

                                    Optional<? extends CustomBlock<?>> placedCustomBlock = CustomItemLibrary.getInstance().getService().getBlock(placeLocation);
                                    Vector3d soundPosition = placeLocation.getPosition().add(Vector3d.ONE.mul(0.5));
                                    BlockState placeState = placeLocation.getBlock();
                                    SoundType placeSound = placedCustomBlock.map(customBlock -> customBlock.getDefinition().getSoundPlace())
                                            .orElseGet(() -> placeState.getType().getSoundGroup().getPlaceSound());

                                    // TODO: plays some sort of dog breathing sound, get rid of that
                                    world.playSound(placeSound, SoundCategories.BLOCK, soundPosition, 1, 1);
                                }
                            });
                });
    }

    /*
    I would make it placeable, but there are bugs with the data api.

    @Listener(order = Order.BEFORE_POST)
    public void onInteractItemSecondary(InteractItemEvent.Secondary event, @Root Player player) {
        Optional<ItemStack> itemStack = player.getItemInHand(event.getHandType());

        if(itemStack.isPresent()) {
            val items = lastUsedItem.computeIfAbsent(player.getUniqueId(), k -> Maps.newHashMap());

            items.put(event.getHandType(), itemStack.get().createSnapshot());
        } else {
            Optional.ofNullable(lastUsedItem.get(player.getUniqueId()))
                    .ifPresent(items -> items.remove(event.getHandType()));
        }
    }

    @Listener(order = Order.BEFORE_POST)
    public void onChangeBlockPlace(ChangeBlockEvent.Place event, @Root Player player) {
        final Wrapper<List<CustomMaterial>> customMaterialsPlaced = Wrapper.of(Lists.newLinkedList());

        Optional.ofNullable(lastUsedItem.get(player.getUniqueId()))
                .ifPresent(items -> items.values().forEach(feature -> {
                    CustomItemLibrary.getInstance().getService().getCustomItem(feature.createStack())
                            .filter(CustomMaterial.class::isInstance)
                            .map(CustomMaterial.class::cast)
                            .ifPresent(customMaterial -> customMaterialsPlaced.getValue().add(customMaterial));
                }));

        if(customMaterialsPlaced.getValue().size() < 1)
            return;
        else if(customMaterialsPlaced.getValue().size() > 1) {
            event.setCancelled(true);
            player.sendMessage(Text.of(TextColors.RED, "You can't place down blocks while holding a custom feature in each hand."));
            return;
        }

        CustomMaterial customMaterial = customMaterialsPlaced.getValue().iterator().next();

        event.getTransactions().forEach(transaction -> {
            BlockSnapshot defaultSnapshot = transaction.getDefault();
            Location<World> location = defaultSnapshot.getLocation()
                    .orElseThrow(() -> new IllegalStateException("Could not get the location of a block snapshot."));
            TileEntityArchetype customArchetype = defaultSnapshot.createArchetype()
                    .orElseThrow(() -> new IllegalStateException("Could not create a tile entity archetype."));

            ItemStack savedItemStack = customMaterial.getItemStack().copy();

            savedItemStack.setQuantity(1);

            ItemStackSnapshot savedItemSnapshot = savedItemStack.createSnapshot();
            RepresentedCustomItemSnapshotData data = new RepresentedCustomItemSnapshotData(savedItemSnapshot);

            customArchetype.offer(data);

            BlockSnapshot customSnapshot = customArchetype.toSnapshot(location);

            transaction.setCustom(customSnapshot);

            // workaround of custom data not being applied immediately
            Sponge.getScheduler().createTaskBuilder()
                    .execute(task -> {
                        TileEntity tileEntity = location.getTileEntity()
                                .orElseThrow(() -> new IllegalStateException("Could not get the tile entity of the placed skull."));

                        tileEntity.offer(data);
                    })
                    .submit(CustomItemLibrary.getInstance());
        });
    }

    @Listener(order = Order.BEFORE_POST)
    public void onChangeBlockBreak(ChangeBlockEvent.Break event) {
        event.getTransactions().forEach(transaction -> {
            BlockSnapshot original = transaction.getOriginal();

            original.get(ImmutableRepresentedCustomItemSnapshotData.class).ifPresent(immutableRepresentedCustomItemSnapshotData -> {
                ItemStackSnapshot snapshot = immutableRepresentedCustomItemSnapshotData.representedCustomItemSnapshot().get();
                ItemStack itemStack = snapshot.createStack();

                CustomItemLibrary.getInstance().getService().getCustomItem(itemStack)
                        .filter(CustomMaterial.class::isInstance)
                        .map(CustomMaterial.class::cast)
                        .ifPresent(customMaterial -> {
                            Location<World> location = transaction.getOriginal().getLocation()
                                    .orElseThrow(() -> new IllegalStateException("Could not get the location of a block snapshot."));
                            transaction.setValid(false);

                            Sponge.getScheduler().createTaskBuilder()
                                    .execute(task -> {
                                        Location<World> itemLocation = location.add(Vector3d.ONE.mul(0.5));
                                        Cause setBlockCause = Cause.source(CustomItemLibrary.getInstance().getPluginContainer())
                                                .notifier(event)
                                                .build();

                                        location.setBlockType(BlockTypes.AIR, setBlockCause);
                                        Util.spawnItem(itemLocation, customMaterial.getItemStack().createSnapshot(), event);
                                    })
                                    .submit(CustomItemLibrary.getInstance());
                        });
            });
        });
    }
    */

    private Map<String, CompletableFuture<SkinRecord>> getTexturesToSkins(PluginContainer pluginContainer) {
        return pluginIdsToTexturesToSkins.computeIfAbsent(pluginContainer.getId(), k -> Maps.newHashMap());
    }

    private BiMap<String, SkinRecord> getTexturesToReadySkins(PluginContainer pluginContainer) {
        return getTexturesToReadySkins(pluginContainer.getId());
    }

    private BiMap<String, SkinRecord> getTexturesToReadySkins(String pluginId) {
        return pluginIdsToTexturesToReadySkins.computeIfAbsent(pluginId, k -> HashBiMap.create());
    }

    private MineskinService getMineskinService() {
        return Sponge.getServiceManager().provide(MineskinService.class)
                .orElseThrow(() -> new IllegalStateException("Could not access the MineskinService."));
    }

    public static CustomMaterialRegistry getInstance() {
        return INSTANCE;
    }
}
