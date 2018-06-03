package m00nl1ght.interitus.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import m00nl1ght.interitus.Interitus;
import m00nl1ght.interitus.structures.Structure.StructureData;
import m00nl1ght.interitus.world.InteritusChunkGenerator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;


public class StructurePositionMap {

	private final Long2ObjectOpenHashMap<StructureData> chunks = new Long2ObjectOpenHashMap<StructureData>(1024);
	private final HashMap<String, ArrayList<StructureData>> struct = new HashMap<String, ArrayList<StructureData>>();
	private final InteritusChunkGenerator gen;
	
	public StructurePositionMap(InteritusChunkGenerator gen) {
		this.gen=gen;
	}
	
	public boolean create(StructureData data) {
		return this.create(data, true);
	}
	
	public boolean create(StructureData data, boolean placeInWorld) {
		return this.create(data, placeInWorld, false);
	}
	
	public boolean create(StructureData data, boolean placeInWorld, boolean noPermanentSave) {
		BlockPos end = data.pos.add(data.str.getSize(data.rotation)); int insta=0, c=0;
		int xmin = Math.min(data.pos.getX() >> 4, end.getX() >> 4);
		int xmax = Math.max(data.pos.getX() >> 4, end.getX() >> 4);
		int zmin = Math.min(data.pos.getZ() >> 4, end.getZ() >> 4);
		int zmax = Math.max(data.pos.getZ() >> 4, end.getZ() >> 4);
		for (int x = xmin; x <= xmax; x++) {
			for (int z = zmin; z <= zmax; z++) {
				if (chunks.containsKey(ChunkPos.asLong(x, z))) {
					return false;
				}
			}
		}
		ArrayList<StructureData> list = struct.get(data.str.name);
		if (!noPermanentSave) {
			if (list==null) {struct.put(data.str.name, list = new ArrayList());}
			list.add(data);
		}
		for (int x = xmin; x <= xmax; x++) {
			for (int z = zmin; z <= zmax; z++) {
				if (!noPermanentSave) {chunks.put(ChunkPos.asLong(x, z), data);}
				if (placeInWorld && gen.world.isChunkGeneratedAt(x, z) && gen.world.getChunkFromChunkCoords(x, z).isTerrainPopulated()) {
					data.str.placeInChunk(gen.world, data, x, z); insta++;
				} c++;
			}
		}
		//if (placeInWorld) Toolkit.serverBroadcastMsg("("+data.str.name+") created structure at "+data.pos.toString()+" (instantly "+insta+" of "+c+")");
		return true;
	}
	
	public void place(BlockPos blockpos) {
		place(blockpos.getX() >> 4, blockpos.getZ() >> 4);
	}
	
	public boolean place(int x, int z) {
		StructureData data = chunks.get(ChunkPos.asLong(x, z));
		if (data!=null) {
			data.str.placeInChunk(gen.world, data, x, z);
			return true;
		}
		return false;
	}
	
	public boolean nearOccurence(Structure str, BlockPos pos, int maxDistance) {
		ArrayList<StructureData> list = struct.get(str.name);
		if (list==null || list.isEmpty()) {return false;}
		for (StructureData str1 : list) {
			int a = str1.pos.getX() - pos.getX(), b = str1.pos.getZ() - pos.getZ();
			if (Math.sqrt(a*a+b*b)<maxDistance) {return true;}
		}
		return false;
	}
	
	public int chunkCount() {
		return chunks.size();
	}

	public void writeToNBT(NBTTagCompound nbt) {
		for (Entry<String, ArrayList<StructureData>> entry : struct.entrySet()) {
			if (entry.getValue().isEmpty()) {continue;}
			NBTTagList list = new NBTTagList();
			for (StructureData data : entry.getValue()) {
				list.appendTag(new NBTTagLong(data.pos.toLong()));
			}
			nbt.setTag(entry.getKey(), list);
		}
	}
	
	public void readFromNBT(NBTTagCompound nbt) {
		for (String key : nbt.getKeySet()) {
			Structure struct = StructurePack.getStructure(key);
			if (struct==null) {Interitus.logger.error("World data contains unknown structure data: structure <"+key+"> not found!");}
			NBTTagList list = nbt.getTagList(key, 4);
			for (int i=0; i<list.tagCount(); i++) {
				this.create(new StructureData(struct, BlockPos.fromLong(((NBTTagLong)list.get(i)).getLong())), false);
			}
		}
	}
	
}
