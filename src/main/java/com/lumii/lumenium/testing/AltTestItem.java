package com.lumii.lumenium.testing;

import com.lumii.lumenium.utils.generic.CustomNameColor;
import com.lumii.lumenium.utils.item.TwoHandedItem;
import com.lumii.lumenium.utils.render.SphereRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AltTestItem extends SwordItem implements TwoHandedItem, CustomNameColor {
    public AltTestItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
        ItemStack stack = user.getStackInHand(hand);

        if(world.isClient){
            SphereRenderer.scheduleClient(
                    new Vec3d(user.getX(), user.getY(), user.getZ()),
                    1.5f,
                    new Identifier("minecraft:textures/misc/white.png"),
                    60,
                    0.5f,
                    false,
                    0
            );
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public int getHexColor(ItemStack stack) {
        return 0xFF004F;
    }

    @Override
    public Text getName(ItemStack stack) {
        return getColoredName(stack);
    }
}
