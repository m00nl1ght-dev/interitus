package m00nl1ght.interitus.item;

import java.util.List;

import javax.annotation.Nullable;

import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure;
import m00nl1ght.interitus.structures.Condition;
import m00nl1ght.interitus.structures.ConditionType;
import m00nl1ght.interitus.structures.StructurePack;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemStructureDataTool extends Item {
	
	public ItemStructureDataTool() {
		this.maxStackSize = 1;
		this.setCreativeTab(null);
		this.setNoRepair();
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {return EnumActionResult.PASS;}
		ItemStack stack = player.getHeldItem(hand);
		if (stack.getItem() != ModItem.STRUCTURE_TOOL) {return EnumActionResult.PASS;}
		if (!player.canUseCommandBlock()) {
			player.sendStatusMessage(new TextComponentString("You are not permitted to use this tool."), false);
			return EnumActionResult.FAIL;
		}
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock()==Blocks.CHEST) {
			if (player.isSneaking()) {
				TileEntityAdvStructure te = getTileEntity(player, stack);
				if (te.editLootData(player, pos)) {
					return EnumActionResult.SUCCESS;
				} else {
					return EnumActionResult.FAIL;
				}
			} else {
				return EnumActionResult.PASS;
			}
		}
		if (player.isSneaking()) {
			TileEntityAdvStructure te = getTileEntity(player, stack);
			if (te.editData(player)) {
				return EnumActionResult.SUCCESS;
			} else {
				return EnumActionResult.FAIL;
			}
		} else {
			// TODO UI list to choose condition type
//			ConditionType mode = this.getMode(stack, ConditionType.inGround);
//			int i = mode.ordinal()+1;
//			if (i >= ConditionType.values().length) {i=0;}
//			mode = ConditionType.values()[i];
//			this.setMode(stack, mode);
//			player.sendStatusMessage(new TextComponentString("Condition Type: "+mode.name()), true);
			return EnumActionResult.SUCCESS;
		}
    }
	
	public void onBlockLeftClicked(EntityPlayer player, BlockPos pos, ItemStack stack) {
		if (player.world.isRemote) {return;}
		if (stack.getItem() != ModItem.STRUCTURE_TOOL) {return;}
		if (!player.canUseCommandBlock()) {
			player.sendStatusMessage(new TextComponentString("You are not permitted to use this tool."), false);
			return;
		}
		TileEntityAdvStructure te = this.getTileEntity(player, stack);
		if (te==null) {return;}
		ConditionType type = this.getMode(stack);
		if (type==null) {
			//TODO show UI to choose condition type
			return;
		}
		te.getConditions().add(new Condition(type, pos, this.getNegated(stack)));
		te.markDirtyFlagged();
		player.sendStatusMessage(new TextComponentString("Added new condition at "+pos.toString()), false);
    }
	
	private TileEntityAdvStructure getTileEntity(EntityPlayer player, ItemStack stack) {
		BlockPos tepos = getPosFromStack(stack);
		if (tepos==null) {
			player.sendStatusMessage(new TextComponentString("The nbt tag of this tool is invalid!"), false);
			return null;
		}
		TileEntity te = player.world.getTileEntity(tepos);
		if (te==null || !(te instanceof TileEntityAdvStructure)) {
			player.sendStatusMessage(new TextComponentString("The position this data tool is registered to does not have a structure block. Did someone remove it?"), false);
			return null;
		}
		return (TileEntityAdvStructure) te;
	}
	
	@Override @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		NBTTagCompound tagA = stack.getTagCompound();
		if (tagA==null) {
			tooltip.add("@missing_position");
		} else {
			NBTTagCompound tag = tagA.getCompoundTag("structurePos");
			if (tag.hasNoTags()) {
				tooltip.add("@missing_position");
			} else {
				tooltip.add("@x"+tag.getInteger("x")+"y"+tag.getInteger("y")+"z"+tag.getInteger("z"));
			}
		}
		tooltip.add("");
		tooltip.add("Use this tool to add conditions and");
		tooltip.add("loot entries to your structure.");
    }
	
	public static BlockPos getPosFromStack(ItemStack stack) {
		if (stack.isEmpty()) {return null;}
		NBTTagCompound tag2 = stack.getTagCompound();
		if (tag2==null) {return null;}
		NBTTagCompound tag = tag2.getCompoundTag("structurePos");
		if (tag.hasNoTags()) {return null;}
		return new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
	}
	
	public static void writePosToBuffer(ItemStack stack, PacketBuffer buffer) {
		if (stack.isEmpty()) {throw new IllegalStateException("empty item stack!");}
		NBTTagCompound tag2 = stack.getTagCompound();
		if (tag2==null) {throw new IllegalStateException("invalid nbt!");}
		NBTTagCompound tag = tag2.getCompoundTag("structurePos");
		if (tag.hasNoTags()) {throw new IllegalStateException("invalid position data!");}
		buffer.writeInt(tag.getInteger("x"));
		buffer.writeInt(tag.getInteger("y"));
		buffer.writeInt(tag.getInteger("z"));
	}
	
	public static ConditionType getMode(ItemStack stack) {
		if (stack.isEmpty()) {return null;}
		NBTTagCompound tag = stack.getTagCompound();
		if (tag==null) {return null;}
		return StructurePack.get().getConditionType(tag.getString("mode"));
	}
	
	public static boolean getNegated(ItemStack stack) {
		if (stack.isEmpty()) {return false;}
		NBTTagCompound tag = stack.getTagCompound();
		if (tag==null) {return false;}
		return tag.getBoolean("n");
	}
	
	public static NBTTagCompound addTagForPos(NBTTagCompound tagIn, BlockPos pos) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("x", pos.getX());
		tag.setInteger("y", pos.getY());
		tag.setInteger("z", pos.getZ());
		tagIn.setTag("structurePos", tag);
		return tagIn;
	}
	
	public static void setMode(ItemStack stack, ConditionType mode) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag==null) {
			tag = new NBTTagCompound();
			stack.setTagCompound(tag);
		}
		tag.setString("mode", mode.getName());
	}
	
	public static ItemStack getItemForPos(BlockPos pos) {
		ItemStack stack = new ItemStack(ModItem.STRUCTURE_TOOL);
		stack.setTagCompound(addTagForPos(new NBTTagCompound(), pos));
		return stack;
	}
	
	@Override @SideOnly(Side.CLIENT)
    public boolean isFull3D() {
        return true;
    }
	
	@Override
	public boolean hasEffect(ItemStack stack) {
        return true;
    }
	
	@Override
	public String getHighlightTip(ItemStack item, String displayName) {
        return displayName;
    }
	
	@Override
    public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player) {
        return false;
    }
	
	@Override
	public boolean isDamageable() {
        return false;
    }
	
	@Override
	public boolean isEnchantable(ItemStack stack) {
        return false;
    }
	
	@Override
	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
        return false;
    }
	
	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		//NOOP
	}
	
}
