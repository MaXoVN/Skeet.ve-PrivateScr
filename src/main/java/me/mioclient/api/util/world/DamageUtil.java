package me.mioclient.api.util.world;

import me.mioclient.api.util.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.function.BiPredicate;

public class DamageUtil
        implements Wrapper {
    public static int getRoundedDamage(ItemStack stack) {
        return (int) DamageUtil.getDamageInPercent(stack);
    }

    public static float getDamageInPercent(ItemStack stack) {
        return (float) (DamageUtil.getItemDamage(stack) / stack.getMaxDamage()) * 100.0f;
    }

    public static boolean isArmorLow(EntityPlayer player, int durability) {
        for (ItemStack piece : player.inventory.armorInventory) {
            if (piece == null) {
                return true;
            }
            if (DamageUtil.getItemDamage(piece) >= durability) continue;
            return true;
        }
        return false;
    }
    public static boolean canBreakWeakness(EntityPlayer player) {
        int strengthAmp = 0;
        PotionEffect effect = mc.player.getActivePotionEffect(MobEffects.STRENGTH);
        if (effect != null) {
            strengthAmp = effect.getAmplifier();
        }

        return !mc.player.isPotionActive(MobEffects.WEAKNESS) || strengthAmp >= 1 || mc.player.getHeldItemMainhand().getItem() instanceof ItemSword || mc.player.getHeldItemMainhand().getItem() instanceof ItemPickaxe || mc.player.getHeldItemMainhand().getItem() instanceof ItemAxe || mc.player.getHeldItemMainhand().getItem() instanceof ItemSpade;
    }
    //cry
    public static boolean isNaked(EntityPlayer player) {
        for (ItemStack piece : player.inventory.armorInventory) {
            if (piece == null || piece.isEmpty()) continue;
            return false;
        }
        return true;
    }

    public static int getItemDamage(ItemStack stack) {
        return stack.getMaxDamage() - stack.getItemDamage();
    }

    public static boolean canTakeDamage(boolean suicide) {
        return !mc.player.capabilities.isCreativeMode && !suicide;
    }

    public static float calculateDamage(double posX, double posY, double posZ, Entity entity) {
        float doubleExplosionSize = 12.0f;
        double distancedsize = entity.getDistance(posX, posY, posZ) / (double) doubleExplosionSize;
        Vec3d vec3d = new Vec3d(posX, posY, posZ);
        double blockDensity = 0.0;
        try {
            blockDensity = entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        } catch (Exception exception) {
            // empty catch block
        }
        double v = (1.0 - distancedsize) * blockDensity;
        float damage = (int) ((v * v + v) / 2.0 * 7.0 * (double) doubleExplosionSize + 1.0);
        double finald = 1.0;
        if (entity instanceof EntityLivingBase) {
            finald = DamageUtil.getBlastReduction((EntityLivingBase) entity, DamageUtil.getDamageMultiplied(damage), new Explosion(mc.world, null, posX, posY, posZ, 6.0f, false, true));
        }
        return (float) finald;
    }

    private static float getBlockDensity(Vec3d vec, AxisAlignedBB bb, BlockPos.MutableBlockPos mutablePos) {
        double x = 1.0D / ((bb.maxX - bb.minX) * 2.0D + 1.0D);
        double y = 1.0D / ((bb.maxY - bb.minY) * 2.0D + 1.0D);
        double z = 1.0D / ((bb.maxZ - bb.minZ) * 2.0D + 1.0D);
        double xFloor = (1.0D - Math.floor(1.0D / x) * x) / 2.0D;
        double zFloor = (1.0D - Math.floor(1.0D / z) * z) / 2.0D;
        if (x >= 0.0D && y >= 0.0D && z >= 0.0D) {
            int air = 0;
            int traced = 0;
            float a;
            for (a = 0.0F; a <= 1.0F; a = (float)(a + x)) {
                float b;
                for (b = 0.0F; b <= 1.0F; b = (float)(b + y)) {
                    float c;
                    for (c = 0.0F; c <= 1.0F; c = (float)(c + z)) {
                        double xOff = bb.minX + (bb.maxX - bb.minX) * a;
                        double yOff = bb.minY + (bb.maxY - bb.minY) * b;
                        double zOff = bb.minZ + (bb.maxZ - bb.minZ) * c;
                        RayTraceResult result = rayTraceBlocks((World)mc.world, new Vec3d(xOff + xFloor, yOff, zOff + zFloor), vec, 20, mutablePos, DamageUtil::isResistant);
                        if (result == null)
                            air++;
                        traced++;
                    }
                }
            }
            return air / traced;
        }
        return 0.0F;
    }
    public static RayTraceResult rayTraceBlocks(World world, Vec3d start, Vec3d end, int attempts, BlockPos.MutableBlockPos mutablePos) {
        return rayTraceBlocks(world, start, end, attempts, mutablePos, (pos, state) -> (state.getCollisionBoundingBox((IBlockAccess)world, pos) != null));
    }

    private static RayTraceResult rayTraceBlocks(World world, Vec3d start, Vec3d end, int attempts, BlockPos.MutableBlockPos mutablePos, BiPredicate<BlockPos, IBlockState> predicate) {
        if (!Double.isNaN(start.x) && !Double.isNaN(start.y) && !Double.isNaN(start.z) &&
                !Double.isNaN(end.x) && !Double.isNaN(end.y) && !Double.isNaN(end.z)) {
            int currentX = MathHelper.floor(start.x);
            int currentY = MathHelper.floor(start.y);
            int currentZ = MathHelper.floor(start.z);
            int endXFloor = MathHelper.floor(end.x);
            int endYFloor = MathHelper.floor(end.y);
            int endZFloor = MathHelper.floor(end.z);
            IBlockState startBlockState = world.getBlockState((BlockPos)mutablePos.setPos(currentX, currentY, currentZ));
            Block startBlock = startBlockState.getBlock();
            if (startBlock.canCollideCheck(startBlockState, false) && predicate.test(mutablePos, startBlockState)) {
                RayTraceResult result = startBlockState.collisionRayTrace(world, (BlockPos)mutablePos, start, end);
                if (result != null)
                    return result;
            }
            int counter = attempts;
            while (counter-- >= 0) {
                EnumFacing side;
                if (Double.isNaN(start.x) || Double.isNaN(start.y) || Double.isNaN(start.z))
                    return null;
                if (currentX == endXFloor && currentY == endYFloor && currentZ == endZFloor)
                    return null;
                double totalDiffX = end.x - start.x;
                double totalDiffY = end.y - start.y;
                double totalDiffZ = end.z - start.z;
                double nextX = 999.0D;
                double nextY = 999.0D;
                double nextZ = 999.0D;
                double diffX = 999.0D;
                double diffY = 999.0D;
                double diffZ = 999.0D;
                if (endXFloor > currentX) {
                    nextX = currentX + 1.0D;
                    diffX = (nextX - start.x) / totalDiffX;
                } else if (endXFloor < currentX) {
                    nextX = currentX;
                    diffX = (nextX - start.x) / totalDiffX;
                }
                if (endYFloor > currentY) {
                    nextY = currentY + 1.0D;
                    diffY = (nextY - start.y) / totalDiffY;
                } else if (endYFloor < currentY) {
                    nextY = currentY;
                    diffY = (nextY - start.y) / totalDiffY;
                }
                if (endZFloor > currentZ) {
                    nextZ = currentZ + 1.0D;
                    diffZ = (nextZ - start.z) / totalDiffZ;
                } else if (endZFloor < currentZ) {
                    nextZ = currentZ;
                    diffZ = (nextZ - start.z) / totalDiffZ;
                }
                if (diffX == -0.0D)
                    diffX = -1.0E-4D;
                if (diffY == -0.0D)
                    diffY = -1.0E-4D;
                if (diffZ == -0.0D)
                    diffZ = -1.0E-4D;
                if (diffX < diffY && diffX < diffZ) {
                    side = (endXFloor > currentX) ? EnumFacing.WEST : EnumFacing.EAST;
                    start = new Vec3d(nextX, start.y + totalDiffY * diffX, start.z + totalDiffZ * diffX);
                } else if (diffY < diffZ) {
                    side = (endYFloor > currentY) ? EnumFacing.DOWN : EnumFacing.UP;
                    start = new Vec3d(start.x + totalDiffX * diffY, nextY, start.z + totalDiffZ * diffY);
                } else {
                    side = (endZFloor > currentZ) ? EnumFacing.NORTH : EnumFacing.SOUTH;
                    start = new Vec3d(start.x + totalDiffX * diffZ, start.y + totalDiffY * diffZ, nextZ);
                }
                currentX = MathHelper.floor(start.x) - ((side == EnumFacing.EAST) ? 1 : 0);
                currentY = MathHelper.floor(start.y) - ((side == EnumFacing.UP) ? 1 : 0);
                currentZ = MathHelper.floor(start.z) - ((side == EnumFacing.SOUTH) ? 1 : 0);
                mutablePos.setPos(currentX, currentY, currentZ);
                IBlockState state = world.getBlockState((BlockPos)mutablePos);
                Block block = state.getBlock();
                if (block.canCollideCheck(state, false) && predicate.test(mutablePos, state)) {
                    RayTraceResult result = state.collisionRayTrace(world, (BlockPos)mutablePos, start, end);
                    if (result != null)
                        return result;
                }
            }
        }
        return null;
    }


    public static float calculateDamage(double posX, double posY, double posZ, Entity entity, BlockPos.MutableBlockPos mutablePos) {
        if (entity instanceof EntityPlayer && ((EntityPlayer)entity).isCreative())
            return 0.0F;
        float doubleExplosionSize = 12.0F;
        float scaledDist = (float)entity.getDistance(posX, posY, posZ) / doubleExplosionSize;
        if (scaledDist > 1.0F)
            return 0.0F;
        Vec3d vec = new Vec3d(posX, posY, posZ);
        float factor = (1.0F - scaledDist) * getBlockDensity(vec, entity.getEntityBoundingBox(), mutablePos);
        float damage = (factor * factor + factor) * 3.5F * doubleExplosionSize + 1.0F;
        if (entity instanceof EntityLivingBase)
            damage = getBlastReduction((EntityLivingBase)entity, damage);
        return damage;
    }
    public static float getBlastReduction(EntityLivingBase entity, float damage) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)entity;
            ICachedEntityPlayer cachedData = (ICachedEntityPlayer)player;
            float totalArmourValue = cachedData.getTotalArmourValue();
            float armourToughness = cachedData.getArmourToughness();
            float resistanceMultiplier = cachedData.getResistanceMultiplier();
            float blastMultiplier = cachedData.getBlastMultiplier();
            damage = getDifficultyMultiplier(damage);
            damage = CombatRules.getDamageAfterAbsorb(damage, totalArmourValue, armourToughness) * resistanceMultiplier * blastMultiplier;
            return Math.max(0.0F, damage);
        }
        damage = CombatRules.getDamageAfterAbsorb(damage, entity.getTotalArmorValue(), (float)entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        return damage;
    }
    public static float getDifficultyMultiplier(float damage) {
        switch (mc.world.getDifficulty()) {
            case PEACEFUL:
                return 0.0F;
            case EASY:
                return Math.min(damage / 2.0F + 1.0F, damage);
            case HARD:
                return damage * 1.5F;
        }
        return damage;
    }


    private static boolean isResistant(BlockPos pos, IBlockState state) {
        return (!state.getMaterial().isLiquid() && state
                .getBlock().getExplosionResistance((World)mc.world, pos, null, null) >= 19.7D);
    }



    public static float calculateDamageAlt(final Entity crystal, final Entity entity) {
        final BlockPos cPos = new BlockPos(crystal.posX, crystal.posY, crystal.posZ);
        return calculateDamage(cPos.getX(), cPos.getY(), cPos.getZ(), entity);
    }

    public static float calculateDamage(Entity crystal, Entity entity) {
        return calculateDamage(crystal.posX, crystal.posY, crystal.posZ, entity);
    }

    public static boolean hasDurability(ItemStack stack) { //totem
        Item item = stack.getItem();
        return item instanceof ItemArmor || item instanceof ItemSword || item instanceof ItemTool || item instanceof ItemShield;
    }

    public static float getBlastReduction(EntityLivingBase entity, float damageI, Explosion explosion) {
        float damage = damageI;
        if (entity instanceof EntityPlayer) {
            EntityPlayer ep = (EntityPlayer) entity;
            DamageSource ds = DamageSource.causeExplosionDamage(explosion);
            damage = CombatRules.getDamageAfterAbsorb(damage, (float) ep.getTotalArmorValue(), (float) ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
            int k = 0;
            try {
                k = EnchantmentHelper.getEnchantmentModifierDamage(ep.getArmorInventoryList(), ds);
            } catch (Exception exception) {
                // empty catch block
            }
            float f = MathHelper.clamp((float) k, 0.0f, 20.0f);
            damage *= 1.0f - f / 25.0f;
            if (entity.isPotionActive(MobEffects.RESISTANCE)) {
                damage -= damage / 4.0f;
            }
            damage = Math.max(damage, 0.0f);
            return damage;
        }
        damage = CombatRules.getDamageAfterAbsorb(damage, (float) entity.getTotalArmorValue(), (float) entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        return damage;
    }

    public static float getDamageMultiplied(float damage) {
        int diff = mc.world.getDifficulty().getId();
        return damage * (diff == 0 ? 0.0f : (diff == 2 ? 1.0f : (diff == 1 ? 0.5f : 1.5f)));
    }

    public static float calculateDamage(BlockPos pos, Entity entity) {
        return DamageUtil.calculateDamage((double) pos.getX() + 0.5, pos.getY() + 1, (double) pos.getZ() + 0.5, entity);
    }

    public static int getCooldownByWeapon(EntityPlayer player) {
        Item item = player.getHeldItemMainhand().getItem();
        if (item instanceof ItemSword) {
            return 600;
        }
        if (item instanceof ItemPickaxe) {
            return 850;
        }
        if (item == Items.IRON_AXE) {
            return 1100;
        }
        if (item == Items.STONE_HOE) {
            return 500;
        }
        if (item == Items.IRON_HOE) {
            return 350;
        }
        if (item == Items.WOODEN_AXE || item == Items.STONE_AXE) {
            return 1250;
        }
        if (item instanceof ItemSpade || item == Items.GOLDEN_AXE || item == Items.DIAMOND_AXE || item == Items.WOODEN_HOE || item == Items.GOLDEN_HOE) {
            return 1000;
        }
        return 250;
    }
}