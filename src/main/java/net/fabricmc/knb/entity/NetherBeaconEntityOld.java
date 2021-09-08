package net.fabricmc.knb.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.fabricmc.knb.KNB;
import net.fabricmc.knb.blocks.NetherBeaconBlock;
import net.fabricmc.knb.effects.EffectsKNB;
import net.fabricmc.knb.ui.NetherBeaconScreenHandler;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Stainable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.Text.Serializer;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class NetherBeaconEntityOld extends BlockEntity implements NamedScreenHandlerFactory {
    private static final int field_31304 = 4;
    public static final StatusEffect[][] EFFECTS_BY_LEVEL;
    private static final Set<StatusEffect> EFFECTS;
    public static final int field_31300 = 0;
    public static final int field_31301 = 1;
    public static final int field_31302 = 2;
    public static final int field_31303 = 3;
    private static final int field_31305 = 10;
    List<NetherBeaconEntityOld.BeamSegment> beamSegments = Lists.newArrayList();
    private List<NetherBeaconEntityOld.BeamSegment> listOfBeamSegments = Lists.newArrayList();
    int level;
    private int minY;
    @Nullable
    StatusEffect primary;
    @Nullable
    StatusEffect secondary;
    @Nullable
    private Text customName;
    private ContainerLock lock;
    private final PropertyDelegate propertyDelegate;
    Text ownerName;

    public NetherBeaconEntityOld(BlockPos pos, BlockState state) {
        super(KNB.netherBeaconEntityType, pos, state);
        this.lock = ContainerLock.EMPTY;
        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                switch(index) {
                    case 0:
                        return NetherBeaconEntityOld.this.level;
                    case 1:
                        return StatusEffect.getRawId(NetherBeaconEntityOld.this.primary);
                        //return StatusEffect.getRawId(NetherBeaconEntity.this.primary);
                    case 2:
                        return StatusEffect.getRawId(NetherBeaconEntityOld.this.secondary);
                        //return StatusEffect.getRawId(NetherBeaconEntity.this.secondary);
                    default:
                        return 0;
                }
            }

            public void set(int index, int value) {
                switch(index) {
                    case 0:
                        NetherBeaconEntityOld.this.level = value;
                        break;
                    case 1:
                        if (!NetherBeaconEntityOld.this.world.isClient && !NetherBeaconEntityOld.this.beamSegments.isEmpty()) {
                            net.minecraft.block.entity.BeaconBlockEntity.playSound(NetherBeaconEntityOld.this.world, NetherBeaconEntityOld.this.pos, SoundEvents.BLOCK_BEACON_POWER_SELECT);
                        }

                        NetherBeaconEntityOld.this.primary = NetherBeaconEntityOld.getPotionEffectById(value);
                        break;
                    case 2:
                        NetherBeaconEntityOld.this.secondary = NetherBeaconEntityOld.getPotionEffectById(value);
                }

            }

            public int size() {
                return 3;
            }
        };
    }

    public static void tick(World world, BlockPos pos, BlockState state, NetherBeaconEntityOld blockEntity) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        BlockPos blockPos2;
        if (blockEntity.minY < j) {
            blockPos2 = pos;
            blockEntity.listOfBeamSegments = Lists.newArrayList();
            blockEntity.minY = pos.getY() - 1;
        } else {
            blockPos2 = new BlockPos(i, blockEntity.minY + 1, k);
        }

        NetherBeaconEntityOld.BeamSegment beamSegment = blockEntity.listOfBeamSegments.isEmpty() ? null : (NetherBeaconEntityOld.BeamSegment)blockEntity.listOfBeamSegments.get(blockEntity.listOfBeamSegments.size() - 1);
        int l = world.getTopY(Type.WORLD_SURFACE, i, k);

        int n;
        for(n = 0; n < 10 && blockPos2.getY() <= l; ++n) {
            BlockState blockState = world.getBlockState(blockPos2);
            Block block = blockState.getBlock();
            if (block instanceof Stainable) {
                float[] fs = ((Stainable)block).getColor().getColorComponents();
                if (blockEntity.listOfBeamSegments.size() <= 1) {
                    beamSegment = new NetherBeaconEntityOld.BeamSegment(fs);
                    blockEntity.listOfBeamSegments.add(beamSegment);
                } else if (beamSegment != null) {
                    if (Arrays.equals(fs, beamSegment.color)) {
                        beamSegment.increaseHeight();
                    } else {
                        beamSegment = new NetherBeaconEntityOld.BeamSegment(new float[]{(beamSegment.color[0] + fs[0]) / 2.0F, (beamSegment.color[1] + fs[1]) / 2.0F, (beamSegment.color[2] + fs[2]) / 2.0F});
                        blockEntity.listOfBeamSegments.add(beamSegment);
                    }
                }
            } else {
                if (beamSegment == null || blockState.getOpacity(world, blockPos2) >= 15 && !blockState.isOf(Blocks.BEDROCK)) {
                    blockEntity.listOfBeamSegments.clear();
                    blockEntity.minY = l;
                    world.setBlockState(pos, state.with(NetherBeaconBlock.ACTIVE, false));
                    break;
                }

                beamSegment.increaseHeight();
            }

            blockPos2 = blockPos2.up();
            ++blockEntity.minY;
        }

        n = blockEntity.level;
        if (world.getTime() % 80L == 0L) {
            if (!blockEntity.beamSegments.isEmpty()) {
                blockEntity.level = updateLevel(world, i, j, k);
            }

            if (blockEntity.level > 0 && !blockEntity.beamSegments.isEmpty()) {
                //System.out.println("applyingEffects");
                applyPlayerEffects(world, pos, blockEntity.level, blockEntity.primary, blockEntity.secondary);
                playSound(world, pos, SoundEvents.BLOCK_BEACON_AMBIENT);
            }
        }

        if (blockEntity.minY >= l) {
            blockEntity.minY = world.getBottomY() - 1;
            boolean bl = n > 0;
            blockEntity.beamSegments = blockEntity.listOfBeamSegments;
            if (!world.isClient) {
                boolean bl2 = blockEntity.level > 0;
                if (!bl && bl2) {
                    playSound(world, pos, SoundEvents.BLOCK_BEACON_ACTIVATE);
                    // add active texture
                    world.setBlockState(pos, state.with(NetherBeaconBlock.ACTIVE, true));
                    Iterator var17 = world.getNonSpectatingEntities(ServerPlayerEntity.class, (new Box((double)i, (double)j, (double)k, (double)i, (double)(j - 4), (double)k)).expand(10.0D, 5.0D, 10.0D)).iterator();

                    while(var17.hasNext()) {
                        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var17.next();
                        Criteria.CONSTRUCT_BEACON.trigger(serverPlayerEntity, blockEntity.level);
                    }
                } else if (bl && !bl2) {
                    playSound(world, pos, SoundEvents.BLOCK_BEACON_DEACTIVATE);
                    // add deactivated texture
                    world.setBlockState(pos, state.with(NetherBeaconBlock.ACTIVE, false));
                }
            }
        }

    }

    private static int updateLevel(World world, int x, int y, int z) {
        int i = 0;

        for(int j = 1; j <= 4; i = j++) {
            int k = y - j; // int k = y - j;
            // k switch this to k = y + j for upside down effect
            if (k < world.getBottomY()) {
                break;
            }

            boolean bl = true;

            for(int l = x - j; l <= x + j && bl; ++l) {
                for(int m = z - j; m <= z + j; ++m) {
                    if (world.getBlockState(new BlockPos(l, k, m)) != Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState()) { // if (!world.getBlockState(new BlockPos(l, k, m)).isIn(BlockTags.BEACON_BASE_BLOCKS)) {
                        // k make list of blocks that includes polished blackstone bricks
                        bl = false;
                        break;
                    }
                }
            }

            if (!bl) {
                break;
            }
        }

        return i;
    }

    public void markRemoved() {
        playSound(this.world, this.pos, SoundEvents.BLOCK_BEACON_DEACTIVATE);
        super.markRemoved();
    }

    /*
    EFFECTS_BY_LEVEL = new StatusEffect[][]{{StatusEffects.GLOWING, StatusEffects.INVISIBILITY}, {StatusEffects.INSTANT_DAMAGE, StatusEffects.WITHER, StatusEffects.SLOWNESS},
                {StatusEffects.LEVITATION, StatusEffects.SLOW_FALLING, StatusEffects.SATURATION, StatusEffects.WATER_BREATHING, StatusEffects.INSTANT_HEALTH, StatusEffects.HERO_OF_THE_VILLAGE, StatusEffects.STRENGTH}, {StatusEffects.FIRE_RESISTANCE}};
     */

    private static HashSet<StatusEffect> MOB_EFFECTS = new HashSet<StatusEffect>();
    private static HashSet<StatusEffect> PLAYER_EFFECTS = new HashSet<StatusEffect>();
    static {
        MOB_EFFECTS.add(StatusEffects.GLOWING);
        MOB_EFFECTS.add(StatusEffects.INSTANT_DAMAGE);
        MOB_EFFECTS.add(StatusEffects.LEVITATION);
        MOB_EFFECTS.add(StatusEffects.SLOW_FALLING);
        PLAYER_EFFECTS.add(StatusEffects.INVISIBILITY);
        PLAYER_EFFECTS.add(StatusEffects.WITHER);
        PLAYER_EFFECTS.add(StatusEffects.SLOWNESS);
        PLAYER_EFFECTS.add(StatusEffects.LEVITATION);
        PLAYER_EFFECTS.add(StatusEffects.SLOW_FALLING);
        PLAYER_EFFECTS.add(StatusEffects.FIRE_RESISTANCE);
        PLAYER_EFFECTS.add(StatusEffects.SATURATION);
        PLAYER_EFFECTS.add(StatusEffects.WATER_BREATHING);
        PLAYER_EFFECTS.add(StatusEffects.INSTANT_HEALTH);
        PLAYER_EFFECTS.add(StatusEffects.HERO_OF_THE_VILLAGE);
        PLAYER_EFFECTS.add(EffectsKNB.guardianEffect);
    }

    /*
    private static StatusEffect[] MOB_EFFECTS = new StatusEffect[]{StatusEffects.GLOWING, StatusEffects.INSTANT_DAMAGE,
            StatusEffects.LEVITATION, StatusEffects.SLOW_FALLING};
    private static StatusEffect[] PLAYER_EFFECTS = new StatusEffect[]{StatusEffects.INVISIBILITY, StatusEffects.WITHER, StatusEffects.SLOWNESS,
            StatusEffects.LEVITATION, StatusEffects.SLOW_FALLING,
            StatusEffects.FIRE_RESISTANCE, StatusEffects.SATURATION, StatusEffects.WATER_BREATHING,
            StatusEffects.INSTANT_HEALTH, StatusEffects.HERO_OF_THE_VILLAGE, StatusEffects.STRENGTH};
    */

    private static void applyPlayerEffects(World world, BlockPos pos, int beaconLevel, @Nullable StatusEffect primaryEffect, @Nullable StatusEffect secondaryEffect) {
        if (!world.isClient) {
            double d = (double)(beaconLevel * 10 + 10);
            int i = 0;
            if (beaconLevel >= 4 && primaryEffect == secondaryEffect) {
                i = 1;
            }

            int j = (7 + beaconLevel * 2) * 20;
            Box box = (new Box(pos)).expand(d).stretch(0.0D, (double)world.getHeight(), 0.0D);

            // apply primary effect
            if (primaryEffect != null) {
                List<PlayerEntity> list = world.getNonSpectatingEntities(PlayerEntity.class, box);
                Iterator var11 = list.iterator();

                PlayerEntity playerEntity2;
                while(var11.hasNext()) {
                    playerEntity2 = (PlayerEntity)var11.next();
                    playerEntity2.addStatusEffect(new StatusEffectInstance(primaryEffect, j, i, true, true));
                }
            }

            //if (beaconLevel >= 2 && primaryEffect != secondaryEffect && secondaryEffect != null) {
            if (secondaryEffect != null) {
                // against mobs
                if (MOB_EFFECTS.contains(secondaryEffect)) {
                    List<HostileEntity> list2 = world.getNonSpectatingEntities(HostileEntity.class, box);
                    Iterator var22 = list2.iterator();
                    HostileEntity m;
                    while(var22.hasNext()) {
                        m = (HostileEntity)var22.next();
                        m.addStatusEffect(new StatusEffectInstance(secondaryEffect, j, 0, true, true));
                    }
                }
                // against players
                if (PLAYER_EFFECTS.contains(secondaryEffect)) {
                    List<PlayerEntity> list2 = world.getNonSpectatingEntities(PlayerEntity.class, box);
                    Iterator var22 = list2.iterator();
                    PlayerEntity p;
                    while(var22.hasNext()) {
                        p = (PlayerEntity)var22.next();
                        int amp = 0;
                        p.addStatusEffect(new StatusEffectInstance(secondaryEffect, j, amp, true, true));
                    }
                }
            }
        }
    }

    public static void playSound(World world, BlockPos pos, SoundEvent sound) {
        world.playSound((PlayerEntity)null, pos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    public List<NetherBeaconEntityOld.BeamSegment> getBeamSegments() {
        return (List)(this.level == 0 ? ImmutableList.of() : this.beamSegments);
    }

    @Nullable
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return new BlockEntityUpdateS2CPacket(this.pos, 3, this.toInitialChunkDataNbt());
    }

    public NbtCompound toInitialChunkDataNbt() {
        return this.writeNbt(new NbtCompound());
    }

    @Nullable
    static StatusEffect getPotionEffectById(int id) {
        StatusEffect statusEffect = StatusEffect.byRawId(id);
        return EFFECTS.contains(statusEffect) ? statusEffect : null;
    }

    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.primary = getPotionEffectById(nbt.getInt("Primary"));
        this.secondary = getPotionEffectById(nbt.getInt("Secondary"));
        if (nbt.contains("CustomName", 8)) {
            this.customName = Serializer.fromJson(nbt.getString("CustomName"));
        }
        if (nbt.contains("OwnerName", 8)) {
            this.ownerName = Serializer.fromJson(nbt.getString("OwnerName"));
        }

        this.lock = ContainerLock.fromNbt(nbt);
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("Primary", StatusEffect.getRawId(this.primary));
        nbt.putInt("Secondary", StatusEffect.getRawId(this.secondary));
        nbt.putInt("Levels", this.level);
        if (this.customName != null) {
            nbt.putString("CustomName", Serializer.toJson(this.customName));
        }

        if (this.ownerName != null) {
            nbt.putString("OwnerName", Serializer.toJson(this.ownerName));
        }

        this.lock.writeNbt(nbt);
        return nbt;
    }

    public void setCustomName(@Nullable Text customName) {
        this.customName = customName;
    }

    @Nullable
    public ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return LockableContainerBlockEntity.checkUnlocked(playerEntity, this.lock, this.getDisplayName()) ?
                new NetherBeaconScreenHandler(i, playerInventory, this.propertyDelegate, ScreenHandlerContext.create(this.world, this.getPos())) : null; // need screenhandler
        // log -> Block entity minecraft:beacon @ BlockPos{x=-16, y=72, z=-200} state Block{knb:nether_beacon} invalid for ticking:
    }

    public Text getDisplayName() {
        return (Text)(this.customName != null ? this.customName : new TranslatableText("container.beacon"));
    }

    public void setWorld(World world) {
        super.setWorld(world);
        this.minY = world.getBottomY() - 1;
    }

    static {
        //EFFECTS_BY_LEVEL = new StatusEffect[][]{{StatusEffects.SPEED, StatusEffects.HASTE}, {StatusEffects.RESISTANCE, StatusEffects.JUMP_BOOST}, {StatusEffects.STRENGTH}, {StatusEffects.REGENERATION}};
        //EFFECTS_BY_LEVEL = new StatusEffect[][]{{StatusEffects.HERO_OF_THE_VILLAGE, StatusEffects.INSTANT_DAMAGE}, {StatusEffects.INVISIBILITY, StatusEffects.WITHER},
        //        {StatusEffects.LEVITATION}, {StatusEffects.FIRE_RESISTANCE}};
        EFFECTS_BY_LEVEL = new StatusEffect[][]{{StatusEffects.GLOWING, StatusEffects.INVISIBILITY, StatusEffects.SLOWNESS}, {StatusEffects.WITHER, StatusEffects.LEVITATION, StatusEffects.SLOW_FALLING},
                {StatusEffects.INSTANT_DAMAGE, StatusEffects.SATURATION, StatusEffects.WATER_BREATHING, EffectsKNB.guardianEffect}, {StatusEffects.FIRE_RESISTANCE}};
        // EFFECTS_BY_LEVEL = new StatusEffect[][]{{StatusEffects.SPEED, StatusEffects.HASTE}, {StatusEffects.RESISTANCE, StatusEffects.JUMP_BOOST}, {StatusEffects.STRENGTH}, {StatusEffects.REGENERATION}};
        // k swap out effects
        EFFECTS = (Set)Arrays.stream(EFFECTS_BY_LEVEL).flatMap(Arrays::stream).collect(Collectors.toSet());
    }

    public void setOwner(Text name) {
        this.ownerName = name;
    }

    public static class BeamSegment {
        final float[] color;
        private int height;

        public BeamSegment(float[] color) {
            this.color = color;
            this.height = 1;
        }

        protected void increaseHeight() {
            ++this.height;
        }

        public float[] getColor() {
            return this.color;
        }

        public int getHeight() {
            return this.height;
        }
    }
}