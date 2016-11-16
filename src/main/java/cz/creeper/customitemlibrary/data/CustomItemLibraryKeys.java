package cz.creeper.customitemlibrary.data;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.KeyFactory;
import org.spongepowered.api.data.value.mutable.Value;

public final class CustomItemLibraryKeys {
    public static final Key<Value<String>> CUSTOM_ITEM_ID =
        KeyFactory.makeSingleKey(new TypeToken<String>() {},
                                 new TypeToken<Value<String>>() {},
                                 DataQuery.of("CustomItemId"),
                                 "customitemlibrary:custom_item_id",
                                 "Custom Item ID");

    private CustomItemLibraryKeys() {}
}
