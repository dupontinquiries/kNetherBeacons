package net.fabricmc.knb.disc;

import net.fabricmc.knb.KNB;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Rarity;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.item.ItemGroup;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;

public class MusicDiskKNB extends MusicDiscItem {
    public MusicDiskKNB(SoundEvent se) {
        super(0, (net.minecraft.sound.SoundEvent) se,
                new FabricItemSettings().group(ItemGroup.MISC).maxCount(1).rarity(Rarity.RARE));
    }
}
