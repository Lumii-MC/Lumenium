package com.lumii.softlightlib.mixin;


import com.lumii.softlightlib.utils.item.KillSaveItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityKillSaveMixin {

    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private void lumii$callKillSave(DamageSource source, CallbackInfo ci) {
        Entity attackerEntity = source.getAttacker();
        if (!(attackerEntity instanceof LivingEntity attacker)) return;

        LivingEntity victim = (LivingEntity) (Object) this;
        World world = victim.getWorld();

        ItemStack stack = attacker.getMainHandStack();
        if (stack.getItem() instanceof KillSaveItem saveItem) {
            boolean saved = saveItem.onKill(world, stack, attacker, victim);
            if (saved) {
                ci.cancel(); // stop the death
            }
        }
    }
}

