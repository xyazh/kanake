package com.xyazh.kanake.block.blocks.teleportation;

import com.xyazh.kanake.Kanake;
import com.xyazh.kanake.block.blocks.BlockBase;
import com.xyazh.kanake.particle.ModParticles;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class BlockTeleportation extends BlockBase implements ITileEntityProvider {
    public BlockTeleportation(String name) {
        super(name, Material.FIRE);
        setLightLevel(0.4F);
        setHardness(3.0F);
        setResistance(3.0F);
        setLightOpacity(0);
        GameRegistry.registerTileEntity(TileTeleportation.class, new ResourceLocation(Kanake.MODID, name));
        this.setTickRandomly(true);
        if (!Kanake.proxy.isServer()) {
            this.bindTileRender();
        }
    }

    @SideOnly(Side.CLIENT)
    protected void bindTileRender() {
        net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(
                TileTeleportation.class,
                new com.xyazh.kanake.block.blocks.teleportation.render.RenderTileTeleportation());
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(@Nonnull IBlockState stateIn, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Random rand) {
        for (int i = 0; i < 1; i++) {
            double r = rand.nextFloat();
            double phi = rand.nextFloat() * 2 * Math.PI ;
            double fx, fy, fz;
            fx = r * Math.cos(phi);
            fy = rand.nextFloat()/2;
            fz = r * Math.sin(phi);
            com.xyazh.kanake.particle.simple.SimpleParticle particle = new com.xyazh.kanake.particle.simple.particles.TpParticle1(worldIn,pos.getX() + fx + 0.5, pos.getY() + fy, pos.getZ() + fz + 0.5, 0, 0.05, 0);
        }
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
        return new TileTeleportation();
    }

    @Override
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileTeleportation) {
            worldIn.removeTileEntity(pos);
        }
        super.breakBlock(worldIn, pos, state);
    }

    public void onEntityWalk(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Entity entityIn) {
    }

    public void onEntityCollidedWithBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Entity entityIn) {
    }

    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(@Nonnull IBlockState blockState, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
        return NULL_AABB;
    }

    public boolean isOpaqueCube(@Nonnull IBlockState state) {
        return false;
    }

    public boolean isFullCube(@Nonnull IBlockState state) {
        return false;
    }

    public int quantityDropped(@Nonnull Random random) {
        return 0;
    }

    public int tickRate(@Nonnull World worldIn) {
        return 30;
    }

    public boolean requiresUpdates() {
        return false;
    }

    public boolean isCollidable() {
        return false;
    }

    @Nonnull
    @Override
    public EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }
}
