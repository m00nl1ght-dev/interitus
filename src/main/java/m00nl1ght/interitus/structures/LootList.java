package m00nl1ght.interitus.structures;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import javax.annotation.Nonnull;

import org.apache.commons.io.IOUtils;

import m00nl1ght.interitus.Interitus;
import m00nl1ght.interitus.structures.LootList.LootEntry;
import m00nl1ght.interitus.util.RandomCollection;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;


public class LootList extends RandomCollection<LootEntry> {
	
	public final String name;
	
	public LootList(String name) {
		this.name=name;
	}

	public ItemStack get() {
		LootEntry loot = this.next();
		return loot==null?ItemStack.EMPTY:loot.build();
	}
	
	public LootList add(LootEntry entry) {
		this.add(entry.weight, entry);
		return this;
	}
	
	public LootList loadFromNBT(NBTTagCompound tag) {
		NBTTagList list = tag.getTagList("items", 10);
		this.clear();
		for (int i=0; i<list.tagCount(); i++) {
			LootEntry loot = new LootEntry(list.getCompoundTagAt(i));
			this.add(loot.weight, loot);
		}
		return this;
	}
	
	
	public static LootList loadFromFile(File folder, String name) {
		File file = new File(folder, name + ".nbt");
		if (!file.exists()) {
			Interitus.logger.error("Could not load loot list (file not found): " + file.getPath());
			return null;
		} else {
			InputStream inputstream = null;
			try {
				inputstream = new FileInputStream(file);
				NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(inputstream);
			    LootList template = new LootList(name);
			    template.loadFromNBT(nbttagcompound);
				return template;
			} catch (Throwable var10) {
				Interitus.logger.error("Could not load loot list (nbt/stream error): " + name);
			} finally {
				IOUtils.closeQuietly(inputstream);
			}
			return null;
		}
	}
	
	public NBTTagCompound saveToNBT(NBTTagCompound tag) {
		NBTTagList list = new NBTTagList();
		for (LootEntry entry : entries()) {
			list.appendTag(entry.toNBT());
		}
		tag.setTag("items", list);
		return tag;
	}
	
	public void saveToFile(File folder) {
		File file = new File(folder, name+".nbt");
		folder.mkdirs();
		OutputStream outputstream = null;
		try {
			NBTTagCompound nbttagcompound = saveToNBT(new NBTTagCompound());
			outputstream = new FileOutputStream(file);
			CompressedStreamTools.writeCompressed(nbttagcompound, outputstream);
		} catch (Throwable var13) {
			var13.printStackTrace();
		} finally {
			IOUtils.closeQuietly(outputstream);
		}
	}
	
	public static boolean putInRandomEmptySlot(IInventory inv, ItemStack stack, Random rand) {
		int s=inv.getSizeInventory(), idx = rand.nextInt(s), i=idx;
		while (!inv.getStackInSlot(i).isEmpty()) {i++; if (i>=s) {i=0;} if (i==idx) {return false;}}
		inv.setInventorySlotContents(i, stack);
        return true;
    }
	
	public static class LootEntry {
		public final Item item;
		public final int min, max, meta;
		public final NBTTagCompound nbt;
		public final double weight;
		public LootEntry(@Nonnull Item item, double weight, int minCount, int maxCount, int meta, NBTTagCompound nbtData) {
			this.item=item; this.weight=weight; this.max=maxCount; this.min=minCount; this.meta=meta; this.nbt=nbtData;
		}
		public LootEntry(NBTTagCompound tag) {
			item = Item.getByNameOrId(tag.getString("item"));
			nbt = tag.hasKey("nbt", 10)?tag.getCompoundTag("nbt"):null;
			weight = tag.getDouble("weight");
			min = tag.getInteger("min"); max = tag.getInteger("max"); meta = tag.getInteger("meta");  
			if (item==null || weight<=0D || min<=0 || max<=0 || min>max) {throw new IllegalArgumentException("Invalid item entry in loot list: "+this.toString());}
		}
		public LootEntry(ItemStack stack, double weight, int min, int max) {
			this(stack.getItem(), weight, min, max, stack.getMetadata(), stack.getTagCompound());
		}
		public ItemStack build() {
//			if (item==ModItem.RUNE) {
//				return ItemRune.getRandomRune(min);
//			}
			ItemStack stack = new ItemStack(item, min==max?min:min+Interitus.random.nextInt(max-min+1), meta, null);
			if (nbt!=null) {stack.setTagCompound(nbt.copy());}
			return stack;
		}
		public NBTTagCompound toNBT() {
			NBTTagCompound tag = new NBTTagCompound();
			ResourceLocation loc = Item.REGISTRY.getNameForObject(this.item);
			if (loc==null) {throw new IllegalStateException("Unknown item in loot entry: "+item);}
			tag.setString("item", loc.toString());
			tag.setInteger("min", min); tag.setInteger("max", max); tag.setInteger("meta", meta);
			tag.setDouble("weight", weight);
			if (nbt!=null && !nbt.hasNoTags()) {tag.setTag("nbt", nbt);}
			return tag;
		}
		@Override
		public String toString() {
			return "LootEntry {item:"+item.getRegistryName()+" min:"+min+" max:"+max+" meta:"+meta+" weight:"+weight+"}";
		}
	}

}
