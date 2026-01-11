package com.lumii.lumenium.testing;

import com.lumii.lumenium.utils.generic.CustomNameColor;
import com.lumii.lumenium.utils.item.OnKillEffectItem;
import com.lumii.lumenium.utils.item.TwoHandedItem;
import com.lumii.lumenium.utils.render.BeamRenderer;
import com.lumii.lumenium.utils.render.SphereRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.server.world.ServerWorld;
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

        if(hand.equals(Hand.MAIN_HAND)  && world.isClient){
            SphereRenderer.scheduleClient(
                    new Vec3d(user.getX()+0, user.getY()+1, user.getZ()+0),
                    50f,
                    50f,
                    new Identifier("lumenium:textures/rendered/sun.png"),
                    Integer.MAX_VALUE,
                    1f,
                    false,
                    0,
                    2.5f
            );
            BeamRenderer.scheduleClient(
                    new Vec3d(user.getX(), user.getY()-5, user.getZ()),
                    1f,
                    1f,
                    600f,
                    new Identifier("lumenium:textures/rendered/folly.png"),
                    0,
                    1f,
                    true,
                    15,
                    1f,
                    10f,
                    0f
            );


        } else if (hand.equals(Hand.OFF_HAND) && world.isClient){
            SphereRenderer.scheduleClient(
                    new Vec3d(user.getX()+0, user.getY()+1, user.getZ()+0),
                    10f,
                    10f,
                    new Identifier("lumenium:textures/rendered/earth_noclouds.png"),
                    Integer.MAX_VALUE,
                    1f,
                    false,
                    0,
                    1f
            );
        }
        if(!world.isClient){
            double radius = 3.0; // how far it reaches
            double power = 3.0;  // how strong the knockback is

            Vec3d playerPos = user.getPos();

            // Find entities in a box around the player
            for (var entity : world.getEntitiesByClass(
                    LivingEntity.class,
                    user.getBoundingBox().expand(radius),
                    e -> e != user // exclude self
            )) {
                Vec3d direction = entity.getPos().subtract(playerPos).normalize();
                Vec3d velocity = direction.multiply(power).add(0, 1.5, 0); // vertical lift
                entity.setVelocity(velocity);
                entity.velocityModified = true; // make sure server updates client
            }
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
