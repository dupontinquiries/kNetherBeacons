package net.fabricmc.knb;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.knb.blocks.BlocksKNB;
import net.fabricmc.knb.blocks.blockitems.BlockItemsKNB;
import net.fabricmc.knb.effects.EffectsKNB;
import net.fabricmc.knb.entity.NetherBeaconEntity;
import net.fabricmc.knb.entity.VillagerBeaconEntity;
import net.fabricmc.knb.ui.NetherBeaconScreenHandler;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Vector;

public class KNB implements ModInitializer {

	// modid
	public static final String modName = "knb";

	// nether beacon
	public static BlockEntityType<NetherBeaconEntity> netherBeaconEntityType;
	public static final ScreenHandlerType<NetherBeaconScreenHandler> beaconScreen;
	public static final Identifier netherBeaconIdentifier = new Identifier(modName, "nether_beacon");
	public static final Identifier precursorIdentifier = new Identifier(modName, "nether_beacon_precursor");
	//public static final ScreenHandlerType<NetherBeaconScreenHandler> netherBeaconScreen;

	// villager beacon
	public static BlockEntityType<VillagerBeaconEntity> villagerBeaconEntityType;
	public static final Identifier villagerBeaconIdentifier = new Identifier(modName, "villager_beacon");

	// music discs
	//public static Vector<String> musicDiscs = new Vector<String>();
	public static final Identifier altitudeIdentifier = new Identifier(modName, "altitude");
	public static final SoundEvent altitudeMusicEvent = new SoundEvent(altitudeIdentifier);
	//public static final Identifier jamiesname_shulk_ID = id("jamiesname_shulk");
	//public static final SoundEvent jamiesname_shulkEvent = new SoundEvent(jamiesname_shulk_ID);

	public static StatusEffect guardianEffect;
	public static Identifier guardianEffectIdentifier = new Identifier(KNB.modName, "guardian_effect");

    static {
		// UI
		beaconScreen = ScreenHandlerRegistry.registerSimple(netherBeaconIdentifier, NetherBeaconScreenHandler::new);
		// entity
		//netherBeaconEntityType = Registry.register(Registry.BLOCK_ENTITY_TYPE, netherBeaconIdentifier, FabricBlockEntityTypeBuilder.create(NetherBeaconEntity::new, BlocksKNB.NETHER_BEACON).build());
		netherBeaconEntityType = Registry.register(Registry.BLOCK_ENTITY_TYPE, netherBeaconIdentifier, FabricBlockEntityTypeBuilder.create(NetherBeaconEntity::new, BlocksKNB.netherBeaconBlock).build());
		villagerBeaconEntityType = Registry.register(Registry.BLOCK_ENTITY_TYPE, villagerBeaconIdentifier, FabricBlockEntityTypeBuilder.create(VillagerBeaconEntity::new, BlocksKNB.villagerBeaconBlock).build());

		// music discs
		Registry.register(Registry.SOUND_EVENT, altitudeIdentifier, altitudeMusicEvent);
		//musicDiscs.add("altitude");
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

		// status effect icons
		EffectsKNB.reg();

		// packets
		ServerPlayNetworking.registerGlobalReceiver(new Identifier(modName, "c2s-acknowledge"), (server, player, handler, buf, responseSender) -> {
			((IsModded)player).setHasMod(true);
		});

		//ScreenHandlerTypeKNB.registerScreenHandlers(modName);

	}

}
