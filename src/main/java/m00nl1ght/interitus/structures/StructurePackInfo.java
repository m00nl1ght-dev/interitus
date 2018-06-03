package m00nl1ght.interitus.structures;

import java.util.ArrayList;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class StructurePackInfo {
	
	public ArrayList<String> lootlists = new ArrayList<String>();
	public ArrayList<StructureInfo> structures = new ArrayList<StructureInfo>();
	public ArrayList<PackInfo> packs = new ArrayList<PackInfo>();
	public PackInfo active;
	
	public static NBTTagCompound create() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setTag("act", writePackInfo(StructurePack.current));
		NBTTagList list = new NBTTagList();
		for (StructurePack pack : StructurePack.packs.values()) {
			list.appendTag(writePackInfo(pack));
		}
		tag.setTag("p", list);
		return tag;
	}
	
	private static NBTTagCompound writePackInfo(StructurePack pack) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("n", pack.name);
		tag.setString("t", pack.getTitle());
		tag.setString("d", pack.getDescription());
		tag.setBoolean("f", pack.isReadOnly());
		tag.setFloat("v", pack.getVersion());
		return tag;
	}
	
	public static StructurePackInfo fromNBT(NBTTagCompound tag) {
		StructurePackInfo info = new StructurePackInfo();
		NBTBase nbt = tag.getTag("act");
		if (nbt instanceof NBTTagCompound) {info.active = new PackInfo((NBTTagCompound) nbt);}
		NBTTagList list = tag.getTagList("", 10);
		for (int i=0; i<list.tagCount(); i++) {
			info.packs.add(new PackInfo(list.getCompoundTagAt(i)));
		}
		return info;
	}
	
	public static class PackInfo {
		
		public final String name;
		public final boolean read_only;
		public final String title;
		public final String description;
		public final float version;;
		
		public PackInfo(NBTTagCompound tag) {
			this.name = tag.getString("n");
			this.title = tag.getString("t");
			this.description = tag.getString("d");
			this.read_only = tag.getBoolean("f");
			this.version = tag.getFloat("v");
		}
		
	}
	
	public static class StructureInfo {
		
		public StructureInfo(NBTTagCompound tag) {
			
		}
		
	}

}
