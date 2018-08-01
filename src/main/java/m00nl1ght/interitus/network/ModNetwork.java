package m00nl1ght.interitus.network;

import m00nl1ght.interitus.Interitus;
import m00nl1ght.interitus.network.CDefaultPackage.CDefaultPackageHandler;
import m00nl1ght.interitus.network.SDefaultPackage.SDefaultPackageHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class ModNetwork {
	
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Interitus.MODID);
	
	public static void init() {
		INSTANCE.registerMessage(CDefaultPackageHandler.class, CDefaultPackage.class, 1, Side.SERVER);
		INSTANCE.registerMessage(SDefaultPackageHandler.class, SDefaultPackage.class, 2, Side.CLIENT);
	}

}
