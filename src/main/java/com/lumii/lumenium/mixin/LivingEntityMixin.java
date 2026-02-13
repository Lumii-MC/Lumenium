package com.lumii.lumenium.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.lumii.lumenium.utils.effect.UnclearableEffect;
import com.lumii.lumenium.utils.item.KillSaveItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow
    public abstract boolean addStatusEffect(StatusEffectInstance effect);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }


    @Inject(
            method = "tryUseTotem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void killSave(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity living = (LivingEntity)(Object)this;
        Entity attacker = source.getAttacker();
        if (attacker instanceof LivingEntity lAttacker) {
            ItemStack stack = lAttacker.getMainHandStack();
            if (stack.getItem() instanceof KillSaveItem killSave) {
                if (killSave.killEntity(attacker.getWorld(), stack, lAttacker, living)) {
                    cir.setReturnValue(true);
                }
            }
        }
    }

    @Inject(method = "removeStatusEffect", at = @At("HEAD"), cancellable = true)
    private void lumenium$stopClear(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;

        // if the entity has any UnclearableEffect, prevent clearing.
        for (StatusEffectInstance inst : self.getActiveStatusEffects().values()) {
            if (inst.getEffectType() instanceof UnclearableEffect) {
                cir.setReturnValue(false);
                cir.cancel();
                return;
            }
        }
    }

}
