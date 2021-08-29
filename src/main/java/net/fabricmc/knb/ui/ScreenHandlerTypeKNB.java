package net.fabricmc.knb.ui;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ScreenHandlerTypeKNB {

    public static ScreenHandlerType<NetherBeaconScreenHandler> netherBeaconScreen;

    public static void registerScreenHandlers(String modName) {
        netherBeaconScreen = ScreenHandlerRegistry.registerSimple(new Identifier(modName, "nether_beacon_screen"),
                (syncId, inventory) -> new NetherBeaconScreenHandler(syncId, inventory));
                // (syncId, inventory) -> new NetherBeaconScreenHandler(netherBeaconScreen, ScreenHandlerType.BEACON, syncId, inventory, ScreenHandlerContext.EMPTY));
    }

}
