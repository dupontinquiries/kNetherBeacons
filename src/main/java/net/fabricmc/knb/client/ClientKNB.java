package net.fabricmc.knb.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.knb.KNB;
import net.fabricmc.knb.render.NetherBeaconEntityRenderer;
import net.fabricmc.knb.ui.NetherBeaconScreen;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

public class ClientKNB implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        // public static <H extends ScreenHandler, S extends Screen & ScreenHandlerProvider<H>> void register(ScreenHandlerType<? extends H> type, Factory<H, S> screenFactory) {

        // ScreenRegistry.<NetherBeaconScreenHandler, HandledScreen<NetherBeaconScreenHandler>>register(HandledScreen<NetherBeaconScreenHandler>::new);

        //ScreenRegistry.<NetherBeaconScreenHandler, HandledScreen<NetherBeaconScreenHandler>>register(ScreenHandlerTypeKNB.netherBeaconScreen,
        //        (desc, inventory, title) -> new NetherBeaconScreen(desc, inventory, title));

        //BlockEntityRendererRegistry.INSTANCE.register(EntitiesKNB.netherBeaconEntity, NetherBeaconEntity::new);

        BlockEntityRendererRegistry.INSTANCE.register(KNB.netherBeaconEntityType, NetherBeaconEntityRenderer::new);

        //ScreenRegistry.register(ScreenHandlerTypeKNB.netherBeaconScreen, NetherBeaconScreen::new);

        ScreenRegistry.register(KNB.beaconScreen, NetherBeaconScreen::new);

        ////BlockEntityRendererRegistry.INSTANCE.register(KNB.netherBeaconEntityType, NetherBeaconEntity::new);

        /*ClientPlayNetworking.registerGlobalReceiver(new Identifier(KNB.modName, "s2c-query"), (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                ClientPlayNetworking.send(new Identifier(KNB.modName, "c2s-acknowledge"), PacketByteBufs.empty());
            });
        });*/

        //BlockEntityRendererRegistry.INSTANCE.register(EntitiesKNB.netherBeaconEntity, NetherBeaconEntityRenderer::new);

        /*
        //Register Textures to Chest Atlas
        ClientSpriteRegistryCallback.event(TexturedRenderLayers.CHEST_ATLAS_TEXTURE).register((texture, registry) -> {
            registry.register(ExtraChestTypes.IRON.texture);
        });
         */

        /*
        // Crystal Chest Rendering Packets
        ClientPlayNetworking.registerGlobalReceiver(IronChests.UPDATE_INV_PACKET_ID, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            DefaultedList<ItemStack> inv = DefaultedList.ofSize(12, ItemStack.EMPTY);
            for (int i = 0; i < 12; i++) {
                inv.set(i, buf.readItemStack());
            }
            client.execute(() -> {
                CrystalChestBlockEntity blockEntity = (CrystalChestBlockEntity) MinecraftClient.getInstance().world.getBlockEntity(pos);
                blockEntity.setInvStackList(inv);
            });
        });
        */
    }

}
