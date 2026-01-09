package com.lumii.lumenium.mixin;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.BipedEntityModel.ArmPose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BipedEntityModel.class)
public interface BipedEntityModelAccessor {

    @Accessor("rightArmPose")
    void softlightlib$setRightArmPose(ArmPose pose);

    @Accessor("leftArmPose")
    void softlightlib$setLeftArmPose(ArmPose pose);
}
