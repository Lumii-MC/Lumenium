package com.lumii.lumenium.utils.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface ReapingItem {
    /**
     * Called when this weapon successfully "reaps" a target.
     *
     * @param player The attacking player
     * @param stack  The item being used
     * @param target The living entity hit
     * @return
     */
    boolean onReap(PlayerEntity player, ItemStack stack, LivingEntity target);
}
