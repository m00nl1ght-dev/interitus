package m00nl1ght.interitus.network;

import m00nl1ght.interitus.Interitus;
import m00nl1ght.interitus.network.ClientPackage.ClientPackageHandler;
import m00nl1ght.interitus.network.ServerPackage.ServerPackageHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class ModNetwork {
	
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Interitus.MODID);
	
	public static void init() {
		INSTANCE.registerMessage(ServerPackageHandler.class, ServerPackage.class, 1, Side.SERVER);
		INSTANCE.registerMessage(ClientPackageHandler.class, ClientPackage.class, 2, Side.CLIENT);
	}

}
