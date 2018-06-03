package m00nl1ght.interitus;

import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure;
import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure.LootEntryPrimer;
import m00nl1ght.interitus.block.tileentity.TileEntitySummoner;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

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
	public String localize(String unlocalized, Object... args) {
		return I18n.translateToLocalFormatted(unlocalized, args);
	}

	@Override
	public void displayAdvStructScreen(TileEntityAdvStructure te) {}

	@Override
	public void displaySummonerScreen(TileEntitySummoner te) {}

	@Override
	public void displayStructureDataScreen(TileEntityAdvStructure te, GuiScreen parent) {}

	@Override
	public void displayStructureLootScreen(TileEntityAdvStructure te, LootEntryPrimer entry) {}

}
