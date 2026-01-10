package com.lumii.lumenium.mixin;

import com.lumii.lumenium.utils.item.OnKillEffectItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class KillEffectMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource source, CallbackInfo ci) {
        if (!(source.getAttacker() instanceof LivingEntity attacker)) return;
        if (!(attacker instanceof PlayerEntity player)) return;

        ItemStack main = player.getMainHandStack();
        if (main.getItem() instanceof OnKillEffectItem item) {
            item.onKill(player.world, main, player, (LivingEntity)(Object)this);
        }
    }
}