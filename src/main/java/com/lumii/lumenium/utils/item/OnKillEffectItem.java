package com.lumii.lumenium.utils.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface OnKillEffectItem {

    /**
     * Called when this item is about to kill a target.
     *
     * @param world   The world of the attacker
     * @param stack   The weapon ItemStack
     * @param attacker The entity doing the attack
     * @param victim  The LivingEntity that would die
     */

    void onKill(World world, ItemStack stack, LivingEntity attacker, LivingEntity victim);
}