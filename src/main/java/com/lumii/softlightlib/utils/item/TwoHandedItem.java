package com.lumii.softlightlib.utils.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface TwoHandedItem {
    /**
     * Determines if this weapon is two-handed.
     */
    default boolean isTwoHanded(ItemStack stack) {
        return true;
    }

    /**
     * Prevents off-hand usage when holding a two-handed item.
     */
    static boolean canUseOffhand(PlayerEntity player) {
        ItemStack main = player.getMainHandStack();
        if (main.getItem() instanceof TwoHandedItem twoHanded && twoHanded.isTwoHanded(main)) {
            return false;
        }
        return true;
    }
}
