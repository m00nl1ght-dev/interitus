package m00nl1ght.interitus;

import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure;
import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure.LootEntryPrimer;
import m00nl1ght.interitus.block.tileentity.TileEntitySummoner;
import m00nl1ght.interitus.structures.StructurePackInfo;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

public interface IProxy {
	
	public void preInit(FMLPreInitializationEvent e);

    public void init(FMLInitializationEvent e);

	public void postInit(FMLPostInitializationEvent e);
	
	public void serverStarted(FMLServerStartedEvent e);

    public void serverStarting(FMLServerStartingEvent e);
    
    public String localize(String unlocalized, Object... args);
    
	public void displayAdvStructScreen(TileEntityAdvStructure te, StructurePackInfo packInfo);

	public void displaySummonerScreen(TileEntitySummoner te);
	
	public void displayStructureDataScreen(TileEntityAdvStructure te, StructurePackInfo packInfo);
	
	public void displayStructureLootScreen(TileEntityAdvStructure te, LootEntryPrimer entry, StructurePackInfo packInfo);

	public void displayAdvStructScreen(StructurePackInfo packInfo);

}
