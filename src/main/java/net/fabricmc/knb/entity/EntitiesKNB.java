package net.fabricmc.knb.entity;

import net.fabricmc.knb.blocks.BlocksKNB;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class EntitiesKNB {

    //public static final BlockEntityType<NetherBeaconEntity> netherBeaconEntity = FabricBlockEntityTypeBuilder.create(NetherBeaconEntity::new, BlocksKNB.NETHER_BEACON).build(null);

    public static void registerBlockEntities(String modName) {
        //Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(modName, "nether_beacon_entity"), netherBeaconEntity);
    }

}
