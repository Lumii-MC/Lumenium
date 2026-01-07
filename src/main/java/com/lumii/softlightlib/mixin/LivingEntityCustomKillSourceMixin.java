package com.lumii.softlightlib.mixin;

import com.lumii.softlightlib.utils.item.CustomKillSourceItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityCustomKillSourceMixin {

    /**
     * Replaces the DamageSource used when a LivingEntity dies
     * if the killing item implements CustomKillSourceItem.
     */
    @ModifyVariable(
            method = "onDeath",
            at = @At("HEAD"),
            argsOnly = true
    )
    private DamageSource lumii$customKillSource(DamageSource original) {
        if (!(original.getAttacker() instanceof LivingEntity attacker)) {
            return original;
        }

        ItemStack stack = attacker.getMainHandStack();
        if (stack.getItem() instanceof CustomKillSourceItem custom) {
            DamageSource customSource = custom.getKillSource(stack, attacker);
            if (customSource != null) {
                return customSource;
            }
        }

        return original;
    }
}

