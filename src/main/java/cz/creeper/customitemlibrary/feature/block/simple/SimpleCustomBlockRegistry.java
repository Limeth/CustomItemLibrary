package cz.creeper.customitemlibrary.feature.block.simple;

import com.flowpowered.math.vector.Vector3d;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.data.CustomBlockData;
import cz.creeper.customitemlibrary.data.CustomItemLibraryKeys;
import cz.creeper.customitemlibrary.event.CustomBlockBreakEvent;
import cz.creeper.customitemlibrary.event.CustomBlockBreakItemDropEvent;
import cz.creeper.customitemlibrary.event.MiningProgressEvent;
import cz.creeper.customitemlibrary.event.MiningStopEvent;
import cz.creeper.customitemlibrary.feature.CustomFeatureRegistry;
import cz.creeper.customitemlibrary.feature.DurabilityRegistry;
import cz.creeper.customitemlibrary.feature.block.CustomBlockDefinition;
import cz.creeper.customitemlibrary.managers.MiningManager;
import lombok.AccessLevel;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleCustomBlockRegistry implements CustomFeatureRegistry<SimpleCustomBlock, SimpleCustomBlockDefinition> {
    public static final ItemType DAMAGE_INDICATOR_ITEM_TYPE = ItemTypes.DIAMOND_CHESTPLATE;
    public static final String DAMAGE_INDICATOR_SUFFIX = "_damage_indicator_%d";
    public static final Vector3d TRANSLATION_DEFAULT = Vector3d.ZERO;
    public static final Vector3d TRANSLATION_VECTOR = new Vector3d(0, -43.225, 0);
    public static final BinaryOperator<Vector3d> TRANSLATION_OPERATOR = Vector3d::add;
    public static final Vector3d SCALE_DEFAULT = Vector3d.ONE;
    public static final Vector3d SCALE_VECTOR = Vector3d.ONE.mul(1.6);
    public static final BinaryOperator<Vector3d> SCALE_OPERATOR = Vector3d::mul;
    private static final SimpleCustomBlockRegistry INSTANCE = new SimpleCustomBlockRegistry();

    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void register(SimpleCustomBlockDefinition definition) {
        DurabilityRegistry durabilityRegistry = DurabilityRegistry.getInstance();

        durabilityRegistry.register(SimpleCustomBlock.HELMET_ITEM_TYPE, definition);

        if(definition.isGenerateDamageIndicatorModels()) {
            List<String> damageIndicatorModels = definition.getModels().stream()
                    .flatMap(model -> Stream.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
                            .map(stage -> getDamageIndicatorModel(model, stage)))
                    .collect(Collectors.toList());

            durabilityRegistry.register(DAMAGE_INDICATOR_ITEM_TYPE, definition.getPluginContainer(), damageIndicatorModels, definition.getModelDirectoryName());
        }
    }

    public static String getDamageIndicatorModel(String model, int stage) {
        return (model != null ? model : "default") + String.format(DAMAGE_INDICATOR_SUFFIX, stage);
    }

    public static String getDamageIndicatorModel(String model, double percentage) {
        return getDamageIndicatorModel(model, (int) Math.max(0, Math.min(9, percentage * 10)));
    }

    @Override
    public void writeAsset(SimpleCustomBlockDefinition definition, String asset,
            ReadableByteChannel input, WritableByteChannel output,
            Path outputFile) throws IOException {
        if(definition.getModelAssets().contains(asset)) {
            try (
                    Reader reader = Channels.newReader(input, "UTF-8");
                    Writer writer = Channels.newWriter(output, "UTF-8")
            ) {
                JsonParser parser = new JsonParser();
                JsonElement root = parser.parse(reader);

                alignBlockModel(root);
                getGson().toJson(root, writer);
            }
        } else {
            CustomFeatureRegistry.super.writeAsset(definition, asset, input, output,
                    outputFile);
        }
    }

    public static void alignBlockModel(JsonElement root) {
        if(root.isJsonObject()) {
            JsonObject rootObject = root.getAsJsonObject();
            JsonElement display = rootObject.get("display");
            JsonObject displayObject = display != null && display.isJsonObject()
                    ? display.getAsJsonObject() : new JsonObject();
            JsonElement head = displayObject.get("head");
            JsonObject headObject = head != null && head.isJsonObject()
                    ? head.getAsJsonObject() : new JsonObject();

            modifyArray(headObject, "translation", TRANSLATION_DEFAULT, TRANSLATION_VECTOR, TRANSLATION_OPERATOR);
            modifyArray(headObject, "scale", SCALE_DEFAULT, SCALE_VECTOR, SCALE_OPERATOR);
            displayObject.add("head", headObject);
            rootObject.add("display", displayObject);
        }
    }

    public static Optional<ArmorStand> getDamageIndicatorArmorStand(SimpleCustomBlock block) {
        return block.getDataHolder().get(CustomItemLibraryKeys.CUSTOM_BLOCK_DAMAGE_INDICATOR_ARMOR_STAND_ID)
                .flatMap(block.getExtent()::getEntity)
                .filter(ArmorStand.class::isInstance)
                .map(ArmorStand.class::cast);
    }

    public static ArmorStand getOrSpawnDamageIndicatorArmorStand(SimpleCustomBlock block) {
        CustomBlockData customBlockData = block.getDataHolder()
                .get(CustomBlockData.class)
                .orElseGet(CustomBlockData::new);

        UUID damageIndicatorArmorStandId = customBlockData.damageIndicatorArmorStandId().get();
        Extent extent = block.getExtent();
        ArmorStand armorStand;

        dance:
        {
            if (!damageIndicatorArmorStandId.equals(CustomBlockData.UUID_MISSING)) {
                Optional<Entity> entityOptional =
                        extent.getEntity(damageIndicatorArmorStandId);

                if (entityOptional.isPresent()) {
                    Entity entity = entityOptional.get();

                    if (entity instanceof ArmorStand) {
                        armorStand = (ArmorStand) entity;
                        break dance;
                    }
                }
            }

            armorStand = CustomBlockDefinition.createDummyArmorStand(block.getBlock());

            extent.spawnEntity(armorStand, Cause.source(EntitySpawnCause.builder()
                    .entity(armorStand)
                    .type(SpawnTypes.PLUGIN).build())
                    .build());
            customBlockData.set(CustomItemLibraryKeys.CUSTOM_BLOCK_DAMAGE_INDICATOR_ARMOR_STAND_ID, armorStand.getUniqueId());
        }

        block.getDataHolder().offer(customBlockData);

        return armorStand;
    }

    @Listener(order = Order.BEFORE_POST)
    public void onMiningProgress(MiningProgressEvent event) {
        Player player = event.getPlayer();
        BlockSnapshot snapshot = event.getSnapshot();
        Location<World> location = snapshot.getLocation()
                .orElseThrow(() -> new IllegalStateException("Could not access the location of the block that is being mined."));

        CustomItemLibrary.getInstance().getService().getBlock(location)
            .filter(SimpleCustomBlock.class::isInstance)
            .map(SimpleCustomBlock.class::cast)
            .ifPresent(customBlock -> {
                SimpleCustomBlockDefinition definition = customBlock.getDefinition();
                BlockType harvestingType = definition.getHarvestingType();
                double hardness = definition.getHardness();
                MiningManager.MiningDuration duration = MiningManager.computeDuration(player, harvestingType, hardness);

                if (event.getDuration() >= duration.getBreakDuration()) {
                    Cause cause = event.getCause();
                    CustomBlockBreakEvent customEvent = CustomBlockBreakEvent.of(customBlock, cause);

                    Sponge.getEventManager().post(customEvent);

                    if(customEvent.isCancelled())
                        return;

                    if(duration.isCorrectToolUsed()) {
                        spawnDrops(snapshot, customBlock, player, cause);
                    }

                    World world = location.getExtent();
                    ParticleEffect particleEffect = ParticleEffect.builder()
                            .type(ParticleTypes.BREAK_BLOCK)
                            .option(ParticleOptions.BLOCK_STATE, definition.getBreakEffectState())
                            .build();
                    Vector3d particlePosition = location.getBlockPosition().toDouble();

                    world.spawnParticles(particleEffect, particlePosition);
                    location.setBlockType(BlockTypes.AIR, cause);
                    event.setCancelled(true);

                    getDamageIndicatorArmorStand(customBlock)
                            .ifPresent(Entity::remove);
                } else {
                    ArmorStand damageIndicatorArmorStand = getOrSpawnDamageIndicatorArmorStand(customBlock);
                    String currentModel = definition.isGenerateDamageIndicatorModels() ? customBlock.getModel() : null;
                    double progress = event.getDuration() / duration.getBreakDuration();
                    String damageIndicatorModel = getDamageIndicatorModel(currentModel, progress);
                    ItemStack damageIndicatorItemStack = DurabilityRegistry.createItemUnsafe(DAMAGE_INDICATOR_ITEM_TYPE, definition.getPluginContainer(), damageIndicatorModel);

                    damageIndicatorArmorStand.setHelmet(damageIndicatorItemStack);
                }
            });
    }

    @Listener(order = Order.BEFORE_POST)
    public void onMiningStop(MiningStopEvent event) {
        BlockSnapshot snapshot = event.getSnapshot();
        Location<World> location = snapshot.getLocation()
                .orElseThrow(() -> new IllegalStateException("Could not access the location of the block that is being mined."));

        CustomItemLibrary.getInstance().getService().getBlock(location)
                .filter(SimpleCustomBlock.class::isInstance)
                .map(SimpleCustomBlock.class::cast)
                .flatMap(SimpleCustomBlockRegistry::getDamageIndicatorArmorStand)
                .ifPresent(Entity::remove);
    }

    private static void spawnDrops(BlockSnapshot snapshot, SimpleCustomBlock customBlock, Player player, Cause cause) {
        SimpleCustomBlockDefinition definition = customBlock.getDefinition();
        List<ItemStackSnapshot> drops = definition.getDropProvider()
                .provideDrops(customBlock, player, cause);

        if(!drops.isEmpty()) {
            World world = player.getWorld();
            Vector3d itemPosition = customBlock.getBlock().getPosition().toDouble()
                    .add(Vector3d.ONE.mul(0.5));
            List<Item> items = drops.stream()
                    .map(itemStackSnapshot -> {
                        Item item = (Item) world.createEntity(
                                EntityTypes.ITEM, itemPosition);

                        item.offer(Keys.REPRESENTED_ITEM, itemStackSnapshot);

                        return item;
                    })
                    .collect(Collectors.toList());

            Cause subEventItemCause = Cause.source(snapshot).from(cause).build();
            CustomBlockBreakItemDropEvent subEventItem = new CustomBlockBreakItemDropEvent(items, world, subEventItemCause);

            Sponge.getEventManager().post(subEventItem);

            if(!subEventItem.isCancelled()) {
                subEventItem.getEntities().forEach(entity -> {
                    Cause itemSpawnCause = Cause.source(
                                EntitySpawnCause.builder()
                                        .entity(entity)
                                        .type(SpawnTypes.PLUGIN)
                                        .build()
                            )
                            .from(cause)
                            .owner(CustomItemLibrary.getInstance().getPluginContainer())
                            .build();

                    world.spawnEntity(entity, itemSpawnCause);
                });
            }
        }
    }

    public static SimpleCustomBlockRegistry getInstance() {
        return INSTANCE;
    }

    private static void modifyArray(JsonObject parent, String name, Vector3d defaultValue, Vector3d modification, BinaryOperator<Vector3d> operator) {
        JsonElement supposedArray = parent.get(name);
        JsonArray array = supposedArray != null && supposedArray.isJsonArray() ? supposedArray.getAsJsonArray() : new JsonArray();

        if(array.size() < 3) {
            array = new JsonArray();

            array.add(new JsonPrimitive(defaultValue.getX()));
            array.add(new JsonPrimitive(defaultValue.getY()));
            array.add(new JsonPrimitive(defaultValue.getZ()));
        }

        Vector3d found = new Vector3d(array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble());
        Vector3d result = operator.apply(found, modification);

        array = new JsonArray();

        array.add(new JsonPrimitive(result.getX()));
        array.add(new JsonPrimitive(result.getY()));
        array.add(new JsonPrimitive(result.getZ()));

        parent.add(name, array);
    }
}
