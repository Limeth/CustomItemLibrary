package cz.creeper.customitemlibrary.managers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import cz.creeper.customitemlibrary.CustomItemLibrary;
import cz.creeper.customitemlibrary.util.Identifier;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemCategoryManager {
    public static final String NODE_ITEM_CATEGORIES = "item_categories";
    private final Multimap<String, Identifier> categoryToItemIds = HashMultimap.create();

    public ItemCategoryManager load() {
        categoryToItemIds.clear();

        CustomItemLibrary plugin = CustomItemLibrary.getInstance();
        Path path = plugin.getConfigPath();
        ConfigurationOptions options = plugin.getDefaultConfigurationOptions();
        HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .setDefaultOptions(options).setPath(path).build();

        try {
            CommentedConfigurationNode root = loader.load();
            CommentedConfigurationNode nodeCategories = root.getNode(NODE_ITEM_CATEGORIES);

            nodeCategories.setComment("Categories are used to determine the mining speed of blocks. Here you can add items from mods.");

            if(nodeCategories.isVirtual()) {
                defaultCategory(nodeCategories, "shovel", ItemTypes.WOODEN_SHOVEL, ItemTypes.STONE_SHOVEL,
                        ItemTypes.IRON_SHOVEL, ItemTypes.GOLDEN_SHOVEL, ItemTypes.DIAMOND_SHOVEL);
                defaultCategory(nodeCategories, "hoe", ItemTypes.WOODEN_HOE, ItemTypes.STONE_HOE,
                        ItemTypes.IRON_HOE, ItemTypes.GOLDEN_HOE, ItemTypes.DIAMOND_HOE);
                defaultCategory(nodeCategories, "pickaxe", ItemTypes.WOODEN_PICKAXE, ItemTypes.STONE_PICKAXE,
                        ItemTypes.IRON_PICKAXE, ItemTypes.GOLDEN_PICKAXE, ItemTypes.DIAMOND_PICKAXE);
                defaultCategory(nodeCategories, "axe", ItemTypes.WOODEN_AXE, ItemTypes.STONE_AXE,
                        ItemTypes.IRON_AXE, ItemTypes.GOLDEN_AXE, ItemTypes.DIAMOND_AXE);
                defaultCategory(nodeCategories, "shears", ItemTypes.SHEARS);
            }

            nodeCategories.getChildrenMap().forEach((rawCategory, rawItemIds) -> {
                String category = Objects.toString(rawCategory);
                List<String> itemIds = rawItemIds.getList(Objects::toString);
                Set<Identifier> identifiers = itemIds.stream().map(itemId -> {
                    if(Identifier.isParseable(itemId))
                        return Identifier.parse(itemId);
                    else
                        return new Identifier("minecraft", itemId);
                }).collect(Collectors.toSet());

                categoryToItemIds.putAll(category, identifiers);
            });

            loader.save(root);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;
    }

    private static void defaultCategory(CommentedConfigurationNode nodeCategories, String category, ItemType... itemTypes) {
        nodeCategories.getNode(category).setValue(
                Stream.of(itemTypes).map(ItemType::getId).collect(Collectors.toList())
        );
    }
}
