package cz.creeper.customitemlibrary.feature.block;

import cz.creeper.customitemlibrary.feature.CustomFeature;
import cz.creeper.customitemlibrary.feature.item.CustomItem;
import cz.creeper.customitemlibrary.feature.HasModels;

public interface CustomBlock<T extends CustomBlockDefinition<? extends CustomBlock<T>>> extends CustomFeature<T> {
}
