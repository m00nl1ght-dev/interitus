package m00nl1ght.interitus;

import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure;
import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure.LootEntryPrimer;
import m00nl1ght.interitus.block.tileentity.TileEntitySummoner;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;

public class ServerProxy implements IProxy {
	
	public static final String ref = "m00nl1ght.interitus.ServerProxy";

	@Override
	public void preInit(FMLPreInitializationEvent e) {
		
	}

	@Override
	public void init(FMLInitializationEvent e) {
		
	}

	@Override
	public void postInit(FMLPostInitializationEvent e) {
		
	}

	@Override
	public void serverStarted(FMLServerStartedEvent e) {
		
	}

	@Override
	public void serverStarting(FMLServerStartingEvent e) {
		
	}
	
	@Override
	public void serverStopped(FMLServerStoppedEvent e) {
		
	}

	@Override
	public String localize(String unlocalized, Object... args) {
		return I18n.translateToLocalFormatted(unlocalized, args);
	}

	@Override
	public void displayAdvStructScreen(TileEntityAdvStructure te) {
		throw new UnsupportedOperationException("no ui on server side");
	}

	@Override
	public void displaySummonerScreen(TileEntitySummoner te) {
		throw new UnsupportedOperationException("no ui on server side");
	}

	@Override
	public void displayStructureDataScreen(TileEntityAdvStructure te) {
		throw new UnsupportedOperationException("no ui on server side");
	}

	@Override
	public void displayStructureLootScreen(TileEntityAdvStructure te, LootEntryPrimer entry) {
		throw new UnsupportedOperationException("no ui on server side");
	}

	@Override
	public void displayAdvStructScreen() {
		throw new UnsupportedOperationException("no ui on server side");
	}

	@Override
	public void displayGenTasksScreen(NBTTagCompound nbt, String struct) {
		throw new UnsupportedOperationException("no ui on server side");
	}

	@Override
	public void displayCondTypeScreen(NBTTagCompound tag, String type) {
		throw new UnsupportedOperationException("no ui on server side");
	}

}
