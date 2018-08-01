package m00nl1ght.interitus.util;

import net.minecraft.command.ICommandSender;

public interface IDebugObject {
	
	public void debugMsg(ICommandSender sender);
	
	public default void resetStats() {}

}
