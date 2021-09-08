package net.fabricmc.knb;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.knb.blocks.BlocksKNB;
import net.fabricmc.knb.blocks.blockitems.BlockItemsKNB;
import net.fabricmc.knb.entity.NetherBeaconEntity;
import net.fabricmc.knb.ui.NetherBeaconScreenHandler;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class KNB implements ModInitializer {

	// modid
	public static final String modName = "knb";

	// nether beacon
	public static BlockEntityType<NetherBeaconEntity> netherBeaconEntityType;
	public static final ScreenHandlerType<NetherBeaconScreenHandler> beaconScreen;
	public static final Identifier netherBeaconIdentifier = new Identifier(modName, "nether_beacon");
	//public static final ScreenHandlerType<NetherBeaconScreenHandler> netherBeaconScreen;

	static {
		// UI
		beaconScreen = ScreenHandlerRegistry.registerSimple(netherBeaconIdentifier, NetherBeaconScreenHandler::new);
		// entity
		//netherBeaconEntityType = Registry.register(Registry.BLOCK_ENTITY_TYPE, netherBeaconIdentifier, FabricBlockEntityTypeBuilder.create(NetherBeaconEntity::new, BlocksKNB.NETHER_BEACON).build());
		netherBeaconEntityType = Registry.register(Registry.BLOCK_ENTITY_TYPE, netherBeaconIdentifier, FabricBlockEntityTypeBuilder.create(NetherBeaconEntity::new, BlocksKNB.netherBeaconBlock).build());
	}

	@Override
	public void onInitialize() {

		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		System.out.println("The nether beacon is here!");
		BlocksKNB.blocks(modName);
		BlockItemsKNB.items(modName);

		// entities
		//EntitiesKNB.registerBlockEntities(modName);

		// packets
		ServerPlayNetworking.registerGlobalReceiver(new Identifier(modName, "c2s-acknowledge"), (server, player, handler, buf, responseSender) -> {
			((IsModded)player).setHasMod(true);
		});

		//ScreenHandlerTypeKNB.registerScreenHandlers(modName);

	}

}
