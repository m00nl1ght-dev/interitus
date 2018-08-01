package m00nl1ght.interitus;

import m00nl1ght.interitus.block.ModBlock;
import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure;
import m00nl1ght.interitus.block.tileentity.TileEntitySummoner;
import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure.LootEntryPrimer;
import m00nl1ght.interitus.client.ConditionTypeClient;
import m00nl1ght.interitus.client.GenTaskClient;
import m00nl1ght.interitus.client.gui.GuiEditAdvStructure;
import m00nl1ght.interitus.client.gui.GuiEditCondType;
import m00nl1ght.interitus.client.gui.GuiEditLootEntry;
import m00nl1ght.interitus.client.gui.GuiEditSummoner;
import m00nl1ght.interitus.client.gui.GuiGenTasks;
import m00nl1ght.interitus.client.gui.GuiStructureData;
import m00nl1ght.interitus.client.gui.GuiStructurePacks;
import m00nl1ght.interitus.item.ModItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
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
		GenTaskClient.initBiomes();
		ConditionTypeClient.initBlocks();
	}

	@Override
	public void serverStarting(FMLServerStartingEvent e) {
		
	}
	
	@Override
	public void serverStarted(FMLServerStartedEvent e) {
		
	}
	
	@Override
	public void serverStopped(FMLServerStoppedEvent e) {
		
	}

	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event) {
		ModItem.initModels(event);
	}
	
	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload event) {
		if (event.getWorld().provider.getDimension()==0) {
			ConditionTypeClient.clearMaterialList();
		}
	}

	@Override
	public String localize(String unlocalized, Object... args) {
		return I18n.format(unlocalized, args);
	}
	
	@Override
	public void displayAdvStructScreen(TileEntityAdvStructure te) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiEditAdvStructure(te));
	}
	
	@Override
	public void displaySummonerScreen(TileEntitySummoner te) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiEditSummoner(te));
	}
	
	@Override
	public void displayStructureDataScreen(TileEntityAdvStructure te) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiStructureData(te, null));
	}
	
	@Override
	public void displayStructureLootScreen(TileEntityAdvStructure te, LootEntryPrimer entry) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiEditLootEntry(te, null, entry));
	}

	@Override
	public void displayAdvStructScreen() {
		Minecraft.getMinecraft().displayGuiScreen(new GuiStructurePacks());
	}
	
	@Override
	public void displayGenTasksScreen(NBTTagCompound nbt, String struct) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiGenTasks(Minecraft.getMinecraft().currentScreen, struct, nbt));
	}

	@Override
	public void displayCondTypeScreen(NBTTagCompound tag, String type) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiEditCondType(Minecraft.getMinecraft().currentScreen, type, tag));
	}

}
