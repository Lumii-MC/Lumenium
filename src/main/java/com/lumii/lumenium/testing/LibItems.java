package com.lumii.lumenium.testing;

import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class LibItems {

    public static final Item TEST_ITEM = register("test_item", new TestItem(new Item.Settings()));
    public static final Item ALT_TEST_ITEM = register("alt_test_item", new AltTestItem(ToolMaterials.NETHERITE, 3, -2.4f, new Item.Settings()));

    private static Item register(String path, Item item) {
        return Registry.register(Registry.ITEM, new Identifier("lumenium", path), item);
    }

    public static void init(){}
}
