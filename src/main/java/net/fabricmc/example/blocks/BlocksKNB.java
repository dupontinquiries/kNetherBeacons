package net.fabricmc.example.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;


public class BlocksKNB {

    public static final NetherBeaconBlock NETHER_BEACON = new NetherBeaconBlock(FabricBlockSettings.copyOf(Blocks.BEACON));

    public static void blocks(String modName) {

        Registry.register(Registry.BLOCK, new Identifier(modName, "nether_beacon"), NETHER_BEACON);

    }
}
