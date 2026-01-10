package com.lumii.lumenium.testing;

import com.lumii.lumenium.utils.generic.CustomNameColor;
import com.lumii.lumenium.utils.item.OnKillEffectItem;
import com.lumii.lumenium.utils.item.TwoHandedItem;
import com.lumii.lumenium.utils.render.SphereRenderer;
import net.minecraft.entity.LivingEntity;
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

public class AltTestItem extends SwordItem implements TwoHandedItem, CustomNameColor, OnKillEffectItem {
    public AltTestItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
        ItemStack stack = user.getStackInHand(hand);

        if(world.isClient){
            SphereRenderer.scheduleClient(
                    new Vec3d(user.getX()+0, user.getY()+1, user.getZ()+0),
                    6f,
                    6f,
                    new Identifier("lumenium:textures/rendered/2k_stars.png"),
                    1000,
                    1f,
                    false,
                    0,
                    1.0f
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

    @Override
    public void onKill(World world, ItemStack stack, LivingEntity attacker, LivingEntity victim) {
        victim.setHealth(1f);
    }
}
