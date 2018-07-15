package m00nl1ght.interitus.structures;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import m00nl1ght.interitus.Interitus;
import m00nl1ght.interitus.structures.Structure.StructureData;
import m00nl1ght.interitus.util.Toolkit;
import m00nl1ght.interitus.world.InteritusChunkGenerator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;


public class StructurePositionMap {

	private final Long2ObjectOpenHashMap<StructureData> chunks = new Long2ObjectOpenHashMap<StructureData>(1024);
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
		if (!noPermanentSave) {
			data.str.instances.add(data);
		}
		for (int x = xmin; x <= xmax; x++) {
			for (int z = zmin; z <= zmax; z++) {
				if (!noPermanentSave) {chunks.put(ChunkPos.asLong(x, z), data);}
				if (placeInWorld && gen.world.isChunkGeneratedAt(x, z)) {
					Chunk chunk =  gen.world.getChunkFromChunkCoords(x, z);
					if (chunk.isTerrainPopulated()) {
						data.str.placeInChunk(chunk, data); insta++;
					}
				} c++;
			}
		}
		if (Interitus.config.debugWorldgen) Toolkit.serverBroadcastMsg("("+data.str.name+") created structure at "+data.pos.toString()+" (instantly "+insta+" of "+c+")");
		return true;
	}
	
	public boolean place(Chunk chunk) {
		StructureData data = chunks.get(ChunkPos.asLong(chunk.x, chunk.z));
		if (data!=null) {
			data.str.placeInChunk(chunk, data);
			return true;
		}
		return false;
	}
	
	public boolean place(int x, int z) {
		StructureData data = chunks.get(ChunkPos.asLong(x, z));
		if (data!=null) {
			data.str.placeInChunk(gen.world.getChunkFromChunkCoords(x, z), data);
			return true;
		}
		return false;
	}
	
	public int chunkCount() {
		return chunks.size();
	}

	public void writeToNBT(NBTTagCompound nbt) {
		for (Structure str : StructurePack.current.structures.values()) {
			if (str.instances.isEmpty()) {continue;}
			NBTTagList list = new NBTTagList();
			for (StructureData data : str.instances) {
				list.appendTag(new NBTTagLong(data.pos.toLong()));
			}
			nbt.setTag(str.name, list);
		}
	}
	
	public void readFromNBT(NBTTagCompound nbt) {
		for (String key : nbt.getKeySet()) {
			Structure struct = StructurePack.getStructure(key);
			if (struct==null) {Interitus.logger.warn("World data contains unknown structure data: structure <"+key+"> not found!"); continue;}
			NBTTagList list = nbt.getTagList(key, 4);
			for (int i=0; i<list.tagCount(); i++) {
				this.create(new StructureData(struct, BlockPos.fromLong(((NBTTagLong)list.get(i)).getLong())), false);
			}
		}
	}
	
}
