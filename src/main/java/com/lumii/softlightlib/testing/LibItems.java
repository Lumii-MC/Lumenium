package com.lumii.softlightlib.testing;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class LibItems {

    public static final Item TEST_ITEM = register("test_item", new TestItem(new Item.Settings()));

    private static Item register(String name, Item item) {
        return Registry.register(Registry.ITEM, new Identifier("softlightlib", name), item);
    }

    public static void init(){}
}
