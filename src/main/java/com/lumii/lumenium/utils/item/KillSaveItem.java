package com.lumii.lumenium.utils.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface KillSaveItem {

    /**
     * Called when this item is about to kill a target.
     *
     * @param world   The world of the attacker
     * @param stack   The weapon ItemStack
     * @param attacker The entity doing the attack
     * @param victim  The LivingEntity that would die
     * @return true to "save" the victim (prevent death), false to allow normal death
     */
    boolean killEntity(World world, ItemStack stack, LivingEntity attacker, LivingEntity victim);
}


