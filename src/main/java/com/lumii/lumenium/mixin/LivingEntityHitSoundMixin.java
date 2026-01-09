package com.lumii.lumenium.mixin;


import com.lumii.lumenium.utils.item.CustomHitSoundItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityHitSoundMixin {

    @Inject(method = "damage", at = @At("TAIL"))
    private void lumii$customHitSound(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return; // only if damage actually applied

        if (!(source.getAttacker() instanceof LivingEntity attacker)) return;
        LivingEntity victim = (LivingEntity) (Object) this;
        World world = victim.getWorld();

        ItemStack stack = attacker.getMainHandStack();
        if (stack.getItem() instanceof CustomHitSoundItem soundItem) {
            SoundEvent sound = soundItem.getHitSound(world, stack, attacker, victim);
            if (sound != null) {
                world.playSound(
                        null,
                        victim.getX(),
                        victim.getY(),
                        victim.getZ(),
                        sound,
                        victim.getSoundCategory(),
                        soundItem.getHitVolume(stack),
                        soundItem.getHitPitch(stack)
                );
            }
        }
    }
}
