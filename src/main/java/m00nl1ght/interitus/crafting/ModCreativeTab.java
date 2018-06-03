package m00nl1ght.interitus.crafting;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;


public class ModCreativeTab extends CreativeTabs {
	
	private ItemStack displayStack;
	
	public ModCreativeTab(String unlocalizedName, ItemStack stack) {
        this(unlocalizedName);
        this.displayStack = stack;
    }
	
	public ModCreativeTab(String unlocalizedName) {
        super(unlocalizedName);
    }

	@Override
	public ItemStack getTabIconItem() {
		return displayStack;
	}

}
