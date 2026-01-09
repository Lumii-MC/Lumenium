package com.lumii.lumenium.testing;

import com.lumii.lumenium.utils.effect.UnclearableEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class LibEffects {

    public static StatusEffect TEST_EFFECT;

    public static void init() {

        TEST_EFFECT = Registry.register(
                Registry.STATUS_EFFECT,
                new Identifier("lumenium", "test_effect"),
                new TestEffect(StatusEffectCategory.BENEFICIAL, 0xff00ff) // purple lol
        );
    }
}

