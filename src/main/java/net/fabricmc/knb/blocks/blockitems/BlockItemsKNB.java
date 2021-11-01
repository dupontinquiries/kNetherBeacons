package net.fabricmc.knb.blocks.blockitems;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.knb.KNB;
import net.fabricmc.knb.blocks.BlocksKNB;
import net.fabricmc.knb.disc.MusicDiskKNB;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

public class BlockItemsKNB {

    public static final Item.Settings BUILDING_BLOCKS = new Item.Settings().group(ItemGroup.BUILDING_BLOCKS);

    public static void items(String modName) {

        // blocks
        Registry.register(Registry.ITEM, KNB.netherBeaconIdentifier,
                new BlockItem(BlocksKNB.netherBeaconBlock, BUILDING_BLOCKS));

        Registry.register(Registry.ITEM, KNB.villagerBeaconIdentifier,
                new BlockItem(BlocksKNB.villagerBeaconBlock, BUILDING_BLOCKS));

        // precursor
        Registry.register(Registry.ITEM, KNB.precursorIdentifier,
                new Item(new Item.Settings().group(ItemGroup.MATERIALS).fireproof().maxCount(1).rarity(Rarity.EPIC)));

        // discs
        Registry.register(Registry.ITEM, KNB.altitudeIdentifier,
                new MusicDiskKNB(KNB.altitudeMusicEvent));

    }
}
