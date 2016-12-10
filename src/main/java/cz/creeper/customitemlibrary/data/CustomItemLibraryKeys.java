package cz.creeper.customitemlibrary.data;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.KeyFactory;
import org.spongepowered.api.data.value.mutable.Value;

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

    private CustomItemLibraryKeys() {}
}
