package m00nl1ght.interitus.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import m00nl1ght.interitus.Main;
import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
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
    
    public static void sendStructureBlockGui(EntityPlayerMP player, TileEntityAdvStructure te) {
    	try {
			PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
			te.writeCoordinates(packetbuffer);
			//TODO add pack info
			ModNetwork.INSTANCE.sendTo(new SDefaultPackage("StructBlockGui", packetbuffer), player);
		} catch (Exception exception) {
			Main.logger.warn("Could not send structure block gui packet", exception);
		}
    }
    
    public static void sendStructureDataGui(EntityPlayerMP player, TileEntityAdvStructure te) {
    	try {
			PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
			te.writeCoordinates(packetbuffer);
			ModNetwork.INSTANCE.sendTo(new SDefaultPackage("StructDataGui", packetbuffer), player);
		} catch (Exception exception) {
			Main.logger.warn("Could not send structure data gui packet", exception);
		}
    }
    
    public static void sendStructureLootGui(EntityPlayerMP player, TileEntityAdvStructure te, BlockPos target) {
    	try {
			PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
			te.writeCoordinates(packetbuffer);
			packetbuffer.writeBlockPos(target);
			ModNetwork.INSTANCE.sendTo(new SDefaultPackage("StructLootGui", packetbuffer), player);
		} catch (Exception exception) {
			Main.logger.warn("Could not send structure loot gui packet", exception);
		}
    }

	public static class SDefaultPackageHandler implements IMessageHandler<SDefaultPackage, IMessage> {

		@Override
		public IMessage onMessage(SDefaultPackage p, MessageContext ctx) {

			switch (p.getChannelName()) {
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
				default:
					throw new IllegalStateException("Unknown SDefaultPacket channel: "+p.getChannelName());
			}
			return null;
		}

		private void procStructBlockGui(PacketBuffer data) {
			try {
				BlockPos blockpos = new BlockPos(data.readInt(), data.readInt(), data.readInt());
				TileEntity tileentity1 = Minecraft.getMinecraft().world.getTileEntity(blockpos);
				if (tileentity1 instanceof TileEntityAdvStructure) {
					Main.proxy.displayAdvStructScreen((TileEntityAdvStructure) tileentity1);
				}
			} catch (Exception exception1) {
				Main.logger.error("Couldn't proc structure data gui", exception1);
			}			
		}
		
		private void procStructDataGui(PacketBuffer data) {
			try {
				BlockPos blockpos = new BlockPos(data.readInt(), data.readInt(), data.readInt());
				TileEntity tileentity1 = Minecraft.getMinecraft().world.getTileEntity(blockpos);
				if (tileentity1 instanceof TileEntityAdvStructure) {
					Main.proxy.displayStructureDataScreen((TileEntityAdvStructure) tileentity1, null);
				}
			} catch (Exception exception1) {
				Main.logger.error("Couldn't proc structure data gui", exception1);
			}			
		}
		
		private void procStructLootGui(PacketBuffer data) {
			try {
				BlockPos blockpos = new BlockPos(data.readInt(), data.readInt(), data.readInt());
				BlockPos target = data.readBlockPos();
				TileEntity tileentity1 = Minecraft.getMinecraft().world.getTileEntity(blockpos);
				if (tileentity1 instanceof TileEntityAdvStructure) {
					TileEntityAdvStructure te = (TileEntityAdvStructure) tileentity1;
					Main.proxy.displayStructureLootScreen(te, te.getLootOrNew(target));
				}
			} catch (Exception exception1) {
				Main.logger.error("Couldn't proc structure loot gui", exception1);
			}			
		}
		
	}

}
