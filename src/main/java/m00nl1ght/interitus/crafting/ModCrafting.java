package m00nl1ght.interitus.crafting;

import m00nl1ght.interitus.block.ModBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;

public class ModCrafting {
	
	public static final ModCreativeTab modTab = new ModCreativeTab("Interitus", new ItemStack(ModBlock.blockAdvStructure));
	
	public static void init(RegistryEvent.Register<IRecipe> event) {
		
    }
	
}
