package net.fabricmc.knb.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.knb.KNB;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;


public class BlocksKNB {

    public static final NetherBeaconBlock netherBeaconBlock; // = new NetherBeaconBlock(FabricBlockSettings.copyOf(Blocks.BEACON));

    static {
        netherBeaconBlock = Registry.register(Registry.BLOCK, KNB.netherBeaconIdentifier, new NetherBeaconBlock(FabricBlockSettings.copyOf(Blocks.BEACON).breakInstantly()));
    }

    public static void blocks(String modName) {

    }
}
