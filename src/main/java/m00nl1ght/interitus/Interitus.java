package m00nl1ght.interitus;

import java.util.Random;

import org.apache.logging.log4j.Logger;

import m00nl1ght.interitus.block.ModBlock;
import m00nl1ght.interitus.item.ModItem;
import m00nl1ght.interitus.network.ModNetwork;
import m00nl1ght.interitus.structures.StructurePack;
import m00nl1ght.interitus.util.ModConfig;
import m00nl1ght.interitus.world.InteritusProfiler;
import m00nl1ght.interitus.world.capabilities.WorldDataCapabilityStorage;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;


@Mod(modid = Interitus.MODID, name = Interitus.MODNAME, version = Interitus.VERSION)
public class Interitus {

    public static final String MODID = "interitus";
    public static final String MODNAME = "Interitus";
    public static final String VERSION = "1.12.2-0.0.2";
    public static final float SUPPORTED_PACK_VERSION_MIN = 0.1F;
    public static final float SUPPORTED_PACK_VERSION_MAX = 0.1F;
    public static InteritusProfiler profiler = new InteritusProfiler();
    public static ModConfig config = new ModConfig();
    public static Random random = new Random();
    public static Logger logger;
    
    @Instance
    public static Interitus instance = new Interitus(); 
    
    @SidedProxy(clientSide=ClientProxy.ref, serverSide=ServerProxy.ref)
    public static IProxy proxy; 

    @EventHandler
    public void preInit(FMLPreInitializationEvent e) { 
    	logger = e.getModLog();
    	logger.info("PreInitialising "+MODNAME+" v"+VERSION+".");
        proxy.preInit(e);
    }

    @EventHandler
    public void init(FMLInitializationEvent e) {
    	logger.info("Initialising "+MODNAME+" v"+VERSION+".");
    	WorldDataCapabilityStorage.register();
    	ModBlock.initTileEntities();
    	ModItem.applyItemSettings();
    	ModNetwork.init();
        proxy.init(e);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e) {
    	logger.info("PostInitialising "+MODNAME+" v"+VERSION+".");
    	StructurePack.updateAvailbalePacks();
        proxy.postInit(e);
    }
    
    @EventHandler
	public void serverStarting(FMLServerStartingEvent e) {
    	e.registerServerCommand(new CommandHandler());
    	proxy.serverStarting(e);
	}
    
    @EventHandler
	public void serverStarted(FMLServerStartedEvent e) {
    	proxy.serverStarted(e);
	}
    
    @EventHandler
	public void serverStopped(FMLServerStoppedEvent e) {
    	StructurePack.loadDefault();
    	proxy.serverStopped(e);
	}

}
