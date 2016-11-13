package cz.creeper.customitemlibrary;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import cz.creeper.customitemlibrary.util.BiKeyHashMap;
import cz.creeper.customitemlibrary.util.BiKeyMap;
import org.spongepowered.api.Sponge;

import java.util.Optional;

public class CustomItemServiceImpl implements CustomItemService {
    public static final char ID_SEPARATOR = ':';
    private final BiKeyMap<String, Integer, CustomItemRecord> records = new BiKeyHashMap<>();
    //private final BiMap<String, Integer> idToDurability = HashBiMap.create();

    @Override
    public CustomItemRecord registerCustomItem(Object plugin, String typeId) {
        String id = getId(plugin, typeId);
        Optional<CustomItemRecord> record = getCustomItemRecord(id);

        if(record.isPresent()) {
            return record.get();
        } else {
            int durability = records.size();
            CustomItemRecord newRecord = new CustomItemRecord(this, id, durability);

            records.put(id, durability, newRecord);

            return newRecord;
        }
    }

    @Override
    public Optional<CustomItemRecord> getCustomItemRecord(Object plugin, String typeId) {
        return getCustomItemRecord(getId(plugin, typeId));
    }

    private Optional<CustomItemRecord> getCustomItemRecord(String id) {
        return Optional.ofNullable(records.getFirst(id));
    }

    @Override
    public Optional<CustomItemRecord> getCustomItemRecord(Object plugin, int durability) {
        return Optional.ofNullable(records.getSecond(durability));
    }

    @Override
    public void loadDictionary() {

    }

    @Override
    public void saveDictionary() {

    }

    private static String getId(Object plugin, String typeId) {
        Preconditions.checkNotNull(plugin, "plugin");
        Preconditions.checkNotNull(typeId, "typeId");

        return Sponge.getPluginManager().fromInstance(plugin)
                .orElseThrow(() -> new IllegalArgumentException("Could not get the plugin container."))
                + Character.toString(ID_SEPARATOR) + typeId;
    }
}
