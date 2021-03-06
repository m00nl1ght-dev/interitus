package m00nl1ght.interitus.structures;

import java.util.ArrayList;

import m00nl1ght.interitus.Interitus;
import m00nl1ght.interitus.util.VarBlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.Rotation;

public class StructurePackInfo {
	
	private static NBTTagCompound last;
	
	public static final ArrayList<String> lootlists = new ArrayList<String>();
	public static final  ArrayList<String> condtypes = new ArrayList<String>();
	public static final  ArrayList<StructureInfo> structures = new ArrayList<StructureInfo>();
	public static final  ArrayList<PackInfo> packs = new ArrayList<PackInfo>();
	
	public static PackInfo active;
	
	private StructurePackInfo() {}
	
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
		NBTTagList condlist = new NBTTagList();
		for (ConditionType ct : StructurePack.get().cond_types.values()) {
			condlist.appendTag(new NBTTagString(ct.getName()));
		}
		tag.setTag("c", condlist);
		NBTTagList structlist = new NBTTagList();
		for (Structure str : StructurePack.get().structures.values()) {
			NBTTagCompound t = new NBTTagCompound();
			t.setString("n", str.name);
			str.getSize(VarBlockPos.PUBLIC_CACHE, Rotation.NONE);
			t.setIntArray("s", VarBlockPos.PUBLIC_CACHE.toArray());
			t.setInteger("t", str.tasks.size());
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
	
	public static void fromNBT(NBTTagCompound tag) {
		NBTBase nbt = tag.getTag("act");
		if (nbt instanceof NBTTagCompound) {active = new PackInfo((NBTTagCompound) nbt);}
		packs.clear();
		NBTTagList list = tag.getTagList("p", 10);
		for (int i=0; i<list.tagCount(); i++) {
			packs.add(new PackInfo(list.getCompoundTagAt(i)));
		}
		lootlists.clear();
		NBTTagList lootlist = tag.getTagList("l", 8);
		for (int i=0; i<lootlist.tagCount(); i++) {
			lootlists.add(lootlist.getStringTagAt(i));
		}
		condtypes.clear();
		NBTTagList condlist = tag.getTagList("c", 8);
		for (int i=0; i<condlist.tagCount(); i++) {
			condtypes.add(condlist.getStringTagAt(i));
		}
		structures.clear();
		NBTTagList structlist = tag.getTagList("s", 10);
		for (int i=0; i<structlist.tagCount(); i++) {
			structures.add(new StructureInfo(structlist.getCompoundTagAt(i)));
		}
	}
	
	public static boolean packExists(String name) {
		if (name.equals("Default")) {return true;}
		if (active.name.equals(name)) {return true;}
		for (PackInfo pack : packs) {
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
		public int genTasks;
		
		public StructureInfo(NBTTagCompound tag) {
			name = tag.getString("n");
			size = tag.getIntArray("s");
			genTasks = tag.getInteger("t");
		}
		
	}

}
