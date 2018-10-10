package m00nl1ght.interitus.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public abstract class BasicPackage<E extends Enum<?>> implements IMessage {

	public BasicPackage() {}
	
	private int channel;
	private PacketBuffer data;
	
	public BasicPackage(E channel, PacketBuffer data) {
		this.channel = channel.ordinal();
		this.data = data;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeInt(channel);
        synchronized(this.data) {
        	this.data.markReaderIndex();
        	buffer.writeBytes(this.data);
        	this.data.resetReaderIndex();
        }
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		this.channel = buffer.readInt();
        int i = buffer.readableBytes();
        if (i >= 0) {
            this.data = new PacketBuffer(buffer.readBytes(i));
        }
	}
	
	public int getInternalID() {
		return this.channel;
	}

    public PacketBuffer getData() {
        return this.data;
    }
	
}
