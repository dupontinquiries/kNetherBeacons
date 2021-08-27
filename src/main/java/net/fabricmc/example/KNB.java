package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.example.blocks.BlocksKNB;
import net.fabricmc.example.blocks.blockitems.BlockItemsKNB;

public class KNB implements ModInitializer {

	private static final String ModName = "knb";

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		System.out.println("Hello Fabric world!");
		BlocksKNB.blocks(ModName);
		BlockItemsKNB.items(ModName);
	}
}
