package net.fabricmc.knb.entity;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.stream.Collectors;

import net.fabricmc.knb.KNB;
import net.fabricmc.knb.blocks.NetherBeaconBlock;
import net.fabricmc.knb.effects.EffectsKNB;
import net.fabricmc.knb.ui.NetherBeaconScreenHandler;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Stainable;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
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
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.text.Text.Serializer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.Heightmap.Type;
import org.jetbrains.annotations.Nullable;

public class NetherBeaconEntity extends BlockEntity implements NamedScreenHandlerFactory {
    private static final int field_31304 = 4;
    public static final StatusEffect[][] EFFECTS_BY_LEVEL;
    private static final Set<StatusEffect> EFFECTS;
    public static final int field_31300 = 0;
    public static final int field_31301 = 1;
    public static final int field_31302 = 2;
    public static final int field_31303 = 3;
    private static final int field_31305 = 10;
    List<NetherBeaconEntity.BeamSegment> beamSegments = Lists.newArrayList();
    private List<NetherBeaconEntity.BeamSegment> listOfBeamSegments = Lists.newArrayList();
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
    private String ownerName;

    public NetherBeaconEntity(BlockPos pos, BlockState state) {
        super(KNB.netherBeaconEntityType, pos, state);
        this.lock = ContainerLock.EMPTY;
        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                switch(index) {
                    case 0:
                        return NetherBeaconEntity.this.level;
                    case 1:
//                        return EFFECT_MAP.indexOf(NetherBeaconEntity.this.primary);
                        return StatusEffect.getRawId(NetherBeaconEntity.this.primary);
                        //return StatusEffect.getRawId(NetherBeaconEntity.this.primary);
                    case 2:
//                        return EFFECT_MAP.indexOf(NetherBeaconEntity.this.secondary);
                        return StatusEffect.getRawId(NetherBeaconEntity.this.secondary);
                        //return StatusEffect.getRawId(NetherBeaconEntity.this.secondary);
                    default:
                        return 0;
                }
            }

            public void set(int index, int value) {
                switch(index) {
                    case 0:
                        NetherBeaconEntity.this.level = value;
                        break;
                    case 1:
                        if (!NetherBeaconEntity.this.world.isClient && !NetherBeaconEntity.this.beamSegments.isEmpty()) {
                            net.minecraft.block.entity.BeaconBlockEntity.playSound(NetherBeaconEntity.this.world, NetherBeaconEntity.this.pos, SoundEvents.BLOCK_BEACON_POWER_SELECT);
                        }

                        NetherBeaconEntity.this.primary = EFFECT_MAP.get(value);
//                        NetherBeaconEntity.this.primary = NetherBeaconEntity.getPotionEffectById(value);
                        break;
                    case 2:
                        NetherBeaconEntity.this.secondary = EFFECT_MAP.get(value);
//                        NetherBeaconEntity.this.secondary = NetherBeaconEntity.getPotionEffectById(value);
                }

            }

            public int size() {
                return 3;
            }
        };
    }

    public static void tick(World world, BlockPos pos, BlockState state, NetherBeaconEntity blockEntity) {
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

        NetherBeaconEntity.BeamSegment beamSegment = blockEntity.listOfBeamSegments.isEmpty() ? null : (NetherBeaconEntity.BeamSegment)blockEntity.listOfBeamSegments.get(blockEntity.listOfBeamSegments.size() - 1);
        int l = world.getTopY(Type.WORLD_SURFACE, i, k);

        int n;
        for(n = 0; n < 10 && blockPos2.getY() <= l; ++n) {
            BlockState blockState = world.getBlockState(blockPos2);
            Block block = blockState.getBlock();
            if (block instanceof Stainable) {
                float[] fs = ((Stainable)block).getColor().getColorComponents();
                if (blockEntity.listOfBeamSegments.size() <= 1) {
                    beamSegment = new NetherBeaconEntity.BeamSegment(fs);
                    blockEntity.listOfBeamSegments.add(beamSegment);
                } else if (beamSegment != null) {
                    if (Arrays.equals(fs, beamSegment.color)) {
                        beamSegment.increaseHeight();
                    } else {
                        beamSegment = new NetherBeaconEntity.BeamSegment(new float[]{(beamSegment.color[0] + fs[0]) / 2.0F, (beamSegment.color[1] + fs[1]) / 2.0F, (beamSegment.color[2] + fs[2]) / 2.0F});
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
                applyPlayerEffects(world, pos, blockEntity.level, blockEntity.primary, blockEntity.secondary, blockEntity.ownerName);
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
    public static Vector<StatusEffect> EFFECT_MAP = new Vector<>(15);
    private static HashSet<StatusEffect> BENEFICIAL_EFFECTS = new HashSet<StatusEffect>();
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

        EFFECT_MAP.add(StatusEffects.GLOWING);
        EFFECT_MAP.add(StatusEffects.INSTANT_DAMAGE);
        EFFECT_MAP.add(StatusEffects.LEVITATION);
        EFFECT_MAP.add(StatusEffects.SLOW_FALLING);
        EFFECT_MAP.add(StatusEffects.INVISIBILITY);
        EFFECT_MAP.add(StatusEffects.WITHER);
        EFFECT_MAP.add(StatusEffects.SLOWNESS);
        EFFECT_MAP.add(StatusEffects.LEVITATION);
        EFFECT_MAP.add(StatusEffects.SLOW_FALLING);
        EFFECT_MAP.add(StatusEffects.FIRE_RESISTANCE);
        EFFECT_MAP.add(StatusEffects.SATURATION);
        EFFECT_MAP.add(StatusEffects.WATER_BREATHING);
        EFFECT_MAP.add(StatusEffects.INSTANT_HEALTH);
        EFFECT_MAP.add(StatusEffects.HERO_OF_THE_VILLAGE);
        EFFECT_MAP.add(EffectsKNB.guardianEffect);
    }

    /*
    private static StatusEffect[] MOB_EFFECTS = new StatusEffect[]{StatusEffects.GLOWING, StatusEffects.INSTANT_DAMAGE,
            StatusEffects.LEVITATION, StatusEffects.SLOW_FALLING};
    private static StatusEffect[] PLAYER_EFFECTS = new StatusEffect[]{StatusEffects.INVISIBILITY, StatusEffects.WITHER, StatusEffects.SLOWNESS,
            StatusEffects.LEVITATION, StatusEffects.SLOW_FALLING,
            StatusEffects.FIRE_RESISTANCE, StatusEffects.SATURATION, StatusEffects.WATER_BREATHING,
            StatusEffects.INSTANT_HEALTH, StatusEffects.HERO_OF_THE_VILLAGE, StatusEffects.STRENGTH};
    */

    private static void applyPlayerEffects(World world, BlockPos pos, int beaconLevel, @Nullable StatusEffect primaryEffect, @Nullable StatusEffect secondaryEffect, String ownerName) {
        if (!world.isClient) {
            System.out.println(" * applying beacon effects");
            double d = (double)(beaconLevel * 10 + 15);
            int i = 0;
            if (beaconLevel >= 4 && primaryEffect == secondaryEffect) {
                i = 1;
            }

            int j = (7 + beaconLevel * 2) * 20;
            Box box = (new Box(pos)).expand(d).stretch(0.0D, (double)world.getHeight(), 0.0D);

            if (primaryEffect != null) {

                if (primaryEffect.isBeneficial()) { //secondaryEffect.isBeneficial()
                    System.out.println(" (a) effect is beneficial");
                    List<PlayerEntity> playerEntities = world.getNonSpectatingEntities(PlayerEntity.class, box);
                    Iterator iter = playerEntities.iterator();
                    PlayerEntity player;
                    while(iter.hasNext()) {
                        player = (PlayerEntity)iter.next();
                        if ( player.getName().asString() == ownerName ) {
                            System.out.println(" *** applied beacon effects to owner: " + ownerName);
                            player.addStatusEffect(new StatusEffectInstance(primaryEffect, j, i, true, true));
                            break;
                        }
                    }
                } else {
                    System.out.println(" (b) effect is not beneficial");
                    List<LivingEntity> livingEntities = world.getNonSpectatingEntities(LivingEntity.class, box);
                    Iterator iter = livingEntities.iterator();
                    LivingEntity le;
                    while(iter.hasNext()) {
                        le = (LivingEntity)iter.next();
                        if ( !(le instanceof PlayerEntity && ((PlayerEntity)le).getName().asString() == ownerName) ) {
                            le.addStatusEffect(new StatusEffectInstance(primaryEffect, j, i, true, true));
                        }
                    }
                    System.out.println(" *** applied beacon effects to other mobs");
                }

                /*
                System.out.println(" ddd primary effect is NOT null! (" + primaryEffect.getTranslationKey() + ")");
                System.out.println(" zzz owner name is: " + ownerName);
                List<PlayerEntity> playerEntities = world.getNonSpectatingEntities(PlayerEntity.class, box);
                Iterator iter = playerEntities.iterator();
                PlayerEntity player;
                while(iter.hasNext()) {
                    player = (PlayerEntity)iter.next();
                    if ( player.getName().asString() == ownerName ) {
                        System.out.println(" *** applied beacon effects to owner");
                        player.addStatusEffect(new StatusEffectInstance(primaryEffect, j, 0, true, true));
                        break;
                    }
                }
                */
            } else {
                System.out.println(" ddd primary effect is null!");
            }

            if (secondaryEffect != null) {
                System.out.println(" ddd secondary effect is NOT null! (" + secondaryEffect.getTranslationKey() + ")");
                System.out.println(" zzz owner name is: " + ownerName);
                if (secondaryEffect.isBeneficial()) { //secondaryEffect.isBeneficial()
                    System.out.println(" (a) effect is beneficial");
                    List<PlayerEntity> playerEntities = world.getNonSpectatingEntities(PlayerEntity.class, box);
                    Iterator iter = playerEntities.iterator();
                    PlayerEntity player;
                    while(iter.hasNext()) {
                        player = (PlayerEntity)iter.next();
                        if ( player.getName().asString() == ownerName ) {
                            System.out.println(" *** applied beacon effects to owner");
                            player.addStatusEffect(new StatusEffectInstance(secondaryEffect, j, i, true, true));
                            break;
                        }
                    }
                } else {
                    System.out.println(" (b) effect is not beneficial");
                    List<LivingEntity> livingEntities = world.getNonSpectatingEntities(LivingEntity.class, box);
                    Iterator iter = livingEntities.iterator();
                    LivingEntity le;
                    while(iter.hasNext()) {
                        le = (LivingEntity)iter.next();
                        if ( !(le instanceof PlayerEntity && ((PlayerEntity)le).getName().asString() == ownerName) ) {
                            le.addStatusEffect(new StatusEffectInstance(secondaryEffect, j, i, true, true));
                        }
                    }
                    System.out.println(" *** applied beacon effects to other mobs");
                }
            } else {
                System.out.println(" ddd secondary effect is null!");
            }
        }
    }

    public static void playSound(World world, BlockPos pos, SoundEvent sound) {
        world.playSound((PlayerEntity)null, pos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    public List<NetherBeaconEntity.BeamSegment> getBeamSegments() {
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
        return statusEffect;
        //return EFFECTS.contains(statusEffect) ? statusEffect : null;
    }

    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
//        this.primary = getPotionEffectById(nbt.getInt("ePrimary"));
//        this.secondary = getPotionEffectById(nbt.getInt("eSecondary"));
        // get effects
        int pKey = nbt.getInt("ePrimary"), sKey = nbt.getInt("eSecondary");
        this.primary = EFFECT_MAP.get(pKey);
        this.secondary = EFFECT_MAP.get(sKey);
        // get level
        this.level = nbt.getInt("Levels");
        // get custom name
        if (nbt.contains("CustomName", 8)) {
            this.customName = Serializer.fromJson(nbt.getString("CustomName"));
        }
        // get owner
        if (nbt.contains("kOwnerName", 8)) {
            this.ownerName = nbt.getString("kOwnerName");
        } else {
            this.ownerName = "none";
        }
        System.out.println("ownerName from nbt: " + this.ownerName);
        this.lock = ContainerLock.fromNbt(nbt);
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
//        nbt.putInt("ePrimary", EFFECT_MAP.indexOf(primary));
//        nbt.putInt("eSecondary", EFFECT_MAP.indexOf(secondary));
        // save effects
        nbt.putInt("Primary", StatusEffect.getRawId(this.primary));
        nbt.putInt("Secondary", StatusEffect.getRawId(this.secondary));
        nbt.putInt("ePrimary", EFFECT_MAP.indexOf(primary));
        nbt.putInt("eSecondary", EFFECT_MAP.indexOf(secondary));
        // save level
        nbt.putInt("Levels", this.level);
        if (this.customName != null) {
            nbt.putString("CustomName", Serializer.toJson(this.customName));
        }
        // store owner name
        if (this.ownerName != null) {
            nbt.putString("kOwnerName", this.ownerName);
        } else {
            nbt.putString("kOwnerName", "none");
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
        EFFECTS_BY_LEVEL = new StatusEffect[][]{{StatusEffects.GLOWING, StatusEffects.INVISIBILITY, StatusEffects.SLOWNESS, StatusEffects.MINING_FATIGUE}, {StatusEffects.WITHER, StatusEffects.LEVITATION, StatusEffects.SLOW_FALLING, StatusEffects.HUNGER},
                {StatusEffects.INSTANT_HEALTH, StatusEffects.SATURATION, StatusEffects.WATER_BREATHING, EffectsKNB.guardianEffect}, {StatusEffects.FIRE_RESISTANCE}};
        // EFFECTS_BY_LEVEL = new StatusEffect[][]{{StatusEffects.SPEED, StatusEffects.HASTE}, {StatusEffects.RESISTANCE, StatusEffects.JUMP_BOOST}, {StatusEffects.STRENGTH}, {StatusEffects.REGENERATION}};
        // k swap out effects
        EFFECTS = (Set)Arrays.stream(EFFECTS_BY_LEVEL).flatMap(Arrays::stream).collect(Collectors.toSet());
    }

    public void setOwner(String name) {
        this.ownerName = name;
        //markDirty();
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