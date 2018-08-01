package m00nl1ght.interitus.structures;

import java.util.Map.Entry;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import m00nl1ght.interitus.Interitus;
import m00nl1ght.interitus.structures.Structure.IStructureData;
import m00nl1ght.interitus.structures.Structure.StructureData;
import m00nl1ght.interitus.util.Toolkit;
import m00nl1ght.interitus.util.VarBlockPos;
import m00nl1ght.interitus.world.InteritusChunkGenerator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;


public class StructurePositionMap {

	private final Long2ObjectOpenHashMap<IStructureData> chunks = new Long2ObjectOpenHashMap<IStructureData>(512);
	private final InteritusChunkGenerator gen;
	private boolean finishing = false;
	
	public StructurePositionMap(InteritusChunkGenerator gen) {
		this.gen=gen;
	}
	
	public static void createDirect(World world, StructureData data) {
		data.str.getSize(VarBlockPos.PUBLIC_CACHE, data.rotation);
		VarBlockPos.PUBLIC_CACHE.varAdd(data.pos);
		int xmin = Math.min(data.pos.getX() >> 4, VarBlockPos.PUBLIC_CACHE.getX() >> 4);
		int xmax = Math.max(data.pos.getX() >> 4, VarBlockPos.PUBLIC_CACHE.getX() >> 4);
		int zmin = Math.min(data.pos.getZ() >> 4, VarBlockPos.PUBLIC_CACHE.getZ() >> 4);
		int zmax = Math.max(data.pos.getZ() >> 4, VarBlockPos.PUBLIC_CACHE.getZ() >> 4);
		for (int x = xmin; x <= xmax; x++) {
			for (int z = zmin; z <= zmax; z++) {
				data.str.placeInChunk(world.getChunkFromChunkCoords(x, z), data);
			}
		}
	}
	
	public boolean create(StructureData data) {
		if (finishing) {throw new IllegalStateException("Finishing pending structures");}
		data.str.getSize(VarBlockPos.PUBLIC_CACHE, data.rotation);
		VarBlockPos.PUBLIC_CACHE.varAdd(data.pos); 
		int insta=0; //debug
		int xmin = Math.min(data.pos.getX() >> 4, VarBlockPos.PUBLIC_CACHE.getX() >> 4);
		int xmax = Math.max(data.pos.getX() >> 4, VarBlockPos.PUBLIC_CACHE.getX() >> 4);
		int zmin = Math.min(data.pos.getZ() >> 4, VarBlockPos.PUBLIC_CACHE.getZ() >> 4);
		int zmax = Math.max(data.pos.getZ() >> 4, VarBlockPos.PUBLIC_CACHE.getZ() >> 4);
		for (int x = xmin; x <= xmax; x++) {
			for (int z = zmin; z <= zmax; z++) {
				if (chunks.containsKey(Toolkit.intPairToLong(x, z))) {
					return false;
				}
			}
		}
		data.str.instances.add(data);
		for (int x = xmin; x <= xmax; x++) {
			for (int z = zmin; z <= zmax; z++) {
				if (gen.world.isChunkGeneratedAt(x, z)) {
					Chunk chunk =  gen.world.getChunkFromChunkCoords(x, z);
					if (chunk.isTerrainPopulated()) {
						data.str.placeInChunk(chunk, data); insta++;
						continue;
					}
				}
				chunks.put(Toolkit.intPairToLong(x, z), data);
			}
		}
		if (Interitus.config.debugWorldgen) Toolkit.serverBroadcastMsg("("+data.str.name+") created structure at "+data.pos.toString()+" (instantly "+insta+" of "+(xmax-xmin+1)*(zmax-zmin+1)+")");
		return true;
	}
	
	public boolean place(Chunk chunk) {
		if (finishing) {throw new IllegalStateException("Finishing pending structures");}
		return this.place(chunk, chunks.remove(Toolkit.intPairToLong(chunk.x, chunk.z)));
	}
	
	public boolean place(int x, int z) {
		if (finishing) {throw new IllegalStateException("Finishing pending structures");}
		return this.place(gen.world.getChunkFromChunkCoords(x, z), chunks.remove(Toolkit.intPairToLong(x, z)));
	}
	
	private boolean place(Chunk chunk, IStructureData data) {
		if (data!=null) {
			StructureData single = data.getSingle();
			if (single!=null) {
				single.str.placeInChunk(chunk, single);
				return true;
			}
			StructureData[] multi = data.getMulti();
			if (multi!=null && multi.length>0) {
				for (StructureData data0 : multi) {
					data0.str.placeInChunk(chunk, data0);
				}
				return true;
			}
		}
		return false;
	}
	
	public void finishPending() {
		finishing = true;
		for (Entry<Long, IStructureData> entry : chunks.entrySet()) {
			this.place(gen.world.getChunkFromChunkCoords(Toolkit.longToIntPairA(entry.getKey()), Toolkit.longToIntPairB(entry.getKey())), entry.getValue());
		}
		chunks.clear();
		finishing = false;
	}
	
	public int chunkCount() {
		return chunks.size();
	}

	public void writeToNBT(NBTTagCompound nbt) {
		for (Entry<Long, IStructureData> entry : chunks.entrySet()) {
			entry.getValue().appendChunk(entry.getKey());
		}
		for (Structure str : StructurePack.current.structures.values()) {
			if (str.instances.isEmpty()) {continue;}
			NBTTagList list = new NBTTagList();
			for (StructureData data : str.instances) {
				data.saveToNBT(list);
			}
			nbt.setTag(str.name, list);
		}
	}
	
	public void readFromNBT(NBTTagCompound nbt) {
		if (finishing) {throw new IllegalStateException("Finishing pending structures");}
		chunks.clear();
		for (String key : nbt.getKeySet()) {
			Structure struct = StructurePack.getStructure(key);
			if (struct==null) {Interitus.logger.warn("World data contains unknown structure data: structure <"+key+"> not found!"); continue;}
			struct.instances.clear();
			NBTTagList list = nbt.getTagList(key, 10);
			for (int i=0; i<list.tagCount(); i++) {
				NBTTagCompound tag = list.getCompoundTagAt(i);
				StructureData data = new StructureData(struct, BlockPos.fromLong(tag.getLong("p")), tag.getByte("a"));
				struct.instances.add(data);
				if (tag.hasKey("c")) {
					NBTTagList ch = tag.getTagList("c", 4);
					for (int j = 0; j < ch.tagCount(); j++) {
						long c = ((NBTTagLong)ch.get(j)).getLong();
						IStructureData d = chunks.get(c);
						if (d==null) {
							chunks.put(c, data);
						} else {
							IStructureData d0 = d.add(data);
							if (d!=d0) {chunks.put(c, d0);}
						}
					}
				}
			}
		}
	}
	
}
