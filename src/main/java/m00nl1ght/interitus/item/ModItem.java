package m00nl1ght.interitus.item;

import java.util.ArrayList;
import java.util.List;

import m00nl1ght.interitus.Main;
import m00nl1ght.interitus.crafting.ModCrafting;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public final class ModItem {
	
	public static List<Item> items = new ArrayList<Item>();

	public static ItemStructureDataTool STRUCTURE_TOOL;
		
	public static final <T extends Item> T create(T item, String name) {
		item.setUnlocalizedName(name).setRegistryName(Main.MODID, name);
		items.add(item); return item;
	}
	
    public static final void initItems(RegistryEvent.Register<Item> event) {
    	STRUCTURE_TOOL = create(new ItemStructureDataTool(),"structure_tool");
    	for (Item item : items) {
    		event.getRegistry().register(item);
    	}
    }
    
    public static final void applyItemSettings() {
    	for (Item item : items) {
    		item.setCreativeTab(ModCrafting.modTab);
    	}
    }
    
    @SideOnly(Side.CLIENT)
    public static final void initModels(ModelRegistryEvent event) {
    	for (Item item : items) {
    		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    	}
    }
    
}
