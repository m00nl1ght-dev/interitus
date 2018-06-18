package m00nl1ght.interitus.crafting;

import m00nl1ght.interitus.block.ModBlock;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;


public class ModCreativeTab extends CreativeTabs {
	
	public ModCreativeTab(String unlocalizedName) {
        super(unlocalizedName);
    }

	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(ModBlock.itemAdvStructure);
	}

}
