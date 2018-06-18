package m00nl1ght.interitus.structures;

import java.util.ArrayList;

import m00nl1ght.interitus.Interitus;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public class StructurePackInfo {
	
	private static NBTTagCompound last;
	
	public ArrayList<String> lootlists = new ArrayList<String>();
	public ArrayList<StructureInfo> structures = new ArrayList<StructureInfo>();
	public ArrayList<PackInfo> packs = new ArrayList<PackInfo>();
	public PackInfo active;
	
	public static NBTTagCompound create() {
		if (last!=null) {return last;} 
		NBTTagCompound tag = new NBTTagCompound();
		tag.setTag("act", writePackInfo(StructurePack.current));
		NBTTagList packlist = new NBTTagList();
		for (StructurePack pack : StructurePack.packs.values()) {
			if (pack.name.equals(StructurePack.current.name)) {continue;}
			packlist.appendTag(writePackInfo(pack));
		}
		if (!StructurePack.current.name.equals("Default")) {
			packlist.appendTag(writePackInfo(StructurePack.emptyPack));
		}
		tag.setTag("p", packlist);
		NBTTagList lootlist = new NBTTagList();
		for (LootList list : StructurePack.get().loot.values()) {
			lootlist.appendTag(new NBTTagString(list.name));
		}
		tag.setTag("l", lootlist);
		NBTTagList structlist = new NBTTagList();
		for (Structure str : StructurePack.get().structures.values()) {
			NBTTagCompound t = new NBTTagCompound();
			t.setString("n", str.name);
			BlockPos size = str.getSize(Rotation.NONE);
			t.setIntArray("s", new int[] {size.getX(), size.getY(), size.getZ()});
			structlist.appendTag(t);
		}
		tag.setTag("s", structlist);
		last = tag;
		return tag;
	}
	
	private static NBTTagCompound writePackInfo(StructurePack pack) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("n", pack.name);
		tag.setString("a", pack.getAuthor());
		tag.setString("d", pack.getDescription());
		tag.setBoolean("f", pack.isReadOnly());
		tag.setFloat("v", pack.getVersion());
		return tag;
	}
	
	public static StructurePackInfo fromNBT(NBTTagCompound tag) {
		StructurePackInfo info = new StructurePackInfo();
		NBTBase nbt = tag.getTag("act");
		if (nbt instanceof NBTTagCompound) {info.active = new PackInfo((NBTTagCompound) nbt);}
		NBTTagList list = tag.getTagList("p", 10);
		for (int i=0; i<list.tagCount(); i++) {
			info.packs.add(new PackInfo(list.getCompoundTagAt(i)));
		}
		NBTTagList lootlist = tag.getTagList("l", 8);
		for (int i=0; i<lootlist.tagCount(); i++) {
			info.lootlists.add(lootlist.getStringTagAt(i));
		}
		NBTTagList structlist = tag.getTagList("s", 10);
		for (int i=0; i<structlist.tagCount(); i++) {
			info.structures.add(new StructureInfo(structlist.getCompoundTagAt(i)));
		}
		return info;
	}
	
	public boolean packExists(String name) {
		if (name.equals("Default")) {return true;}
		if (this.active.name.equals(name)) {return true;}
		for (PackInfo pack : this.packs) {
			if (pack.name.equals(name)) {return true;}
		}
		return false;
	}
	
	public static void markDirty() {
		last=null;
	}
	
	public static class PackInfo {
		
		public final String name;
		public boolean read_only;
		public String author;
		public String description;
		public final float version;;
		
		public PackInfo(NBTTagCompound tag) {
			this.name = tag.getString("n");
			this.author = tag.getString("a");
			this.description = tag.getString("d");
			this.read_only = tag.getBoolean("f");
			this.version = tag.getFloat("v");
		}

		public PackInfo(String new_name, PackInfo from) {
			name = new_name;
			read_only = false;
			author = (from!=null && from.read_only)?from.author:Minecraft.getMinecraft().player.getName();
			description = (from==null || from.name.equals("Default"))?"":from.description;
			version = from==null?Interitus.SUPPORTED_PACK_VERSION_MAX:from.version;
		}
		
	}
	
	public static class StructureInfo {
		
		public final String name;
		public final int[] size;
		
		public StructureInfo(NBTTagCompound tag) {
			name = tag.getString("n");
			size = tag.getIntArray("s");
		}
		
	}

}
