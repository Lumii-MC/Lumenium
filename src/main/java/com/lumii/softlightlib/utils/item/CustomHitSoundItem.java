package com.lumii.softlightlib.utils.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;

public interface CustomHitSoundItem {
    /**
     * Called when this item hits an entity.
     *
     * @param world   the world
     * @param stack   the item being used
     * @param attacker the entity hitting
     * @param target  the entity being hit
     * @return the sound to play (null = no custom sound)
     */
    SoundEvent getHitSound(World world, ItemStack stack, LivingEntity attacker, LivingEntity target);

    /**
     * @return pitch multiplier (1.0f = normal, >1.0f = higher, <1.0f = lower)
     */
    default float getHitPitch(ItemStack stack) {
        return 1.0f;
    }

    /**
     * @return volume multiplier (1.0f = normal)
     */
    default float getHitVolume(ItemStack stack) {
        return 1.0f;
    }
}

