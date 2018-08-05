package m00nl1ght.interitus.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import m00nl1ght.interitus.Interitus;
import m00nl1ght.interitus.structures.StructurePack;
import m00nl1ght.interitus.structures.StructurePositionMap;
import m00nl1ght.interitus.structures.WorldGenTask;
import m00nl1ght.interitus.util.IDebugObject;
import m00nl1ght.interitus.util.InteritusProfiler;
import m00nl1ght.interitus.util.Toolkit;
import m00nl1ght.interitus.util.VarBlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;

public abstract class InteritusChunkGenerator implements IChunkGenerator, IDebugObject {
	
	public static final int heightMapOffset = 2;
	
	public final World world;
	public final Random random = new Random();
	private final Long2ObjectLinkedOpenHashMap<Chunk> chunkCache = new Long2ObjectLinkedOpenHashMap<Chunk>(512);
	private final Map<Biome, ArrayList<WorldGenTask>> genTasks;
	protected final StructurePositionMap structures = new StructurePositionMap(this);
	private boolean createStruct = true;
	
	public int gAll, gDone, gRange, gCond, gVstruct; // debug
	
	public InteritusChunkGenerator(World world) {
		this.world = world;
		this.genTasks = StructurePack.initGen(this);
	}
	
	public abstract Chunk generateChunk(int x, int z, boolean pre);
	
	@Override
	public Chunk generateChunk(int x, int z) {
		Chunk chunk = getChunkFromCache(x, z, true);
		if (chunk==null) {
			chunk = generateChunk(x, z, false);
		}
		this.createStructures(x, z);
		return chunk;
	}
	
	protected Chunk preGenerate(int x, int z) { // TODO investigate -> thats could mess up vanilla structures though
		Chunk chunk = generateChunk(x, z, true);
		chunkCache.put(Toolkit.intPairToLong(x, z), chunk);
		if (chunkCache.size()>Interitus.config.chunkCacheMaxSize) {
			this.gcCache(Interitus.config.chunkCacheShrink);
		}
		return chunk;
	}

	@Override
	public abstract void populate(int x, int z);

	@Override
	public abstract boolean generateStructures(Chunk chunkIn, int x, int z);

	@Override
	public abstract List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos);

	@Override
	public abstract BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored);

	@Override
	public abstract void recreateStructures(Chunk chunkIn, int x, int z);

	@Override
	public abstract boolean isInsideStructure(World worldIn, String structureName, BlockPos pos);
	
	public void writeToNBT(NBTTagCompound tag) {
		NBTTagCompound struct = new NBTTagCompound();
		structures.writeToNBT(struct);
		tag.setTag("struct", struct);
	}

	public void readFromNBT(NBTTagCompound tag) {
		NBTTagCompound struct = tag.getCompoundTag("struct");
		if (struct!=null) {structures.readFromNBT(struct);}
	}
	
	protected void createStructures(int x, int z) {
		if (!this.createStruct) {return;}
		Biome biome = this.world.getBiomeProvider().getBiome(VarBlockPos.PUBLIC_CACHE.set(8+x*16, 0, 8+z*16));
		ArrayList<WorldGenTask> list = genTasks.get(biome);
		if (list==null) {return;}
		for (WorldGenTask task : list) {
			if (task.apply(this, x, z)) {break;}
		}
	}
	
	public void finishPendingStructures() {
		Interitus.logger.info("Finishing all pending structures, this may take a moment ...");
		this.createStruct = false;
		this.structures.finishPending();
		this.createStruct = true;
		Interitus.logger.info("Finished pending structures.");
	}

	public Chunk getChunkFromCache(int x, int z, boolean remove) {
		if (remove) {
			return chunkCache.remove(Toolkit.intPairToLong(x, z));
		} else {
			return chunkCache.get(Toolkit.intPairToLong(x, z));
		}
	}

	public int cacheSize() {
		return chunkCache.size();
	}
	
	public void gcCache(int shrink) {
		if (shrink<=0) {return;}
		while (!chunkCache.isEmpty() && shrink>0) {
			chunkCache.removeFirst(); shrink--;
		}
	}

	public Chunk getChunk(int x, int z) {
		if (world.getChunkProvider().isChunkGeneratedAt(x, z)) {
			return world.getChunkFromChunkCoords(x, z);
		} else {
			long c = Toolkit.intPairToLong(x, z);
			if (chunkCache.containsKey(c)) {
				return chunkCache.get(c);
			} else {
				return this.preGenerate(x, z);
			}
		}
	}

	public IBlockState getBlockState(BlockPos pos) {
		if (pos.getY() < 0 || pos.getY() >= 256) {return Blocks.AIR.getDefaultState();}
		return this.getChunk(pos.getX() >> 4, pos.getZ() >> 4).getBlockState(pos);
	}

	public int getHeight(BlockPos pos) {
		return this.getChunk(pos.getX() >> 4, pos.getZ() >> 4).getHeight(pos);
	}
	
	public static InteritusChunkGenerator get(World world) {
		if (world.getChunkProvider() instanceof ChunkProviderServer) {
			ChunkProviderServer provider = (ChunkProviderServer) world.getChunkProvider();
			return provider.chunkGenerator instanceof InteritusChunkGenerator?(InteritusChunkGenerator)provider.chunkGenerator:null;
		} else {
			return null;
		}
	}
	
	public final StructurePositionMap getStructurePositionMap() {
		return this.structures;
	}
	
	@Override
	public abstract String toString();
	
	@Override
	public void debugMsg(ICommandSender sender) {
		InteritusProfiler.send(sender, "> DIM"+this.world.provider.getDimension()+" > "+this.toString());
		InteritusProfiler.send(sender, "struct [all: "+gAll+" ok: "+gDone+" range: "+gRange+" cond: "+gCond+" vStr: "+gVstruct+"]");
		InteritusProfiler.send(sender, "chunks cached: "+chunkCache.size()+" pending: "+structures.chunkCount());
	}

}
