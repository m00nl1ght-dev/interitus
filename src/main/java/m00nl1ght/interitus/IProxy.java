package m00nl1ght.interitus;

import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure;
import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure.LootEntryPrimer;
import m00nl1ght.interitus.block.tileentity.TileEntitySummoner;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;

public interface IProxy {
	
	public void preInit(FMLPreInitializationEvent e);

    public void init(FMLInitializationEvent e);

	public void postInit(FMLPostInitializationEvent e);
	
	public void serverStarted(FMLServerStartedEvent e);

    public void serverStarting(FMLServerStartingEvent e);
    
	public void serverStopped(FMLServerStoppedEvent e);
    
    public String localize(String unlocalized, Object... args);
    
	public void displayAdvStructScreen(TileEntityAdvStructure te);

	public void displaySummonerScreen(TileEntitySummoner te);
	
	public void displayStructureDataScreen(TileEntityAdvStructure te);
	
	public void displayStructureLootScreen(TileEntityAdvStructure te, LootEntryPrimer entry);

	public void displayAdvStructScreen();
	
	public void displayGenTasksScreen(NBTTagCompound nbt, String struct);

	public void displayCondTypeScreen(NBTTagCompound tag, String type);

}
