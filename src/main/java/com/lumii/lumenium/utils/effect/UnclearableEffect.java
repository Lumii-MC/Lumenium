package com.lumii.lumenium.utils.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class UnclearableEffect extends StatusEffect {

    public UnclearableEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return false; // effect does not tick
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        // no ticking effect
    }

    @Override
    public boolean isInstant() {
        return false;
    }
}
