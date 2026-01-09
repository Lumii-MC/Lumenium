package com.lumii.lumenium.utils.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;

public interface CustomKillSourceItem {
    /**
     * Return a custom DamageSource for kills with this item.
     *
     * @param stack    the weapon ItemStack
     * @param attacker the entity using the item
     * @return a DamageSource to use for kills
     */
    DamageSource getKillSource(ItemStack stack, LivingEntity attacker);
}

