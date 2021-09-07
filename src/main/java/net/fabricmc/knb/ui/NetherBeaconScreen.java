package net.fabricmc.knb.ui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.knb.KNB;
import net.fabricmc.knb.entity.NetherBeaconEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.ingame.BeaconScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.UpdateBeaconC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.screen.BeaconScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class NetherBeaconScreen extends HandledScreen<NetherBeaconScreenHandler> {
    static final Identifier TEXTURE = new Identifier(KNB.modName, "textures/gui/container/nether_beacon.png");
    private static final Text PRIMARY_POWER_TEXT = new TranslatableText("block.minecraft.beacon.primary");
    private static final Text SECONDARY_POWER_TEXT = new TranslatableText("block.minecraft.beacon.secondary");
    private final List<NetherBeaconScreen.BeaconButtonWidget> buttons = Lists.newArrayList();
    @Nullable
    StatusEffect primaryEffect;
    @Nullable
    StatusEffect secondaryEffect;

    public NetherBeaconScreen(NetherBeaconScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 230;
        this.backgroundHeight = 219;
        handler.addListener(new ScreenHandlerListener() {
            public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
            }

            public void onPropertyUpdate(ScreenHandler handler, int property, int value) {
                NetherBeaconScreen.this.primaryEffect = ((NetherBeaconScreenHandler)handler).getPrimaryEffect();
                NetherBeaconScreen.this.secondaryEffect = ((NetherBeaconScreenHandler)handler).getSecondaryEffect();
            }
        });
    }

    private <T extends ClickableWidget & NetherBeaconScreen.BeaconButtonWidget> void addButton(T button) {
        this.addDrawableChild(button);
        this.buttons.add((NetherBeaconScreen.BeaconButtonWidget)button);
    }

    protected void init() {
        super.init();
        this.buttons.clear();
        this.addButton(new NetherBeaconScreen.DoneButtonWidget(this.x + 164, this.y + 107));
        this.addButton(new NetherBeaconScreen.CancelButtonWidget(this.x + 190, this.y + 107));

        int n;
        int o;
        int p;
        StatusEffect statusEffect2;
        NetherBeaconScreen.EffectButtonWidget effectButtonWidget2;
        for(int i = 0; i <= 2; ++i) {
            n = NetherBeaconEntity.EFFECTS_BY_LEVEL[i].length;
            o = n * 22 + (n - 1) * 2;

            for(p = 0; p < n; ++p) {
                statusEffect2 = NetherBeaconEntity.EFFECTS_BY_LEVEL[i][p];
                effectButtonWidget2 = new NetherBeaconScreen.EffectButtonWidget(this.x + 76 + p * 24 - o / 2, this.y + 22 + i * 25, statusEffect2, true, i);
                effectButtonWidget2.active = false;
                this.addButton(effectButtonWidget2);
            }
        }

        //int m = true;
        n = NetherBeaconEntity.EFFECTS_BY_LEVEL[3].length + 1;
        o = n * 22 + (n - 1) * 2;

        for(p = 0; p < n - 1; ++p) {
            statusEffect2 = NetherBeaconEntity.EFFECTS_BY_LEVEL[3][p];
            effectButtonWidget2 = new NetherBeaconScreen.EffectButtonWidget(this.x + 167 + p * 24 - o / 2, this.y + 47, statusEffect2, false, 3);
            effectButtonWidget2.active = false;
            this.addButton(effectButtonWidget2);
        }

        NetherBeaconScreen.EffectButtonWidget effectButtonWidget3 = new NetherBeaconScreen.LevelTwoEffectButtonWidget(this.x + 167 + (n - 1) * 24 - o / 2, this.y + 47, NetherBeaconEntity.EFFECTS_BY_LEVEL[0][0]);
        effectButtonWidget3.visible = false;
        this.addButton(effectButtonWidget3);
    }

    public void handledScreenTick() {
        super.handledScreenTick();
        this.tickButtons();
    }

    void tickButtons() {
        int i = ((NetherBeaconScreenHandler)this.handler).getProperties();
        this.buttons.forEach((button) -> {
            button.tick(i);
        });
    }

    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        drawCenteredText(matrices, this.textRenderer, PRIMARY_POWER_TEXT, 62, 10, 14737632);
        drawCenteredText(matrices, this.textRenderer, SECONDARY_POWER_TEXT, 169, 10, 14737632);
        Iterator var4 = this.buttons.iterator();

        while(var4.hasNext()) {
            NetherBeaconScreen.BeaconButtonWidget beaconButtonWidget = (NetherBeaconScreen.BeaconButtonWidget)var4.next();
            if (beaconButtonWidget.shouldRenderTooltip()) {
                beaconButtonWidget.renderTooltip(matrices, mouseX - this.x, mouseY - this.y);
                break;
            }
        }

    }

    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
        this.itemRenderer.zOffset = 100.0F;
        /*
        this.itemRenderer.renderInGuiWithOverrides(new ItemStack(Items.NETHERITE_INGOT), i + 20, j + 109);
        this.itemRenderer.renderInGuiWithOverrides(new ItemStack(Items.EMERALD), i + 41, j + 109);
        this.itemRenderer.renderInGuiWithOverrides(new ItemStack(Items.DIAMOND), i + 41 + 22, j + 109);
        this.itemRenderer.renderInGuiWithOverrides(new ItemStack(Items.GOLD_INGOT), i + 42 + 44, j + 109);
         */
        this.itemRenderer.renderInGuiWithOverrides(new ItemStack(Items.GOLD_INGOT), i + 42 + 66, j + 109); // k changed to wither skull
        this.itemRenderer.zOffset = 0.0F;
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Environment(EnvType.CLIENT)
    interface BeaconButtonWidget {
        boolean shouldRenderTooltip();

        void renderTooltip(MatrixStack matrices, int mouseX, int mouseY);

        void tick(int level);
    }

    @Environment(EnvType.CLIENT)
    class DoneButtonWidget extends NetherBeaconScreen.IconButtonWidget {
        public DoneButtonWidget(int x, int y) {
            super(x, y, 90, 220, ScreenTexts.DONE);
        }

        public void onPress() {
            //NetherBeaconScreen.this.client.getNetworkHandler().sendPacket(new UpdateBeaconC2SPacket(StatusEffect.getRawId(NetherBeaconScreen.this.primaryEffect), StatusEffect.getRawId(NetherBeaconScreen.this.secondaryEffect)));\
            //HashMap<String, Integer> m = new HashMap<>();
            //NbtCompound nbt = new NbtCompound();
            //nbt.putInt("Primary", StatusEffect.getRawId(NetherBeaconScreen.this.primaryEffect));
            //nbt.putInt("Secondary", StatusEffect.getRawId(NetherBeaconScreen.this.secondaryEffect));
            //NetherBeaconScreen.this.client.getNetworkHandler().sendPacket( new BlockEntityUpdateS2CPacket( PacketByteBufs.create().setInt(1, StatusEffect.getRawId(NetherBeaconScreen.this.primaryEffect) ).setInt(2, StatusEffect.getRawId(NetherBeaconScreen.this.secondaryEffect) ) ) );
            //NetherBeaconScreen.this.client.getNetworkHandler().sendPacket( new BlockEntityUpdateS2CPacket( PacketByteBufs.create().setInt(1, StatusEffect.getRawId(NetherBeaconScreen.this.primaryEffect) ).setInt(2, StatusEffect.getRawId(NetherBeaconScreen.this.secondaryEffect) ) ) );
            //NetherBeaconScreen.this.client.getNetworkHandler().sendPacket( new BlockEntityUpdateS2CPacket( PacketByteBufs.copy(PacketByteBuf.getVarIntLength(2)).writeNbt( nbt ) ) );
            //NetherBeaconScreen.this.client.getNetworkHandler().sendPacket( new BlockUpdateS2CPacket( PacketByteBufs.create().writeNbt( nbt ) ) );
            //m.put("primary", StatusEffect.getRawId(NetherBeaconScreen.this.primaryEffect));
            //m.put("secondary", StatusEffect.getRawId(NetherBeaconScreen.this.secondaryEffect));
            //NetherBeaconScreen.this.client.getNetworkHandler().sendPacket(new BlockUpdateS2CPacket(PacketByteBufs.create()writeIntArray(new int[]{StatusEffect.getRawId(NetherBeaconScreen.this.primaryEffect), StatusEffect.getRawId(NetherBeaconScreen.this.secondaryEffect)})));
            //NetherBeaconScreen.this.client.getNetworkHandler().sendPacket(new BlockUpdateS2CPacket(PacketByteBufs.create()writeIntArray(new int[]{StatusEffect.getRawId(NetherBeaconScreen.this.primaryEffect), StatusEffect.getRawId(NetherBeaconScreen.this.secondaryEffect)})));
            NetherBeaconScreen.this.client.getNetworkHandler().sendPacket(new UpdateBeaconC2SPacket(StatusEffect.getRawId(NetherBeaconScreen.this.primaryEffect), StatusEffect.getRawId(NetherBeaconScreen.this.secondaryEffect)));
            NetherBeaconScreen.this.client.player.closeHandledScreen();
        }

        public void tick(int level) {
            this.active = ((NetherBeaconScreenHandler) NetherBeaconScreen.this.handler).hasPayment() && NetherBeaconScreen.this.primaryEffect != null;
        }
    }

    @Environment(EnvType.CLIENT)
    class CancelButtonWidget extends NetherBeaconScreen.IconButtonWidget {
        public CancelButtonWidget(int x, int y) {
            super(x, y, 112, 220, ScreenTexts.CANCEL);
        }

        public void onPress() {
            NetherBeaconScreen.this.client.player.closeHandledScreen();
        }

        public void tick(int level) {
        }
    }

    @Environment(EnvType.CLIENT)
    private class EffectButtonWidget extends NetherBeaconScreen.BaseButtonWidget {
        private final boolean primary;
        protected final int level;
        private StatusEffect effect;
        private Sprite sprite;
        private Text tooltip;

        public EffectButtonWidget(int x, int y, StatusEffect statusEffect, boolean primary, int level) {
            super(x, y);
            this.primary = primary;
            this.level = level;
            this.init(statusEffect);
        }

        protected void init(StatusEffect statusEffect) {
            this.effect = statusEffect;
            this.sprite = MinecraftClient.getInstance().getStatusEffectSpriteManager().getSprite(statusEffect);
            this.tooltip = this.getEffectName(statusEffect);
        }

        protected MutableText getEffectName(StatusEffect statusEffect) {
            return new TranslatableText(statusEffect.getTranslationKey());
        }

        public void onPress() {
            if (!this.isDisabled()) {
                if (this.primary) {
                    NetherBeaconScreen.this.primaryEffect = this.effect;
                } else {
                    NetherBeaconScreen.this.secondaryEffect = this.effect;
                }

                NetherBeaconScreen.this.tickButtons();
            }
        }

        public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
            NetherBeaconScreen.this.renderTooltip(matrices, this.tooltip, mouseX, mouseY);
        }

        protected void renderExtra(MatrixStack matrices) {
            RenderSystem.setShaderTexture(0, this.sprite.getAtlas().getId());
            drawSprite(matrices, this.x + 2, this.y + 2, this.getZOffset(), 18, 18, this.sprite);
        }

        public void tick(int level) {
            this.active = this.level < level;
            this.setDisabled(this.effect == (this.primary ? NetherBeaconScreen.this.primaryEffect : NetherBeaconScreen.this.secondaryEffect));
        }

        protected MutableText getNarrationMessage() {
            return this.getEffectName(this.effect);
        }
    }

    @Environment(EnvType.CLIENT)
    private class LevelTwoEffectButtonWidget extends NetherBeaconScreen.EffectButtonWidget {
        public LevelTwoEffectButtonWidget(int x, int y, StatusEffect statusEffect) {
            super(x, y, statusEffect, false, 3);
        }

        protected MutableText getEffectName(StatusEffect statusEffect) {
            return (new TranslatableText(statusEffect.getTranslationKey())).append(" II");
        }

        public void tick(int level) {
            if (NetherBeaconScreen.this.primaryEffect != null) {
                this.visible = true;
                this.init(NetherBeaconScreen.this.primaryEffect);
                super.tick(level);
            } else {
                this.visible = false;
            }

        }
    }

    @Environment(EnvType.CLIENT)
    abstract class IconButtonWidget extends NetherBeaconScreen.BaseButtonWidget {
        private final int u;
        private final int v;

        protected IconButtonWidget(int x, int y, int u, int v, Text message) {
            super(x, y, message);
            this.u = u;
            this.v = v;
        }

        protected void renderExtra(MatrixStack matrices) {
            this.drawTexture(matrices, this.x + 2, this.y + 2, this.u, this.v, 18, 18);
        }

        public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
            NetherBeaconScreen.this.renderTooltip(matrices, NetherBeaconScreen.this.title, mouseX, mouseY);
        }
    }

    @Environment(EnvType.CLIENT)
    abstract static class BaseButtonWidget extends PressableWidget implements NetherBeaconScreen.BeaconButtonWidget {
        private boolean disabled;

        protected BaseButtonWidget(int x, int y) {
            super(x, y, 22, 22, LiteralText.EMPTY);
        }

        protected BaseButtonWidget(int x, int y, Text message) {
            super(x, y, 22, 22, message);
        }

        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, NetherBeaconScreen.TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            //int i = true;
            int j = 0;
            if (!this.active) {
                j += this.width * 2;
            } else if (this.disabled) {
                j += this.width * 1;
            } else if (this.isHovered()) {
                j += this.width * 3;
            }

            this.drawTexture(matrices, this.x, this.y, j, 219, this.width, this.height);
            this.renderExtra(matrices);
        }

        protected abstract void renderExtra(MatrixStack matrices);

        public boolean isDisabled() {
            return this.disabled;
        }

        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }

        public boolean shouldRenderTooltip() {
            return this.hovered;
        }

        public void appendNarrations(NarrationMessageBuilder builder) {
            this.appendDefaultNarrations(builder);
        }
    }
}
