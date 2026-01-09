package com.lumii.lumenium.testing;

import com.lumii.lumenium.utils.effect.UnclearableEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class TestEffect extends UnclearableEffect {
    public TestEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }
}
