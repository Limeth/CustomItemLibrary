package cz.creeper.customitemlibrary.data;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.KeyFactory;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public final class CustomItemLibraryKeys {
    public static final Key<Value<String>> CUSTOM_ITEM_PLUGIN_ID =
            KeyFactory.makeSingleKey(new TypeToken<String>() {},
                    new TypeToken<Value<String>>() {},
                    DataQuery.of("CustomItemPluginId"),
                    "customitemlibrary:custom_item_plugin_id",
                    "Custom Item Plugin ID");

    public static final Key<Value<String>> CUSTOM_ITEM_TYPE_ID =
            KeyFactory.makeSingleKey(new TypeToken<String>() {},
                    new TypeToken<Value<String>>() {},
                    DataQuery.of("CustomItemTypeId"),
                    "customitemlibrary:custom_item_type_id",
                    "Custom Item Type ID");

    public static final Key<Value<ItemStackSnapshot>> REPRESENTED_CUSTOM_ITEM_SNAPSHOT =
            KeyFactory.makeSingleKey(new TypeToken<ItemStackSnapshot>() {},
                    new TypeToken<Value<ItemStackSnapshot>>() {},
                    DataQuery.of("RepresentedCustomItemSnapshot"),
                    "customitemlibrary:represented_custom_item_snapshot",
                    "Represented Custom Item Snapshot");

    private CustomItemLibraryKeys() {}
}
