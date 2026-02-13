package com.lumii.lumenium.testing;

import com.lumii.lumenium.utils.item.TwoHandedItem;
import com.lumii.lumenium.utils.math.Easings;
import com.lumii.lumenium.utils.render.screen.ScreenFlashUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class FlashTestItem extends Item implements TwoHandedItem {
    public FlashTestItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
        ItemStack stack = user.getStackInHand(hand);
        ScreenFlashUtil.flash(
                255, 255, 255,
                1,
                10,
                Easings.EXPO_OUT
        );
        return TypedActionResult.success(stack);
    }
}
