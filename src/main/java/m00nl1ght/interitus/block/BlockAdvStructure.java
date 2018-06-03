package m00nl1ght.interitus.block;

import java.util.Random;

import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure;
import m00nl1ght.interitus.crafting.ModCrafting;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockAdvStructure extends Block {

	public static final PropertyEnum<TileEntityAdvStructure.Mode> MODE = PropertyEnum.<TileEntityAdvStructure.Mode> create("mode", TileEntityAdvStructure.Mode.class);

	public BlockAdvStructure(String name) {
		super(Material.IRON, MapColor.BLACK);
		this.setUnlocalizedName(name);
        this.setCreativeTab(ModCrafting.modTab);
        this.setResistance(100000.0F);
        this.setHardness(2.0F);
        this.setSoundType(SoundType.STONE);
		this.setDefaultState(this.blockState.getBaseState());
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityAdvStructure();
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity instanceof TileEntityAdvStructure ? ((TileEntityAdvStructure) tileentity).usedBy(playerIn) : false;
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if (!worldIn.isRemote) {
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if (tileentity instanceof TileEntityAdvStructure) {
				((TileEntityAdvStructure) tileentity).createdBy(placer);
			}
		}
	}

	@Override
    public int quantityDropped(Random random) {
        return 0;
    }

	@Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

	@Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(MODE, TileEntityAdvStructure.Mode.DATA);
    }

	@Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(MODE, TileEntityAdvStructure.Mode.getById(meta));
    }

	@Override
    public int getMetaFromState(IBlockState state) {
        return ((TileEntityAdvStructure.Mode)state.getValue(MODE)).getModeId();
    }

	@Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] {MODE});
    }

}
