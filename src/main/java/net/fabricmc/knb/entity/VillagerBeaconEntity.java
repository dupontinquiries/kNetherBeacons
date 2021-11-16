package net.fabricmc.knb.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.fabricmc.knb.KNB;
import net.fabricmc.knb.blocks.NetherBeaconBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.Text.Serializer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class VillagerBeaconEntity extends BlockEntity {
    private static final int field_31304 = 4;
    public static final int field_31300 = 0;
    public static final int field_31301 = 1;
    public static final int field_31302 = 2;
    public static final int field_31303 = 3;
    private static final int field_31305 = 10;
    List<VillagerBeaconEntity.BeamSegment> beamSegments = Lists.newArrayList();
    private List<VillagerBeaconEntity.BeamSegment> listOfBeamSegments = Lists.newArrayList();
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

    public VillagerBeaconEntity(BlockPos pos, BlockState state) {
        super(KNB.villagerBeaconEntityType, pos, state);
        this.lock = ContainerLock.EMPTY;
        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                switch(index) {
                    case 0:
                        return VillagerBeaconEntity.this.level;
                    case 1:
                        return StatusEffect.getRawId(VillagerBeaconEntity.this.primary);
                    case 2:
                        return StatusEffect.getRawId(VillagerBeaconEntity.this.secondary);
                    default:
                        return 0;
                }
            }

            public void set(int index, int value) {
                switch(index) {
                    case 0:
                        VillagerBeaconEntity.this.level = value;
                        break;
                    case 1:
                        if (!VillagerBeaconEntity.this.world.isClient && !VillagerBeaconEntity.this.beamSegments.isEmpty()) {
                            BeaconBlockEntity.playSound(VillagerBeaconEntity.this.world, VillagerBeaconEntity.this.pos, SoundEvents.BLOCK_BEACON_POWER_SELECT);
                        }

                        VillagerBeaconEntity.this.primary = VillagerBeaconEntity.getPotionEffectById(value);
                        break;
                    case 2:
                        VillagerBeaconEntity.this.secondary = VillagerBeaconEntity.getPotionEffectById(value);
                }

            }

            public int size() {
                return 3;
            }
        };
    }

    public static void tick(World world, BlockPos pos, BlockState state, VillagerBeaconEntity blockEntity) {
        if (!world.isClient && blockEntity.primary == null) {
            Random r = new Random();
            blockEntity.primary = VILLAGER_EFFECTS.get(r.nextInt(VILLAGER_EFFECTS.size()));
        }
        if (world.getTime() % 100L == 0L) {
//            if (world.getTime() % 80L == 0L) {
            world.setBlockState(pos, state.with(NetherBeaconBlock.ACTIVE, true));
            applyPlayerEffects(world, pos, blockEntity.level, blockEntity.primary);
            playSound(world, pos, SoundEvents.BLOCK_BEACON_AMBIENT);
        }
    }
//        applyPlayerEffects(world, pos, blockEntity.level, blockEntity.primary);
        /*
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

        VillagerBeaconEntity.BeamSegment beamSegment = blockEntity.listOfBeamSegments.isEmpty() ? null : (VillagerBeaconEntity.BeamSegment)blockEntity.listOfBeamSegments.get(blockEntity.listOfBeamSegments.size() - 1);
        int l = world.getTopY(Type.WORLD_SURFACE, i, k);

        int n;
        for(n = 0; n < 10 && blockPos2.getY() <= l; ++n) {
            BlockState blockState = world.getBlockState(blockPos2);
            Block block = blockState.getBlock();
            if (block instanceof Stainable) {
                float[] fs = ((Stainable)block).getColor().getColorComponents();
                if (blockEntity.listOfBeamSegments.size() <= 1) {
                    beamSegment = new VillagerBeaconEntity.BeamSegment(fs);
                    blockEntity.listOfBeamSegments.add(beamSegment);
                } else if (beamSegment != null) {
                    if (Arrays.equals(fs, beamSegment.color)) {
                        beamSegment.increaseHeight();
                    } else {
                        beamSegment = new VillagerBeaconEntity.BeamSegment(new float[]{(beamSegment.color[0] + fs[0]) / 2.0F, (beamSegment.color[1] + fs[1]) / 2.0F, (beamSegment.color[2] + fs[2]) / 2.0F});
                        blockEntity.listOfBeamSegments.add(beamSegment);
                    }
                }
            } else {
                if (beamSegment == null || blockState.getOpacity(world, blockPos2) >= 15 && !blockState.isOf(Blocks.BEDROCK)) {
                    blockEntity.listOfBeamSegments.clear();
                    blockEntity.minY = l;
                    world.setBlockState(pos, state.with(VillagerBeaconBlock.ACTIVE, false));
                    break;
                }

                beamSegment.increaseHeight();
            }

            blockPos2 = blockPos2.up();
            ++blockEntity.minY;
        }

        n = blockEntity.level;
        if (world.getTime() % 80L == 0L) {
            applyPlayerEffects(world, pos, blockEntity.level, blockEntity.primary);
            playSound(world, pos, SoundEvents.BLOCK_BEACON_AMBIENT);
            if (!blockEntity.beamSegments.isEmpty()) {
                blockEntity.level = updateLevel(world, i, j, k);
            }

            if (blockEntity.level > 0 && !blockEntity.beamSegments.isEmpty()) {
                //System.out.println("applyingEffects");
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
                    world.setBlockState(pos, state.with(VillagerBeaconBlock.ACTIVE, true));
                    Iterator var17 = world.getNonSpectatingEntities(ServerPlayerEntity.class, (new Box((double)i, (double)j, (double)k, (double)i, (double)(j - 4), (double)k)).expand(10.0D, 5.0D, 10.0D)).iterator();

                    while(var17.hasNext()) {
                        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var17.next();
                        Criteria.CONSTRUCT_BEACON.trigger(serverPlayerEntity, blockEntity.level);
                    }
                } else if (bl && !bl2) {
                    playSound(world, pos, SoundEvents.BLOCK_BEACON_DEACTIVATE);
                    // add deactivated texture
                    world.setBlockState(pos, state.with(VillagerBeaconBlock.ACTIVE, false));
                }
            }
        }

    }

         */
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
                    if (world.getBlockState(new BlockPos(l, k, m)) != Blocks.COBBLESTONE.getDefaultState()) { // if (!world.getBlockState(new BlockPos(l, k, m)).isIn(BlockTags.BEACON_BASE_BLOCKS)) {
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

    private static Vector<StatusEffect> VILLAGER_EFFECTS = new Vector<StatusEffect>();
    static {
        VILLAGER_EFFECTS.add(StatusEffects.SLOW_FALLING);
        VILLAGER_EFFECTS.add(StatusEffects.INVISIBILITY);
        VILLAGER_EFFECTS.add(StatusEffects.FIRE_RESISTANCE);
        VILLAGER_EFFECTS.add(StatusEffects.WATER_BREATHING);
        VILLAGER_EFFECTS.add(StatusEffects.REGENERATION);
        VILLAGER_EFFECTS.add(StatusEffects.INSTANT_HEALTH);
        VILLAGER_EFFECTS.add(StatusEffects.STRENGTH);
        VILLAGER_EFFECTS.add(StatusEffects.SPEED);
        VILLAGER_EFFECTS.add(StatusEffects.DOLPHINS_GRACE);
        VILLAGER_EFFECTS.add(StatusEffects.JUMP_BOOST);
        VILLAGER_EFFECTS.add(StatusEffects.RESISTANCE);
//        VILLAGER_EFFECTS.add(EffectsKNB.agressionEffect);
    }

    /*
    private static StatusEffect[] MOB_EFFECTS = new StatusEffect[]{StatusEffects.GLOWING, StatusEffects.INSTANT_DAMAGE,
            StatusEffects.LEVITATION, StatusEffects.SLOW_FALLING};
    private static StatusEffect[] PLAYER_EFFECTS = new StatusEffect[]{StatusEffects.INVISIBILITY, StatusEffects.WITHER, StatusEffects.SLOWNESS,
            StatusEffects.LEVITATION, StatusEffects.SLOW_FALLING,
            StatusEffects.FIRE_RESISTANCE, StatusEffects.SATURATION, StatusEffects.WATER_BREATHING,
            StatusEffects.INSTANT_HEALTH, StatusEffects.HERO_OF_THE_VILLAGE, StatusEffects.STRENGTH};
    */

    private static void applyPlayerEffects(World world, BlockPos pos, int beaconLevel, @Nullable StatusEffect primaryEffect) {
        if (!world.isClient) {
            double d = (double)(beaconLevel * 10 + 25);
            int i = 0;
            if (beaconLevel >= 4) {
                i = 1;
            }

//            int j = (7 + beaconLevel * 2) * 20;
            int j = 400;
            Box box = (new Box(pos)).expand(d).stretch(0.0D, (double)world.getHeight(), 0.0D);

//            Random r = new Random();
//            primaryEffect = VILLAGER_EFFECTS.get(r.nextInt(VILLAGER_EFFECTS.size()));
//            if (primaryEffect == null) {
//            }

            List<VillagerEntity> villagers = world.getNonSpectatingEntities(VillagerEntity.class, box);
            Iterator iter = villagers.iterator();
            VillagerEntity villager;
            while(iter.hasNext()) {
                villager = (VillagerEntity)iter.next();
                villager.addStatusEffect(new StatusEffectInstance(primaryEffect, j, i, true, true));
//                villager.getBrain().remember(new MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD<PlayerEntity>());
                if (primaryEffect == StatusEffects.WATER_BREATHING) {
//                    villager.getBrain().doExclusively(Activity.CORE);
//                    villager.getBrain().forget(MemoryModuleType.WALK_TARGET);
                }
            }

            List<IronGolemEntity> ironGolems = world.getNonSpectatingEntities(IronGolemEntity.class, box);
            iter = ironGolems.iterator();
            IronGolemEntity ironGolem;
            while(iter.hasNext()) {
                ironGolem = (IronGolemEntity)iter.next();
                ironGolem.addStatusEffect(new StatusEffectInstance(primaryEffect, j, i, true, true));
            }

//            List<PlayerEntity> players = world.getNonSpectatingEntities(PlayerEntity.class, box);
//            iter = players.iterator();
//            PlayerEntity player;
//            while(iter.hasNext()) {
//                player = (PlayerEntity)iter.next();
//                player.addStatusEffect(new StatusEffectInstance(primaryEffect, j, i, true, true));
//            }

//            if (primaryEffect != null) {
//
//
//            } else {
//                System.out.println("primary effect is null!");
//            }

        }
    }

    public static void playSound(World world, BlockPos pos, SoundEvent sound) {
        world.playSound((PlayerEntity)null, pos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    public List<VillagerBeaconEntity.BeamSegment> getBeamSegments() {
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
        // get effects
        this.primary = getPotionEffectById(nbt.getInt("Primary"));
        if (this.primary == null) {
            Random r = new Random();
            this.primary = VILLAGER_EFFECTS.get(r.nextInt(VILLAGER_EFFECTS.size()));
            nbt.putInt("Primary", StatusEffect.getRawId(this.primary));
        }
        // get level
        this.level = nbt.getInt("Levels");
        // get custom name
        if (nbt.contains("CustomName", 8)) {
            this.customName = Serializer.fromJson(nbt.getString("CustomName"));
        }
        this.lock = ContainerLock.fromNbt(nbt);
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        // save effects
        nbt.putInt("Primary", StatusEffect.getRawId(this.primary));
        // add strength and then sub for guardian workaround? - need code here and in apply effects code and also on screen
        // save level
        nbt.putInt("Levels", this.level);
        // store custom name
        if (this.customName != null) {
            nbt.putString("CustomName", Serializer.toJson(this.customName));
        }
        this.lock.writeNbt(nbt);
        return nbt;
    }

    public void setCustomName(@Nullable Text customName) {
        this.customName = customName;
    }

//    public Text getDisplayName() {
//        return (Text)(this.customName != null ? this.customName : new TranslatableText("container.villager_beacon"));
//    }

    public void setWorld(World world) {
        super.setWorld(world);
        this.minY = world.getBottomY() - 1;
    }

    public @Nullable StatusEffect getEffect() {
        return this.primary;
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