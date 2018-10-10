package m00nl1ght.interitus.network;

import java.io.IOException;
import io.netty.buffer.Unpooled;
import m00nl1ght.interitus.Interitus;
import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure;
import m00nl1ght.interitus.client.ConditionTypeClient;
import m00nl1ght.interitus.structures.ConditionType;
import m00nl1ght.interitus.structures.Structure;
import m00nl1ght.interitus.structures.StructurePack;
import m00nl1ght.interitus.structures.StructurePackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ClientPackage extends BasicPackage {
	
	private static IClientPackageHandler[] handlers = new IClientPackageHandler[5];
	
	public enum CPackageType {
		
		STRUCT_PACK_GUI(ClientPackage::procStructPackGui),
		STRUCT_BLOCK_GUI(ClientPackage::procStructBlockGui),
		STRUCT_LOOT_GUI(ClientPackage::procStructLootGui),
		GEN_TASK_GUI(ClientPackage::procGenTaskGui),
		COND_TYPE_GUI(ClientPackage::procCondTypeGui);
		
		private IClientPackageHandler handler;
		CPackageType(IClientPackageHandler handler) {
			this.handler=handler; handlers[this.ordinal()] = handler;
		}
		
	}
	
	public static boolean sendStructurePackGui(EntityPlayerMP player) {
    	try {
			PacketBuffer buffer = createBuffer();
			buffer.writeCompoundTag(StructurePackInfo.create());
			return send(player, CPackageType.STRUCT_PACK_GUI, buffer);
		} catch (Exception e) {return logSendError(CPackageType.STRUCT_PACK_GUI, e);}
    }

	public static void procStructPackGui(PacketBuffer data) throws IOException {
		NBTTagCompound tag = data.readCompoundTag();
		if (tag == null) {throw new IllegalStateException("no pack info");}
		StructurePackInfo.fromNBT(tag);
		Interitus.proxy.displayAdvStructScreen();
	}
	
	public static boolean sendStructureBlockGui(EntityPlayerMP player, TileEntityAdvStructure te, boolean dataScreen) {
    	try {
    		PacketBuffer buffer = createBuffer();
			te.writeCoordinates(buffer);
			buffer.writeBoolean(dataScreen);
			buffer.writeCompoundTag(StructurePackInfo.create());
			return send(player, CPackageType.STRUCT_BLOCK_GUI, buffer);
		} catch (Exception e) {return logSendError(CPackageType.STRUCT_BLOCK_GUI, e);}
    }
	
	public static void procStructBlockGui(PacketBuffer data) throws IOException {
		BlockPos blockpos = new BlockPos(data.readInt(), data.readInt(), data.readInt());
		TileEntity te = Minecraft.getMinecraft().world.getTileEntity(blockpos);
		boolean dataScreen = data.readBoolean();
		NBTTagCompound tag = data.readCompoundTag();
		if (tag == null) {throw new IllegalStateException("no pack info");}
		if (te instanceof TileEntityAdvStructure) {
			StructurePackInfo.fromNBT(tag);
			if (dataScreen) {
				Interitus.proxy.displayStructureDataScreen((TileEntityAdvStructure) te);
			} else {
				Interitus.proxy.displayAdvStructScreen((TileEntityAdvStructure) te);
			}
		}
	}
	
	public static boolean sendStructureLootGui(EntityPlayerMP player, TileEntityAdvStructure te, BlockPos target) {
    	try {
    		PacketBuffer buffer = createBuffer();
			te.writeCoordinates(buffer);
			buffer.writeBlockPos(target);
			buffer.writeCompoundTag(StructurePackInfo.create());
			return send(player, CPackageType.STRUCT_LOOT_GUI, buffer);
		} catch (Exception e) {return logSendError(CPackageType.STRUCT_LOOT_GUI, e);}
    }
	
	public static void procStructLootGui(PacketBuffer data) throws IOException {
		BlockPos blockpos = new BlockPos(data.readInt(), data.readInt(), data.readInt());
		BlockPos target = data.readBlockPos();
		NBTTagCompound tag = data.readCompoundTag();
		if (tag == null) {throw new IllegalStateException("no pack info");}
		TileEntity ate = Minecraft.getMinecraft().world.getTileEntity(blockpos);
		if (ate instanceof TileEntityAdvStructure) {
			TileEntityAdvStructure te = (TileEntityAdvStructure) ate;
			StructurePackInfo.fromNBT(tag);
			Interitus.proxy.displayStructureLootScreen(te, te.getLootOrNew(target));
		}
	}
	
	public static boolean sendGenTaskGui(EntityPlayerMP player, Structure struct) {
    	try {
    		PacketBuffer buffer = createBuffer();
			buffer.writeString(struct.name);
			buffer.writeCompoundTag(StructurePack.getGenTaskClientTag(struct));
			return send(player, CPackageType.GEN_TASK_GUI, buffer);
		} catch (Exception e) {return logSendError(CPackageType.GEN_TASK_GUI, e);}
    }
	
	public static void procGenTaskGui(PacketBuffer data) throws IOException {
		String struct = data.readString(1000);
		NBTTagCompound tag = data.readCompoundTag();
		if (tag == null) {throw new IllegalStateException("no pack info");}
		Interitus.proxy.displayGenTasksScreen(tag, struct);
	}
	
	public static boolean sendCondTypeGui(EntityPlayerMP player, ConditionType type, boolean reqMaterials) {
    	try {
    		PacketBuffer buffer = createBuffer();
			if (type == null) { // -> new cond type
				buffer.writeString("");
			} else {
				buffer.writeString(type.getName());
				buffer.writeCompoundTag(ConditionType.save(type, null));
			}
			if (reqMaterials) {
				NBTTagCompound matTag = new NBTTagCompound();
				ConditionType.writeMaterialList(matTag);
				buffer.writeCompoundTag(matTag);
			}
			return send(player, CPackageType.COND_TYPE_GUI, buffer);
		} catch (Exception e) {return logSendError(CPackageType.COND_TYPE_GUI, e);}
    }
	
	public static void procCondTypeGui(PacketBuffer data) throws IOException {
		String type = data.readString(1000);
		NBTTagCompound tag = null;
		if (!type.isEmpty()) { // -> new cond type
			tag = data.readCompoundTag();
			if (tag == null) {throw new IllegalStateException("no cond info");}
		}
		if (data.readerIndex()<data.writerIndex()) {
			NBTTagCompound tag0 = data.readCompoundTag();
			if (tag0!=null) {
				ConditionTypeClient.setMaterialList(tag0);
			}
		}
		Interitus.proxy.displayCondTypeScreen(tag, type);
	}
	
	public static class ClientPackageHandler implements IMessageHandler<ClientPackage, IMessage>  {
		@Override
		public IMessage onMessage(ClientPackage message, MessageContext ctx) {
			if (message.getInternalID()>handlers.length || message.getInternalID()<0) {
				throw new RuntimeException("invalid ClientPackage: no handler registered for channel "+message.getInternalID());
			}
			IClientPackageHandler handler = handlers[message.getInternalID()];
			Minecraft.getMinecraft().addScheduledTask(() -> {
				try {
					handler.apply(message.getData());
				} catch (Exception e) {
					Interitus.logger.error("Failed to process ClientPacket ("+CPackageType.values()[message.getInternalID()].name()+"): ", e);
				}
			});
			return null;
		}
	}
	
	@FunctionalInterface
	private interface IClientPackageHandler  {
		public void apply(PacketBuffer data) throws IOException;
	}
	
	public ClientPackage() {};
	
	public ClientPackage(CPackageType type, PacketBuffer data) {
		super(type, data);
	}
	
	private static PacketBuffer createBuffer() {
		return new PacketBuffer(Unpooled.buffer());
	}
	
	private static boolean send(EntityPlayerMP player, CPackageType type, PacketBuffer data) {
		ModNetwork.INSTANCE.sendTo(new ClientPackage(type, data), player);
		return true;
	}
	
	private static boolean logSendError(CPackageType type, Exception e) {
		Interitus.logger.error("Failed to send ClientPacket ("+type.name()+"): ", e);
		return false;
	}

}
