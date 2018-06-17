package m00nl1ght.interitus;

import m00nl1ght.interitus.block.ModBlock;
import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure;
import m00nl1ght.interitus.block.tileentity.TileEntitySummoner;
import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure.LootEntryPrimer;
import m00nl1ght.interitus.client.gui.GuiEditAdvStructure;
import m00nl1ght.interitus.client.gui.GuiEditLootEntry;
import m00nl1ght.interitus.client.gui.GuiEditSummoner;
import m00nl1ght.interitus.client.gui.GuiStructureData;
import m00nl1ght.interitus.client.gui.GuiStructurePacks;
import m00nl1ght.interitus.item.ModItem;
import m00nl1ght.interitus.structures.StructurePackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
public class ClientProxy implements IProxy {
	
	public static final String ref = "m00nl1ght.interitus.ClientProxy";

	@Override
	public void preInit(FMLPreInitializationEvent e) {
		
	}

	@Override
	public void init(FMLInitializationEvent e) {
		
	}
	
	@Override
	public void postInit(FMLPostInitializationEvent e) {
		ModBlock.initTileEntityRenderers();
	}

	@Override
	public void serverStarting(FMLServerStartingEvent e) {
		
	}
	
	@Override
	public void serverStarted(FMLServerStartedEvent e) {
		
	}

	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event) {
		ModItem.initModels(event);
	}

	@Override
	public String localize(String unlocalized, Object... args) {
		return I18n.format(unlocalized, args);
	}
	
	@Override
	public void displayAdvStructScreen(TileEntityAdvStructure te, StructurePackInfo packInfo) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiEditAdvStructure(te, packInfo));
	}
	
	@Override
	public void displaySummonerScreen(TileEntitySummoner te) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiEditSummoner(te));
	}
	
	@Override
	public void displayStructureDataScreen(TileEntityAdvStructure te, StructurePackInfo packInfo) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiStructureData(te, null, packInfo));
	}
	
	@Override
	public void displayStructureLootScreen(TileEntityAdvStructure te, LootEntryPrimer entry, StructurePackInfo packInfo) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiEditLootEntry(te, null, entry, packInfo));
	}

	@Override
	public void displayAdvStructScreen(StructurePackInfo packInfo) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiStructurePacks(packInfo));
	}

}
