package m00nl1ght.interitus.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import m00nl1ght.interitus.Interitus;
import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure;
import m00nl1ght.interitus.block.tileentity.TileEntitySummoner;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CDefaultPackage implements IMessage {

	public CDefaultPackage() {}

	private String channel;
	private PacketBuffer data;

	public CDefaultPackage(String channel, PacketBuffer buffer) {
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
    
	public static boolean requestAction(TileEntityAdvStructure te, int id, int param0, int param1) {
    	try {
			PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
			te.writeCoordinates(packetbuffer);
			packetbuffer.writeInt(id);
			packetbuffer.writeInt(param0);
			packetbuffer.writeInt(param1);
			ModNetwork.INSTANCE.sendToServer(new CDefaultPackage("StructAction", packetbuffer));
			return true;
		} catch (Exception exception) {
			Interitus.logger.warn("Could not request structure action "+id, exception);
			return false;
		}
    }
	
	public static boolean sendStructUpdatePacket(TileEntityAdvStructure te, int pendingAction) {
		try {
			PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
			te.writeCoordinates(packetbuffer);
			packetbuffer.writeByte(pendingAction);
			packetbuffer.writeCompoundTag(te.getUpdateTag());
			ModNetwork.INSTANCE.sendToServer(new CDefaultPackage("StructUpdate", packetbuffer));
			return true;
		} catch (Exception exception) {
			Interitus.logger.warn("Could not request structure update with pendingAction "+pendingAction, exception);
			return false;
		}
	}

	public static class CDefaultPackageHandler implements IMessageHandler<CDefaultPackage, IMessage> {

		@Override
		public IMessage onMessage(CDefaultPackage p, MessageContext ctx) {
			
			EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
			
			switch (p.getChannelName()) {
			case "StructUpdate": 
				serverPlayer.getServerWorld().addScheduledTask(() -> {
					if (!serverPlayer.canUseCommandBlock()) {return;}
		            this.procAdvStruct(serverPlayer, p.getBufferData());
				});
				break;
			case "StructAction": 
				serverPlayer.getServerWorld().addScheduledTask(() -> {
					if (!serverPlayer.canUseCommandBlock()) {return;}
		            this.procStructAction(serverPlayer, p.getBufferData());
				});
				break;
			case "Summoner": 
				serverPlayer.getServerWorld().addScheduledTask(() -> {
					if (!serverPlayer.canUseCommandBlock()) {return;}
		            this.procSummoner(serverPlayer, p.getBufferData());
				});
				break;
			default:
				throw new IllegalStateException("Unknown CDefaultPacket channel: "+p.getChannelName());
			}
			return null;
		}
		
		private void procSummoner(EntityPlayerMP player, PacketBuffer data) {
			try {
				BlockPos blockpos = new BlockPos(data.readInt(), data.readInt(), data.readInt());
				IBlockState iblockstate1 = player.world.getBlockState(blockpos);
				TileEntity tileentity1 = player.world.getTileEntity(blockpos);

				if (tileentity1 instanceof TileEntitySummoner) {
					TileEntitySummoner te = (TileEntitySummoner) tileentity1;
					String entID = data.readString(24);
					te.setData(data.readFloat(), data.readInt(), data.readInt(), data.readInt(), data.readFloat(), data.readFloat(), data.readFloat());
					if (te.setEntity(entID)) {
						player.sendStatusMessage(new TextComponentString("Summoner data set."), false);
					} else {
						player.sendStatusMessage(new TextComponentString("Failed to set new entity for summoner, could not find entity with id: "+entID), false);
					}
					te.markDirty();
					player.world.notifyBlockUpdate(blockpos, iblockstate1, iblockstate1, 3);
				}
			} catch (Exception exception1) {
				Interitus.logger.error("Couldn't set summoner block", exception1);
			}
		}

		private void procAdvStruct(EntityPlayerMP player, PacketBuffer data) {
			try {
				BlockPos blockpos = new BlockPos(data.readInt(), data.readInt(), data.readInt());
				IBlockState iblockstate1 = player.world.getBlockState(blockpos);
				TileEntity tileentity1 = player.world.getTileEntity(blockpos);

				if (tileentity1 instanceof TileEntityAdvStructure) {
					TileEntityAdvStructure tileentitystructure = (TileEntityAdvStructure) tileentity1;
					int pendingAction = data.readByte();
					NBTTagCompound tag = data.readCompoundTag();
					if (tag==null || !tag.hasKey("name")) {
						throw new IllegalStateException("Invalid tile entity NBT: "+tag);
					}
					tileentitystructure.readFromNBT(tag);
					
					switch (pendingAction) {
					case 2:
						if (tileentitystructure.save()) {
							player.sendStatusMessage(new TextComponentTranslation("structure_block.save_success", new Object[] {tileentitystructure.getName()}), false);
						} else {
							player.sendStatusMessage(new TextComponentTranslation("structure_block.save_failure", new Object[] {tileentitystructure.getName()}), false);
						}
						break;
					case 3:
						if (!tileentitystructure.isStructureLoadable()) {
							player.sendStatusMessage(new TextComponentTranslation("structure_block.load_not_found", new Object[] {tileentitystructure.getName()}), false);
						} else if (tileentitystructure.load()) {
							player.sendStatusMessage(new TextComponentTranslation("structure_block.load_success", new Object[] {tileentitystructure.getName()}), false);
						} else {
							player.sendStatusMessage(new TextComponentTranslation("structure_block.load_prepare", new Object[] {tileentitystructure.getName()}), false);
						}
						break;
					case 4:
						if (tileentitystructure.detectSize()) {
							player.sendStatusMessage(new TextComponentTranslation("structure_block.size_success", new Object[] {tileentitystructure.getName()}), false);
						} else {
							player.sendStatusMessage(new TextComponentTranslation("structure_block.size_failure", new Object[0]), false);
						}
						break;
					}
					tileentitystructure.resetEditingPlayer();
					tileentitystructure.markDirty();
					player.world.notifyBlockUpdate(blockpos, iblockstate1, iblockstate1, 3);
				}
			} catch (Exception exception1) {
				Interitus.logger.error("Couldn't set structure block", exception1);
			}
		}
		
		private void procStructAction(EntityPlayerMP player, PacketBuffer data) {
			try {
				BlockPos blockpos = new BlockPos(data.readInt(), data.readInt(), data.readInt());
				TileEntity tileentity1 = player.world.getTileEntity(blockpos);

				if (tileentity1 instanceof TileEntityAdvStructure) {
					TileEntityAdvStructure te = (TileEntityAdvStructure) tileentity1;
					IBlockState iblockstate1 = player.world.getBlockState(blockpos);
					int action = data.readInt();
					int param0 = data.readInt();
					int param1 = data.readInt();
					switch (action) {
					case 0: // give structure data tool
						te.giveDataTool(player);
						break;
					case 1: // replace air -> void
						int i = te.replaceBlocks(Blocks.AIR, Blocks.STRUCTURE_VOID);
						player.sendStatusMessage(new TextComponentString("Replaced "+i+" blocks in the structure."), false);
						break;
					case 2: // replace void -> air 
						int i2 = te.replaceBlocks(Blocks.STRUCTURE_VOID, Blocks.AIR);
						player.sendStatusMessage(new TextComponentString("Replaced "+i2+" blocks in the structure."), false);
						break;
					default:
						throw new IllegalStateException("invalid action id");
					}
				}
			} catch (Exception exception1) {
				Interitus.logger.error("Couldn't proc structure tool request", exception1);
			}
		}
		
	}

}
