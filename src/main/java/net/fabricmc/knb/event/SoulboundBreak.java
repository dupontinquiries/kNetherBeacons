package net.fabricmc.knb.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public interface SoulboundBreak {

    Event<SoulboundBreak> EVENT = EventFactory.createArrayBacked(SoulboundBreak.class,
            (listeners) -> (player) -> {
                for (SoulboundBreak listener : listeners) {
                    ActionResult result = listener.interact(player);

                    if(result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    ActionResult interact(PlayerEntity player);

}