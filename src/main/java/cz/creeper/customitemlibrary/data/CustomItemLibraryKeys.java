package cz.creeper.customitemlibrary.data;

import com.google.common.reflect.TypeToken;
import lombok.experimental.UtilityClass;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.KeyFactory;
import org.spongepowered.api.data.value.mutable.MapValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.Map;
import java.util.UUID;

@UtilityClass
public class CustomItemLibraryKeys {
    public Key<Value<String>> CUSTOM_FEATURE_PLUGIN_ID =
            KeyFactory.makeSingleKey(new TypeToken<String>() {},
                    new TypeToken<Value<String>>() {},
                    DataQuery.of("CustomFeaturePluginId"),
                    "customitemlibrary:custom_feature_plugin_id",
                    "Custom Feature Plugin ID");

    public Key<Value<String>> CUSTOM_FEATURE_TYPE_ID =
            KeyFactory.makeSingleKey(new TypeToken<String>() {},
                    new TypeToken<Value<String>>() {},
                    DataQuery.of("CustomFeatureTypeId"),
                    "customitemlibrary:custom_feature_type_id",
                    "Custom Feature Type ID");

    public Key<Value<String>> CUSTOM_FEATURE_MODEL =
            KeyFactory.makeSingleKey(new TypeToken<String>() {},
                    new TypeToken<Value<String>>() {},
                    DataQuery.of("CustomFeatureModel"),
                    "customitemlibrary:custom_feature_model",
                    "Custom Feature Model");

    public Key<Value<ItemStackSnapshot>> REPRESENTED_CUSTOM_ITEM_SNAPSHOT =
            KeyFactory.makeSingleKey(new TypeToken<ItemStackSnapshot>() {},
                    new TypeToken<Value<ItemStackSnapshot>>() {},
                    DataQuery.of("RepresentedCustomFeatureSnapshot"),
                    "customitemlibrary:represented_custom_feature_snapshot",
                    "Represented Custom Feature Snapshot");

    public Key<Value<UUID>> CUSTOM_BLOCK_DAMAGE_INDICATOR_ARMOR_STAND_ID =
            KeyFactory.makeSingleKey(new TypeToken<UUID>() {},
                    new TypeToken<Value<UUID>>() {},
                    DataQuery.of("CustomBlockDamageIndicatorArmorStandId"),
                    "customitemlibrary:custom_block_damage_indicator_armor_stand_id",
                    "Custom Block Damage Indicator Armor Stand ID");

    public Key<MapValue<String, CustomInventoryData>> CUSTOM_INVENTORIES =
            KeyFactory.makeMapKey(new TypeToken<Map<String, CustomInventoryData>>() {},
                    new TypeToken<MapValue<String, CustomInventoryData>>() {},
                    DataQuery.of("CustomInventories"),
                    "customitemlibrary:custom_inventories",
                    "Custom Inventories");

    public Key<Value<String>> CUSTOM_INVENTORY_ID =
            KeyFactory.makeSingleKey(new TypeToken<String>() {},
                    new TypeToken<Value<String>>() {},
                    DataQuery.of("CustomInventoryId"),
                    "customitemlibrary:custom_inventory_id",
                    "Custom Inventory ID");

    public Key<MapValue<String, ItemStackSnapshot>> CUSTOM_INVENTORY_SLOT_ID_TO_ITEMSTACK =
            KeyFactory.makeMapKey(new TypeToken<Map<String, ItemStackSnapshot>>() {},
                    new TypeToken<MapValue<String, ItemStackSnapshot>>() {},
                    DataQuery.of("CustomInventorySlotIdToItemStack"),
                    "customitemlibrary:custom_inventory_slot_id_to_itemstack",
                    "Custom Inventory Slot ID to ItemStack");

    public Key<MapValue<String, String>> CUSTOM_INVENTORY_SLOT_ID_TO_FEATURE_ID =
            KeyFactory.makeMapKey(new TypeToken<Map<String, String>>() {},
                    new TypeToken<MapValue<String, String>>() {},
                    DataQuery.of("CustomInventorySlotIdToFeatureId"),
                    "customitemlibrary:custom_inventory_slot_id_to_feature_id",
                    "Custom Inventory Slot ID to Feature ID");
}
