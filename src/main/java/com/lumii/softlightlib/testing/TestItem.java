package com.lumii.softlightlib.testing;

import com.lumii.softlightlib.utils.generic.CustomNameColor;
import com.lumii.softlightlib.utils.item.TwoHandedItem;
import com.lumii.softlightlib.utils.render.CubeRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TestItem extends Item implements TwoHandedItem, CustomNameColor {
    public TestItem(Settings settings) {
        super(settings);
    }

    @Override
    public int getHexColor(ItemStack stack) {
        return 0xFF004F;
    }

    @Override
    public Text getName(ItemStack stack) {
        return getColoredName(stack);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
        ItemStack stack = user.getStackInHand(hand);

        if(world.isClient){
            CubeRenderer.scheduleClient(new Vec3d(user.getX(), user.getY()+1, user.getZ()),
                    1f, 1f, 1f,
                    new Vec3d(0, 0, 0),
                    1f,
                    new Identifier("minecraft", "textures/misc/enchanted_item_glint.png"),
                    60,
                    true,
                    45,
                    true,
                    0,
                    10f,
                    0.6f);
        }

        return TypedActionResult.success(stack);
    }
}
