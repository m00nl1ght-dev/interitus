package m00nl1ght.interitus.block;

import java.util.Random;

import m00nl1ght.interitus.Interitus;
import m00nl1ght.interitus.block.tileentity.TileEntitySummoner;
import m00nl1ght.interitus.crafting.ModCrafting;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSummoner extends Block {
	
	protected static final AxisAlignedBB SUMMONER_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.15D, 0.9375D);

	public BlockSummoner(String name) {
		super(Material.ROCK, MapColor.BLACK);
		this.setUnlocalizedName(name);
        this.setCreativeTab(ModCrafting.modTab);
        this.setResistance(100F);
        this.setHardness(20F);
        this.setSoundType(SoundType.STONE);
		this.setDefaultState(this.blockState.getBaseState());
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntitySummoner();
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity instanceof TileEntitySummoner ? ((TileEntitySummoner) tileentity).usedBy(playerIn) : false;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if (!world.getBlockState(pos.down()).getMaterial().isSolid()) {
			world.setBlockState(pos.down(), Blocks.OBSIDIAN.getDefaultState());
		}
	}
	
	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }
	
	@Override
	public boolean isFullBlock(IBlockState state) {
        return false;
    }

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
        return false;
    }
	
	@Override
	public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }
	
	@Override
	public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, SpawnPlacementType type) {
        return true;
    }
	
	@Override
	public boolean canDropFromExplosion(Explosion explosionIn) {
        return false;
    }
	
	@Override
	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
        return false;
    }
	
	@Override
	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return false;
    }
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		for (EnumFacing facing : EnumFacing.VALUES) {
			if (worldIn.getBlockState(pos.offset(facing)).getBlock()==ModBlock.blockSummoner) {return false;}
		}
        return worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos);
    }
	
	@Override
	public boolean canSpawnInBlock() {
        return true;
    }
	
	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return face==EnumFacing.DOWN?BlockFaceShape.SOLID:BlockFaceShape.UNDEFINED;
    }
	
	@Override
	public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
        //no drops
    }
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SUMMONER_AABB;
    }
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return SUMMONER_AABB;
    }
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }
	
	@Override
	public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return false;
    }
	
	@Override
	public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return side==EnumFacing.DOWN;
    }
	
	@Override
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.BLOCK;
	}
	
	@Override
	public int quantityDropped(Random random) {
        return 0;
    }
	
	@Override
	public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
        return 20+Interitus.random.nextInt(50);
    }

}
