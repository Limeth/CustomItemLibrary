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
import cz.creeper.customitemlibrary.event.CustomBlockBreakEvent;
import cz.creeper.customitemlibrary.event.CustomBlockBreakItemDropEvent;
import cz.creeper.customitemlibrary.event.MiningProgressEvent;
import cz.creeper.customitemlibrary.feature.CustomFeatureRegistry;
import cz.creeper.customitemlibrary.feature.DurabilityRegistry;
import cz.creeper.customitemlibrary.managers.MiningManager;
import lombok.AccessLevel;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public class SimpleCustomBlockRegistry implements CustomFeatureRegistry<SimpleCustomBlock, SimpleCustomBlockDefinition> {
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
        DurabilityRegistry.getInstance().register(SimpleCustomBlock.HELMET_ITEM_TYPE, definition);
    }

    @Override
    public void writeAsset(SimpleCustomBlockDefinition definition, String asset, ReadableByteChannel input, WritableByteChannel output) throws IOException {
        if(definition.getModelAssets().contains(asset)) {
            try (
                    Reader reader = Channels.newReader(input, "UTF-8");
                    Writer writer = Channels.newWriter(output, "UTF-8")
            ) {
                JsonParser parser = new JsonParser();
                JsonElement root = parser.parse(reader);

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

                getGson().toJson(root, writer);
            }
        } else {
            CustomFeatureRegistry.super.writeAsset(definition, asset, input, output);
        }
    }

    @Listener(order = Order.BEFORE_POST)
    public void onMiningProgress(MiningProgressEvent event, @First Player player) {
        BlockSnapshot snapshot = event.getSnapshot();
        Location<World> location = snapshot.getLocation()
                .orElseThrow(() -> new IllegalStateException("Could not access the location of the block that is being mined."));

        CustomItemLibrary.getInstance().getService().getBlock(location)
            .filter(SimpleCustomBlock.class::isInstance)
            .map(SimpleCustomBlock.class::cast)
            .ifPresent(customBlock -> {
                SimpleCustomBlockDefinition definition = customBlock.getDefinition();
                double hardness = definition.getHardness();
                Optional<ItemStack> itemInHand = player.getItemInHand(HandTypes.MAIN_HAND);
                boolean correctToolUsed = definition.getCorrectToolPredicate().isCorrectTool(itemInHand.orElse(null));
                double requiredDuration = MiningManager.computeDuration(player, itemInHand.orElse(null),
                                                                        correctToolUsed, hardness);

                if (event.getDuration() >= requiredDuration) {
                    Cause cause = event.getCause();
                    CustomBlockBreakEvent customEvent = CustomBlockBreakEvent.of(customBlock, cause);

                    Sponge.getEventManager().post(customEvent);

                    if(customEvent.isCancelled())
                        return;

                    if(correctToolUsed) {
                        itemInHand.ifPresent(itemStack -> {
                            itemStack.get(Keys.ITEM_DURABILITY).ifPresent(durability -> {
                                int newDurability = durability - 1;

                                if(newDurability >= 0) {
                                    itemStack.offer(Keys.ITEM_DURABILITY, newDurability);
                                    player.setItemInHand(HandTypes.MAIN_HAND, itemStack);
                                } else {
                                    ParticleEffect crackEffect = ParticleEffect.builder()
                                            .type(ParticleTypes.ITEM_CRACK)
                                            .option(ParticleOptions.ITEM_STACK_SNAPSHOT, itemStack.createSnapshot())
                                            .build();
                                    Location<World> playerLocation = player.getLocation();
                                    World world = playerLocation.getExtent();
                                    Vector3d crackPosition = playerLocation.getPosition().add(Vector3d.UNIT_Y.mul(1.62));

                                    world.spawnParticles(crackEffect, crackPosition);
                                    player.setItemInHand(HandTypes.MAIN_HAND, null);
                                }
                            });
                        });

                        List<ItemStackSnapshot> drops = definition.getDropProvider()
                                .provideDrops(customBlock, player, cause);

                        if(!drops.isEmpty()) {
                            World world = player.getWorld();
                            Vector3d itemPosition = customBlock.getBlock().getPosition().toDouble()
                                    .add(Vector3d.ONE.mul(0.5));
                            List<Item> items = drops.stream()
                                    .map(itemStackSnapshot -> {
                                        Item item = (Item) world.createEntity(EntityTypes.ITEM, itemPosition);

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

                    World world = location.getExtent();
                    ParticleEffect particleEffect = ParticleEffect.builder()
                            .type(ParticleTypes.BREAK_BLOCK)
                            .option(ParticleOptions.BLOCK_STATE, definition.getEffectState())
                            .build();
                    Vector3d particlePosition = location.getBlockPosition().toDouble();

                    world.spawnParticles(particleEffect, particlePosition);
                    location.setBlockType(BlockTypes.AIR, BlockChangeFlag.ALL, cause);
                    event.setCancelled(true);
                }
            });
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
