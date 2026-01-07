package com.lumii.softlightlib.utils.item;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface BipedEntityModelPoseItem {
    BipedEntityModel.ArmPose getArmPose(ItemStack stack, PlayerEntity player);
}
