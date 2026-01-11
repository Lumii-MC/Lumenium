package com.lumii.lumenium.testing;

import com.lumii.lumenium.utils.generic.CustomItemNameColor;
import com.lumii.lumenium.utils.item.OnKillEffectItem;
import com.lumii.lumenium.utils.item.TwoHandedItem;
import com.lumii.lumenium.utils.render.BeamRenderer;
import com.lumii.lumenium.utils.render.SphereRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TestItem extends Item implements TwoHandedItem, CustomItemNameColor, OnKillEffectItem {
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
            BeamRenderer.scheduleClient(
                    new Vec3d(user.getX(), user.getY()-5, user.getZ()),
                    1f,
                    1f,
                    600f,
                    new Identifier("minecraft:textures/misc/white.png"),
                    20,
                    1f,
                    true,
                    15,
                    1f,
                    10f,
                    0f
            );
        }
        if (hand.equals(Hand.OFF_HAND) && world.isClient){
            SphereRenderer.scheduleClient(
                    new Vec3d(user.getX()+0, user.getY()+1, user.getZ()+0),
                    100f,
                    100f,
                    new Identifier("lumenium:textures/rendered/2k_stars_milky_way.png"),
                    Integer.MAX_VALUE,
                    1f,
                    false,
                    0,
                    0f
            );
        }


        if(!world.isClient){
            BeamRenderer.scheduleCommon(
                    (ServerWorld) world,
                    new Vec3d(user.getX(), user.getY()-5, user.getZ()),
                    0.5f,
                    0.5f,
                    600f,
                    new Identifier("minecraft:textures/misc/white.png"),
                    20,
                    1f,
                    true,
                    15,
                    1f,
                    100f,
                    0f
            );
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public void onKill(World world, ItemStack stack, LivingEntity attacker, LivingEntity victim) {
        if(!world.isClient){
            SphereRenderer.scheduleCommon(
                    (ServerWorld) world,
                    new Vec3d(victim.getX()+0, victim.getY(), victim.getZ()+0),
                    1f,
                    6f,
                    new Identifier("minecraft:textures/misc/white.png"),
                    20,
                    1f,
                    true,
                    10,
                    0f
            );
        }
    }
}