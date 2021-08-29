package net.fabricmc.knb.blocks.blockitems;

import net.fabricmc.knb.blocks.BlocksKNB;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BlockItemsKNB {

    public static final Item.Settings BUILDING_BLOCKS = new Item.Settings().group(ItemGroup.BUILDING_BLOCKS);

    public static void items(String modName) {

        Registry.register(Registry.ITEM, new Identifier(modName, "nether_beacon"),
                new BlockItem(BlocksKNB.netherBeaconBlock, BUILDING_BLOCKS));

    }
}
