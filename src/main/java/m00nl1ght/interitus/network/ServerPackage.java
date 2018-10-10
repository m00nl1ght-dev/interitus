package m00nl1ght.interitus.network;

import java.io.IOException;
import io.netty.buffer.Unpooled;
import m00nl1ght.interitus.Interitus;
import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure;
import m00nl1ght.interitus.block.tileentity.TileEntitySummoner;
import m00nl1ght.interitus.client.ConditionTypeClient;
import m00nl1ght.interitus.client.gui.GuiEditSummoner;
import m00nl1ght.interitus.item.ItemStructureDataTool;
import m00nl1ght.interitus.item.ModItem;
import m00nl1ght.interitus.structures.ConditionType;
import m00nl1ght.interitus.structures.Structure;
import m00nl1ght.interitus.structures.StructurePack;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ServerPackage extends BasicPackage {
	
	private static IServerPackageHandler[] handlers = new IServerPackageHandler[9];
	
	public enum SPackageType {
		
		PACK_ACTION(ServerPackage::procPackAction),
		STRUCT_UPDATE(ServerPackage::procStructUpdate),
		STRUCT_ACTION(ServerPackage::procStructAction),
		SUMMONER_UPDATE(ServerPackage::procSummonerUpdate),
		UPDATE_GEN_TASKS(ServerPackage::procUpdateGenTasks),
		OPEN_GEN_TASK(ServerPackage::procOpenGenTask),
		UPDATE_COND_TYPE(ServerPackage::procUpdateCondType),
		OPEN_COND_TYPE(ServerPackage::procOpenCondType),
		SET_TOOL_COND(ServerPackage::procSetToolCond);
		
		private IServerPackageHandler handler;
		SPackageType(IServerPackageHandler handler) {
			this.handler=handler; handlers[this.ordinal()] = handler;
		}
		
	}
	
	public static boolean sendPackAction(int id, String param0, String param1) {
    	try {
			PacketBuffer buffer = createBuffer();
			buffer.writeInt(id);
			buffer.writeString(param0);
			buffer.writeString(param1);
			return send(SPackageType.PACK_ACTION, buffer);
		} catch (Exception e) {return logSendError(SPackageType.PACK_ACTION, e);}
    }

	public static void procPackAction(PacketBuffer data, EntityPlayerMP player) {
		if (checkPlayer(player)) return;
		if (!StructurePack.canPlayerEdit(player, true, false)) {return;}
		int action = data.readInt();
		String param0 = data.readString(1024);
		String param1 = data.readString(1024);
		switch (action) {
			case 0: // noop
				StructurePack.finishedEditing(player);
				return;
			case 1: // load
				StructurePack.finishedEditing(player);
				StructurePack pack = StructurePack.getPack(param0);
				if (pack == null) {
					msg(player, "Failed to load pack: Pack not found!"); return;
				}
				if (StructurePack.load(pack)) {
					msg(player, "Loaded structure pack <"+pack.name+">.");
				} else {
					msg(player, "Failed to load structure pack <"+pack.name+">.");
				}
				return;
			case 2: // delete
				StructurePack pack0 = StructurePack.getPack(param0);
				if (pack0 == null) {
					msg(player, "Failed to delete pack: Pack not found!"); return;
				}
				if (pack0.delete()) {
					msg(player, "Deleted structure pack <"+pack0.name+">.");
				} else {
					msg(player, "Failed to delete structure pack <"+pack0.name+">.");
				}
				return;
			case 3: // create
				StructurePack from = param1.isEmpty()?null:StructurePack.getPack(param1);
				StructurePack.create(param0, player, from);
				msg(player, "Created structure pack <"+param0+">."); return;
			case 4: // save
				StructurePack.finishedEditing(player);
				try {
					StructurePack.get().save();
				} catch (Exception e) {
					Interitus.logger.error("Failed to save active structure pack: " ,e);
					msg(player, "Failed to save active structure pack."); return;
				}
				msg(player, "Saved active structure pack."); return;
			case 5: // delete structure
				if (StructurePack.get().isReadOnly()) {
					msg(player, "Failed to delete structure: Pack is read-only!"); return;
				}
				if (StructurePack.get().deleteStructure(param0)) {
					msg(player, "Removed structure <"+param0+"> from active pack.");
				} else {
					msg(player, "Failed to delete structure <"+param0+">.");
				}
				return;
			case 6: // set description
				if (StructurePack.get().isReadOnly()) {
					msg(player, "Failed to set pack desription: Pack is read-only!"); return;
				}
				StructurePack.get().setDescription(param0);
				return;
			case 7: // sign pack
				if (StructurePack.get().isReadOnly()) {
					msg(player, "Failed to sign pack: Pack is already signed!"); return;
				}
				if (StructurePack.get().sign(player)) {
					msg(player, "The structure pack <"+StructurePack.get().name+"> has been signed by "+player.getName()+".");
				} else {
					msg(player, "Failed to remove structure <"+param0+">.");
				}
				return;
			case 8: // delete loot list
				if (StructurePack.get().isReadOnly()) {
					msg(player, "Failed to delete loot list: Pack is read-only!"); return;
				}
				if (StructurePack.get().deleteLootList(param0)) {
					msg(player, "Removed loot list <"+param0+"> from active pack.");
				} else {
					msg(player, "Failed to remove loot list <"+param0+">.");
				}
				return;
			case 9: // delete condition type
				if (StructurePack.get().isReadOnly()) {
					msg(player, "Failed to delete condition type: Pack is read-only!"); return;
				}
				if (StructurePack.get().deleteConditionType(param0)) {
					msg(player, "Removed condition type <"+param0+"> from active pack.");
				} else {
					msg(player, "Failed to remove condition type <"+param0+">.");
				}
				return;
			default:
				throw new IllegalStateException("invalid action id: "+action);
		}
	}
	
	public static boolean sendStructUpdate(TileEntityAdvStructure te, int pendingAction) {
		try {
			PacketBuffer buffer = createBuffer();
			te.writeCoordinates(buffer);
			buffer.writeByte(pendingAction);
			buffer.writeCompoundTag(te.getUpdateTag());
			return send(SPackageType.STRUCT_UPDATE, buffer);
		} catch (Exception e) {return logSendError(SPackageType.STRUCT_UPDATE, e);}
	}

	public static void procStructUpdate(PacketBuffer data, EntityPlayerMP player) throws IOException {
		if (checkPlayer(player)) return;
		BlockPos blockpos = new BlockPos(data.readInt(), data.readInt(), data.readInt());
		IBlockState blockstate = player.world.getBlockState(blockpos);
		TileEntity ate = player.world.getTileEntity(blockpos);
		if (ate instanceof TileEntityAdvStructure) {
			TileEntityAdvStructure te = (TileEntityAdvStructure) ate;
			te.finishedEditing(player);
			int pendingAction = data.readByte();
			NBTTagCompound tag = data.readCompoundTag();
			if (tag==null || !tag.hasKey("name")) {
				throw new IllegalStateException("Invalid tile entity NBT: "+tag);
			}
			te.readFromNBT(tag);
			switch (pendingAction) {
			case 2:
				if (!StructurePack.canPlayerEdit(player, false, false)) {
					msg(player, "Could not save structure because "+StructurePack.getEditingPlayer()+" is currently editing the active structure pack, please wait.");
					return;
				}
				if (te.save()) {
					msg(player, "Saved structure <"+te.getName()+"> sucessfully.");
				} else {
					msg(player, "Failed to save structure <"+te.getName()+">.");
				}
				break;
			case 3:
				if (!te.isStructureLoadable()) {
					msg(player, "Structure <"+te.getName()+"> not found.");
				} else if (te.load()) {
					msg(player, "Loaded structure <"+te.getName()+"> sucessfully.");
				} else {
					msg(player, "Prepared structure <"+te.getName()+"> to load.");
				}
				break;
			case 4:
				if (te.detectSize()) {
					msg(player, "Detected structure size sucessfully.");
				} else {
					msg(player, "Failed to detect structure size.");
				}
				break;
			case 5: // choose pack
				if (StructurePack.canPlayerEdit(player, true, true)) {
					if (!ClientPackage.sendStructurePackGui(player)) {
						StructurePack.finishedEditing(player); // in case something goes wrong sending the packet
					}
				}
				break;
			}
			te.markDirty();
			player.world.notifyBlockUpdate(blockpos, blockstate, blockstate, 3);
		}
	}
	
	public static boolean sendStructAction(TileEntityAdvStructure te, int id, int param0, int param1) {
    	try {
    		PacketBuffer buffer = createBuffer();
			te.writeCoordinates(buffer);
			buffer.writeInt(id);
			buffer.writeInt(param0);
			buffer.writeInt(param1);
			return send(SPackageType.STRUCT_ACTION, buffer);
		} catch (Exception e) {return logSendError(SPackageType.STRUCT_ACTION, e);}
    }

	public static void procStructAction(PacketBuffer data, EntityPlayerMP player) {
		if (checkPlayer(player)) return;
		BlockPos blockpos = new BlockPos(data.readInt(), data.readInt(), data.readInt());
		IBlockState blockstate = player.world.getBlockState(blockpos);
		TileEntity ate = player.world.getTileEntity(blockpos);
		if (ate instanceof TileEntityAdvStructure) {
			TileEntityAdvStructure te = (TileEntityAdvStructure) ate;
			int action = data.readInt();
			int param0 = data.readInt();
			int param1 = data.readInt();
			switch (action) {
			case 0: // give structure data tool
				te.giveDataTool(player);
				break;
			case 1: // replace air -> void
				int i = te.replaceBlocks(Blocks.AIR, Blocks.STRUCTURE_VOID);
				msg(player, "Replaced "+i+" blocks in the structure."); break;
			case 2: // replace void -> air 
				int i2 = te.replaceBlocks(Blocks.STRUCTURE_VOID, Blocks.AIR);
				msg(player, "Replaced "+i2+" blocks in the structure."); break;
			default:
				throw new IllegalStateException("invalid action id: "+action);
			}
		}
	}
	
	public static boolean sendSummonerUpdate(GuiEditSummoner gui) {
		try {
			PacketBuffer buffer = createBuffer();
			gui.writeToBuffer(buffer);
			return send(SPackageType.SUMMONER_UPDATE, buffer);
		} catch (Exception e) {return logSendError(SPackageType.SUMMONER_UPDATE, e);}
	}
	
	public static void procSummonerUpdate(PacketBuffer data, EntityPlayerMP player) {
		if (checkPlayer(player)) return;
		BlockPos blockpos = new BlockPos(data.readInt(), data.readInt(), data.readInt());
		IBlockState blockstate = player.world.getBlockState(blockpos);
		TileEntity ate = player.world.getTileEntity(blockpos);
		if (ate instanceof TileEntitySummoner) {
			TileEntitySummoner te = (TileEntitySummoner) ate;
			if (te.readFromBuffer(data)) {
				msg(player, "Summoner data set.");
			} else {
				msg(player, "Failed to set new entity for summoner.");
			}
			te.markDirty();
			player.world.notifyBlockUpdate(blockpos, blockstate, blockstate, 3);
		}
	}
	
	public static boolean sendUpdateGenTasks(NBTTagCompound tag, String struct) {
    	try {
    		PacketBuffer buffer = createBuffer();
			buffer.writeString(struct);
			buffer.writeCompoundTag(tag);
			return send(SPackageType.UPDATE_GEN_TASKS, buffer);
		} catch (Exception e) {return logSendError(SPackageType.UPDATE_GEN_TASKS, e);}
    }

	public static void procUpdateGenTasks(PacketBuffer data, EntityPlayerMP player) throws IOException {
		if (checkPlayer(player)) return;
		if (!StructurePack.canPlayerEdit(player, false, true)) {throw new IllegalStateException("another player is editing the pack");}
		String struct = data.readString(1000);
		Structure str = StructurePack.getStructure(struct);
		if (str==null) {throw new IllegalStateException("structure not found");}
		NBTTagCompound tag = data.readCompoundTag();
		if (tag == null) {throw new IllegalStateException("no nbt tag");}
		StructurePack.updateGenTasks(str, tag);
	}
	
	public static boolean sendOpenGenTasks(String struct) {
    	try {
    		PacketBuffer buffer = createBuffer();
			buffer.writeString(struct);
			return send(SPackageType.OPEN_GEN_TASK, buffer);
		} catch (Exception e) {return logSendError(SPackageType.OPEN_GEN_TASK, e);}
    }

	public static void procOpenGenTask(PacketBuffer data, EntityPlayerMP player) {
		if (checkPlayer(player)) return;
		String struct = data.readString(1000);
		Structure str = StructurePack.getStructure(struct);
		if (str==null) {throw new IllegalStateException("structure not found");}
		ClientPackage.sendGenTaskGui(player, str);
	}
	
	public static boolean sendUpdateCondType(NBTTagCompound tag, String ct) {
    	try {
    		PacketBuffer buffer = createBuffer();
			buffer.writeString(ct);
			buffer.writeCompoundTag(tag);
			return send(SPackageType.UPDATE_COND_TYPE, buffer);
		} catch (Exception e) {return logSendError(SPackageType.UPDATE_COND_TYPE, e);}
    }

	public static void procUpdateCondType(PacketBuffer data, EntityPlayerMP player) throws IOException {
		if (checkPlayer(player)) return;
		if (!StructurePack.canPlayerEdit(player, false, true)) {throw new IllegalStateException("another player is editing the pack");}
		String ct = data.readString(1000);
		NBTTagCompound tag = data.readCompoundTag();
		if (tag == null) {throw new IllegalStateException("no nbt tag");}
		StructurePack.updateCondType(ct, tag);
	}
	
	public static boolean sendOpenCondType(String type) { // empty string -> new cond type
    	try {
    		PacketBuffer buffer = createBuffer();
			buffer.writeString(type);
			buffer.writeBoolean(!ConditionTypeClient.isMaterialListPopulated());
			return send(SPackageType.OPEN_COND_TYPE, buffer);
		} catch (Exception e) {return logSendError(SPackageType.OPEN_COND_TYPE, e);}
    }

	public static void procOpenCondType(PacketBuffer data, EntityPlayerMP player) {
		if (checkPlayer(player)) return;
		String type = data.readString(1000);
		boolean reqMaterials = data.readBoolean();
		if (type.isEmpty()) {
			ClientPackage.sendCondTypeGui(player, null, reqMaterials);
		} else {
			ConditionType ct = StructurePack.getConditionType(type);
			if (ct==null) {throw new IllegalStateException("condition type not found");}
			ClientPackage.sendCondTypeGui(player, ct, reqMaterials);
		}
	}
	
	public static boolean sendSetToolCond(String cond_name) {
		try {
			PacketBuffer buffer = createBuffer();
			buffer.writeString(cond_name);
			return send(SPackageType.SET_TOOL_COND, buffer);
		} catch (Exception e) {return logSendError(SPackageType.SET_TOOL_COND, e);}
	}

	public static void procSetToolCond(PacketBuffer data, EntityPlayerMP player) {
		if (checkPlayer(player)) return;
		String type = data.readString(1000);
		ConditionType ct = StructurePack.getConditionType(type);
		if (ct==null) {throw new IllegalStateException("condition type not found");}
		ItemStack stack = player.getHeldItemMainhand();
		if (!stack.isEmpty() && stack.getItem()==ModItem.STRUCTURE_TOOL) {
			ItemStructureDataTool.setMode(stack, ct);
			player.sendStatusMessage(new TextComponentString("Condition Type: "+ct.getName()), true);
		}
	}
	
	public static class ServerPackageHandler implements IMessageHandler<ServerPackage, IMessage>  {
		@Override
		public IMessage onMessage(ServerPackage message, MessageContext ctx) {
			if (message.getInternalID()>handlers.length || message.getInternalID()<0) {
				throw new RuntimeException("invalid ServerPackage: no handler registered for channel "+message.getInternalID());
			}
			IServerPackageHandler handler = handlers[message.getInternalID()];
			EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
			serverPlayer.getServerWorld().addScheduledTask(() -> {
				try {
					handler.apply(message.getData(), serverPlayer);
				} catch (Exception e) {
					Interitus.logger.error("Failed to process ServerPacket ("+SPackageType.values()[message.getInternalID()].name()+"): ", e);
				}
			});
			return null;
		}
	}
	
	@FunctionalInterface
	private interface IServerPackageHandler {
		public void apply(PacketBuffer data, EntityPlayerMP player) throws IOException;
	}
	
	public ServerPackage() {};
	
	public ServerPackage(SPackageType type, PacketBuffer data) {
		super(type, data);
	}
	
	private static PacketBuffer createBuffer() {
		return new PacketBuffer(Unpooled.buffer());
	}
	
	private static boolean send(SPackageType type, PacketBuffer data) {
		ModNetwork.INSTANCE.sendToServer(new ServerPackage(type, data));
		return true;
	}
	
	private static boolean checkPlayer(EntityPlayerMP player) {
		if (!player.canUseCommandBlock()) {
			player.sendStatusMessage(new TextComponentString("You don't have permission to do that."), false);
			return true;
		}
		return false;
	}
	
	private static void msg(EntityPlayerMP player, String msg) {
		player.sendMessage(new TextComponentString(msg));
	}
	
	private static boolean logSendError(SPackageType type, Exception e) {
		Interitus.logger.error("Failed to send ServerPacket ("+type.name()+"): ", e);
		return false;
	}

}
