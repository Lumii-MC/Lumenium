package com.lumii.softlightlib.utils.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface KillSaveItem {

    /**
     * Called when this weapon is about to kill a target.
     *
     * @param world   The world of the attacker
     * @param stack   The weapon ItemStack
     * @param attacker The entity doing the attack
     * @param victim  The LivingEntity that would die
     * @return true to "save" the victim (prevent death), false to allow normal death
     */
    boolean onKill(World world, ItemStack stack, LivingEntity attacker, LivingEntity victim);
}


