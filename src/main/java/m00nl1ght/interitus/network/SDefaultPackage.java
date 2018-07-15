package m00nl1ght.interitus.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import m00nl1ght.interitus.Interitus;
import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure;
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

public class SDefaultPackage implements IMessage {

	public SDefaultPackage() {}

	private String channel;
	private PacketBuffer data;

	public SDefaultPackage(String channel, PacketBuffer buffer) {
		this.channel = channel;
		this.data = buffer;
		if (buffer.writerIndex() > 32767) {
			throw new IllegalArgumentException("Payload may not be larger than 32767 bytes");
		}
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		PacketBuffer buf = new PacketBuffer(buffer);
		buf.writeString(this.channel);
        synchronized(this.data) { //This may be access multiple times, from multiple threads, lets be safe.
        this.data.markReaderIndex();
        buf.writeBytes(this.data);
        this.data.resetReaderIndex();
        }
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		PacketBuffer buf = new PacketBuffer(buffer);
		this.channel = buf.readString(20);
        int i = buf.readableBytes();
        if (i >= 0 && i <= 32767) {
            this.data = new PacketBuffer(buf.readBytes(i));
        } else {
            throw new IllegalStateException("Payload may not be larger than 32767 bytes");
        }
	}
	
	public String getChannelName() {
        return this.channel;
    }

    public PacketBuffer getBufferData() {
        return this.data;
    }
    
    public static void sendGenTaskGui(EntityPlayerMP player, Structure struct) {
    	try {
			PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
			packetbuffer.writeString(struct.name);
			packetbuffer.writeCompoundTag(StructurePack.getGenTaskClientTag(struct));
			ModNetwork.INSTANCE.sendTo(new SDefaultPackage("GenTaskGui", packetbuffer), player);
		} catch (Exception exception) {
			Interitus.logger.warn("Could not send structure pack gui packet", exception);
		}
    }
    
    public static void sendStructurePackGui(EntityPlayerMP player) {
    	try {
			PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
			packetbuffer.writeCompoundTag(StructurePackInfo.create());
			ModNetwork.INSTANCE.sendTo(new SDefaultPackage("StructPackGui", packetbuffer), player);
		} catch (Exception exception) {
			Interitus.logger.warn("Could not send structure pack gui packet", exception);
		}
    }
    
    public static void sendStructureBlockGui(EntityPlayerMP player, TileEntityAdvStructure te) {
    	try {
			PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
			te.writeCoordinates(packetbuffer);
			packetbuffer.writeCompoundTag(StructurePackInfo.create());
			ModNetwork.INSTANCE.sendTo(new SDefaultPackage("StructBlockGui", packetbuffer), player);
		} catch (Exception exception) {
			Interitus.logger.warn("Could not send structure block gui packet", exception);
		}
    }
    
    public static void sendStructureDataGui(EntityPlayerMP player, TileEntityAdvStructure te) {
    	try {
			PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
			te.writeCoordinates(packetbuffer);
			packetbuffer.writeCompoundTag(StructurePackInfo.create());
			ModNetwork.INSTANCE.sendTo(new SDefaultPackage("StructDataGui", packetbuffer), player);
		} catch (Exception exception) {
			Interitus.logger.warn("Could not send structure data gui packet", exception);
		}
    }
    
    public static void sendStructureLootGui(EntityPlayerMP player, TileEntityAdvStructure te, BlockPos target) {
    	try {
			PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
			te.writeCoordinates(packetbuffer);
			packetbuffer.writeBlockPos(target);
			packetbuffer.writeCompoundTag(StructurePackInfo.create());
			ModNetwork.INSTANCE.sendTo(new SDefaultPackage("StructLootGui", packetbuffer), player);
		} catch (Exception exception) {
			Interitus.logger.warn("Could not send structure loot gui packet", exception);
		}
    }

	public static class SDefaultPackageHandler implements IMessageHandler<SDefaultPackage, IMessage> {

		@Override
		public IMessage onMessage(SDefaultPackage p, MessageContext ctx) {

			switch (p.getChannelName()) {
				case "StructPackGui":
					Minecraft.getMinecraft().addScheduledTask(() -> {
						this.procStructPackGui(p.getBufferData());
					});
					break;
				case "StructBlockGui":
					Minecraft.getMinecraft().addScheduledTask(() -> {
						this.procStructBlockGui(p.getBufferData());
					});
					break;
				case "StructDataGui":
					Minecraft.getMinecraft().addScheduledTask(() -> {
						this.procStructDataGui(p.getBufferData());
					});
					break;
				case "StructLootGui":
					Minecraft.getMinecraft().addScheduledTask(() -> {
						this.procStructLootGui(p.getBufferData());
					});
					break;
				case "GenTaskGui":
					Minecraft.getMinecraft().addScheduledTask(() -> {
						this.procGenTaskGui(p.getBufferData());
					});
					break;
				default:
					throw new IllegalStateException("Unknown SDefaultPacket channel: "+p.getChannelName());
			}
			return null;
		}

		private void procStructPackGui(PacketBuffer data) {
			try {
				NBTTagCompound tag = data.readCompoundTag();
				if (tag == null) {throw new IllegalStateException("no pack info");}
				Interitus.proxy.displayAdvStructScreen(StructurePackInfo.fromNBT(tag));
			} catch (Exception exception1) {
				Interitus.logger.error("Couldn't proc structure pack gui", exception1);
			}			
		}
		
		private void procStructBlockGui(PacketBuffer data) {
			try {
				BlockPos blockpos = new BlockPos(data.readInt(), data.readInt(), data.readInt());
				TileEntity tileentity1 = Minecraft.getMinecraft().world.getTileEntity(blockpos);
				NBTTagCompound tag = data.readCompoundTag();
				if (tag == null) {throw new IllegalStateException("no pack info");}
				if (tileentity1 instanceof TileEntityAdvStructure) {
					Interitus.proxy.displayAdvStructScreen((TileEntityAdvStructure) tileentity1, StructurePackInfo.fromNBT(tag));
				}
			} catch (Exception exception1) {
				Interitus.logger.error("Couldn't proc structure data gui", exception1);
			}			
		}
		
		private void procStructDataGui(PacketBuffer data) {
			try {
				BlockPos blockpos = new BlockPos(data.readInt(), data.readInt(), data.readInt());
				TileEntity tileentity1 = Minecraft.getMinecraft().world.getTileEntity(blockpos);
				NBTTagCompound tag = data.readCompoundTag();
				if (tag == null) {throw new IllegalStateException("no pack info");}
				if (tileentity1 instanceof TileEntityAdvStructure) {
					Interitus.proxy.displayStructureDataScreen((TileEntityAdvStructure) tileentity1, StructurePackInfo.fromNBT(tag));
				}
			} catch (Exception exception1) {
				Interitus.logger.error("Couldn't proc structure data gui", exception1);
			}			
		}
		
		private void procStructLootGui(PacketBuffer data) {
			try {
				BlockPos blockpos = new BlockPos(data.readInt(), data.readInt(), data.readInt());
				BlockPos target = data.readBlockPos();
				NBTTagCompound tag = data.readCompoundTag();
				if (tag == null) {throw new IllegalStateException("no pack info");}
				TileEntity tileentity1 = Minecraft.getMinecraft().world.getTileEntity(blockpos);
				if (tileentity1 instanceof TileEntityAdvStructure) {
					TileEntityAdvStructure te = (TileEntityAdvStructure) tileentity1;
					Interitus.proxy.displayStructureLootScreen(te, te.getLootOrNew(target), StructurePackInfo.fromNBT(tag));
				}
			} catch (Exception exception1) {
				Interitus.logger.error("Couldn't proc structure loot gui", exception1);
			}			
		}
		
		private void procGenTaskGui(PacketBuffer data) {
			try {
				String struct = data.readString(1000);
				NBTTagCompound tag = data.readCompoundTag();
				if (tag == null) {throw new IllegalStateException("no pack info");}
				Interitus.proxy.displayGenTasksScreen(tag, struct);
			} catch (Exception exception1) {
				Interitus.logger.error("Couldn't proc gen task gui", exception1);
			}			
		}
		
	}

}
