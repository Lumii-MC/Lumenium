package com.lumii.softlightlib.mixin;

import com.lumii.softlightlib.utils.item.TwoHandedItem;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.BipedEntityModel.ArmPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> {

    @Inject(method = "setAngles*", at = @At("HEAD"))
    private void spindled$useCrossbowHoldPose(
            T entity,
            float limbAngle,
            float limbDistance,
            float customAngle,
            float headYaw,
            float headPitch,
            CallbackInfo ci) {

        ItemStack stack = entity.getMainHandStack();

        if (stack.getItem() instanceof TwoHandedItem) {
            // Set right arm to CROSSBOW_HOLD
            ((BipedEntityModelAccessor) this).softlightlib$setRightArmPose(ArmPose.CROSSBOW_CHARGE);

            // Optionally set left arm to EMPTY or ITEM if off-hand looks weird
            ((BipedEntityModelAccessor) this).softlightlib$setLeftArmPose(ArmPose.EMPTY);
        }
    }
}

