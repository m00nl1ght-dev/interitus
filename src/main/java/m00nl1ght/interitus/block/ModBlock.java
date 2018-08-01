package m00nl1ght.interitus.block;

import m00nl1ght.interitus.Interitus;
import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure;
import m00nl1ght.interitus.block.tileentity.TileEntitySummoner;
import m00nl1ght.interitus.client.TESRAdvStructure;
import m00nl1ght.interitus.item.ModItem;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public final class ModBlock {
	
	public static Block blockAdvStructure, blockSummoner;
	public static Item itemAdvStructure, itemSummoner;

    public static void initBlocks(RegistryEvent.Register<Block> event) {
    	blockAdvStructure = new BlockAdvStructure("advanced_structure").setRegistryName(Interitus.MODID,"advanced_structure");
    	blockSummoner = new BlockSummoner("summoner").setRegistryName(Interitus.MODID,"summoner");
    	itemAdvStructure = ModItem.create(new ItemBlock(blockAdvStructure),"advanced_structure");
    	itemSummoner = ModItem.create(new ItemBlock(blockSummoner),"summoner");
    	event.getRegistry().register(blockAdvStructure);
    	event.getRegistry().register(blockSummoner);
    }
    
    public static void initTileEntities() {
    	TileEntity.register("advanced_structure", TileEntityAdvStructure.class);
    	TileEntity.register("summoner", TileEntitySummoner.class);
    }
    
    @SideOnly(Side.CLIENT)
    public static void initTileEntityRenderers() {
    	TileEntityRendererDispatcher.instance.renderers.put(TileEntityAdvStructure.class, new TESRAdvStructure(TileEntityRendererDispatcher.instance));
    }

}
