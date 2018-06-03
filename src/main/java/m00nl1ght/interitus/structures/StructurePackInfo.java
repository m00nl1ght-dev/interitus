package m00nl1ght.interitus.structures;

import java.util.ArrayList;

public class StructurePackInfo {
	
	ArrayList<String> lootlists = new ArrayList<String>();
	ArrayList<StructureInfo> structures = new ArrayList<StructureInfo>();
	ArrayList<PackInfo> packs = new ArrayList<PackInfo>();
	
	public static StructurePackInfo fromNBT() {
		StructurePackInfo info = new StructurePackInfo();
		
		return info;
	}
	
	public static class StructureInfo {
		
	}
	
	public static class PackInfo {
		
	}

}
