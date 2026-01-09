package com.lumii.lumenium.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.lumii.lumenium.utils.effect.UnclearableEffect;
import com.lumii.lumenium.utils.item.KillSaveItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
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
        if (attacker instanceof LivingEntity livingAttacker) {
            ItemStack stack = livingAttacker.getMainHandStack();
            if (stack.getItem() instanceof KillSaveItem killSave) {
                if (killSave.killEntity(livingAttacker.getWorld(), stack, livingAttacker, living)) {
                    cir.setReturnValue(true);
                }
            }
        }
    }

    @WrapMethod(method = "clearStatusEffects")
    private boolean preventClear(Operation<Boolean> original) {
        LivingEntity living = (LivingEntity)(Object)this;
        if (!living.getWorld().isClient()) {
            for (StatusEffectInstance instance : living.getActiveStatusEffects().values()) {
                if (instance.getEffectType() instanceof UnclearableEffect) {
                    boolean result = original.call();
                    this.addStatusEffect(instance);
                    return result;
                }
            }
        }
        return original.call();
    }
}
