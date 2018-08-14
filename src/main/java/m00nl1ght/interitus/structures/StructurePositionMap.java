package m00nl1ght.interitus.structures;

import java.util.Map.Entry;

import it.unimi.dsi.fastutil.HashCommon;
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
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;


public class StructurePositionMap {

	private final StructureDataMap chunks = new StructureDataMap(256);
	private final InteritusChunkGenerator gen;
	private boolean finishing = false;
	
	public StructurePositionMap(InteritusChunkGenerator gen) {
		this.gen=gen;
	}
	
	public static void createDirect(World world, StructureData data) {
		data.str.getSize(VarBlockPos.PUBLIC_CACHE, data.rotation);
		VarBlockPos.PUBLIC_CACHE.varAdd(data.pos).varAdd(-1, -1, -1);
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
	
	public boolean findSuitablePosition(WorldGenTask task, VarBlockPos entry_pos,  Rotation rotation) {
		if (finishing) {return false;}
		task.getStructure().getSize(VarBlockPos.PUBLIC_CACHE, rotation);
		VarBlockPos.PUBLIC_CACHE.varAdd(entry_pos).varAdd(-1, -1, -1);
		int xmin = Math.min(entry_pos.getX() >> 4, VarBlockPos.PUBLIC_CACHE.getX() >> 4);
		int xmax = Math.max(entry_pos.getX() >> 4, VarBlockPos.PUBLIC_CACHE.getX() >> 4);
		int zmin = Math.min(entry_pos.getZ() >> 4, VarBlockPos.PUBLIC_CACHE.getZ() >> 4);
		int zmax = Math.max(entry_pos.getZ() >> 4, VarBlockPos.PUBLIC_CACHE.getZ() >> 4);
		
		if (!task.isChunkSuitable(xmin, zmin, chunks.get(Toolkit.intPairToLong(xmin, zmin)))) {
			return false;
		}
		
		int sx = xmax - xmin + 1, sz = zmax - zmin + 1; // size
		if (sx==1 && sz==1) {return true;}
		int ox = sx==1?0:1, oz = sz==1?0:1; // offset to try
		boolean f1 = sz==1, f2 = sx==1; // offset flags -> hit blocked chunk or border?
		IChunkProvider provider = gen.world.getChunkProvider();
		
		while (true) {
			if (!f1) for (int i = 0; i < ox; i++) {
				if (this.isChunkNotSuitable(provider, task, xmin + i, zmin + oz)) {oz--; f1 = true; break;}
			}
			if (!f2) for (int i = 0; i < oz; i++) {
				if (this.isChunkNotSuitable(provider, task, xmin + ox, zmin + i)) {ox--; f2 = true; break;}
			}
			if (!f1 && !f2) {
				if (this.isChunkNotSuitable(provider, task, xmin + ox, zmin + oz)) {
					if (sx>sz) {
						oz--; f1 = true;
						if (ox < sx - 1) {ox++;} else {break;}
					} else {
						ox--; f2 = true;
						if (oz < sz - 1) {oz++;} else {break;}
					}
				} else {
					if (oz < sz - 1) {oz++;} else {f1 = true;}
					if (ox < sx - 1) {ox++;} else {if (f1) break; f2 = true; }
				}
			} else if (!f1) {
				if (oz < sz - 1) {oz++;} else {if (f2) break; f1 = true;}
			} else if (!f2) {
				if (ox < sx - 1) {ox++;} else {if (f1) break; f2 = true;}
			} else {
				break;
			}
		}

		for (int x = xmin - sx + ox + 1; x < xmin; x++) {
			for (int z = zmin - sz + oz + 1; z <= zmin + oz; z++) {
				if (this.isChunkNotSuitable(provider, task, x, z)) {return false;}
			}
		}
		
		for (int x = xmin; x <= xmin + ox; x++) {
			for (int z = zmin - sz + oz + 1; z < zmin; z++) {
				if (this.isChunkNotSuitable(provider, task, x, z)) {return false;}
			}
		}
		
		return true;
	}
	
	private boolean isChunkNotSuitable(IChunkProvider provider, WorldGenTask task, int x, int z) {
		return (provider.isChunkGeneratedAt(x, z) && provider.provideChunk(x, z).isTerrainPopulated()) ||
				!task.isChunkSuitable(x, z, chunks.get(Toolkit.intPairToLong(x, z)));
	}
	
	public void create(StructureData data) {
		if (finishing) {throw new IllegalStateException("Finishing pending structures");}
		data.str.getSize(VarBlockPos.PUBLIC_CACHE, data.rotation);
		VarBlockPos.PUBLIC_CACHE.varAdd(data.pos).varAdd(-1, -1, -1); 
		int xmin = Math.min(data.pos.getX() >> 4, VarBlockPos.PUBLIC_CACHE.getX() >> 4);
		int xmax = Math.max(data.pos.getX() >> 4, VarBlockPos.PUBLIC_CACHE.getX() >> 4);
		int zmin = Math.min(data.pos.getZ() >> 4, VarBlockPos.PUBLIC_CACHE.getZ() >> 4);
		int zmax = Math.max(data.pos.getZ() >> 4, VarBlockPos.PUBLIC_CACHE.getZ() >> 4);
		data.str.instances.add(data);
		for (int x = xmin; x <= xmax; x++) {
			for (int z = zmin; z <= zmax; z++) {
				chunks.putOrAppend(Toolkit.intPairToLong(x, z), data);
			}
		}
		if (Interitus.config.debugWorldgen) Toolkit.serverBroadcastMsg("("+data.str.name+") created structure at "+data.pos.toString()+" ("+(xmax-xmin+1)*(zmax-zmin+1)+" chunks)");
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
	
	private static class StructureDataMap extends Long2ObjectOpenHashMap<IStructureData> {
		
		public StructureDataMap(int expected) {
			super(expected);
		}

		private int insert(final long k, final IStructureData v) {
			int pos;
			if (((k) == (0))) {
				if (containsNullKey) return n;
				containsNullKey = true;
				pos = n;
			} else {
				long curr;
				final long[] key = this.key;
				// The starting point.
				if (!((curr = key[pos = (int) it.unimi.dsi.fastutil.HashCommon.mix((k)) & mask]) == (0))) {
					if (((curr) == (k))) return pos;
					while (!((curr = key[pos = (pos + 1) & mask]) == (0)))
						if (((curr) == (k))) return pos;
				}
			}
			key[pos] = k;
			value[pos] = v;
			if (size++ >= maxFill) rehash(HashCommon.arraySize(size + 1, f));
			return -1;
		}

		public IStructureData putOrAppend(final long k, final StructureData v) {
			final int pos = insert(k, v);
			if (pos < 0) return v;
			value[pos] = value[pos].add(v);
			return value[pos];
		}
		
	}
	
}
