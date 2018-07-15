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
import m00nl1ght.interitus.util.VarBlockPos;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;

public abstract class InteritusChunkGenerator implements IChunkGenerator {
	
	public static final int heightMapOffset = 2;
	
	public final World world;
	public final Random random = new Random();
	private final Long2ObjectLinkedOpenHashMap<Chunk> chunkCache = new Long2ObjectLinkedOpenHashMap<Chunk>(512);
	private final Map<Biome, ArrayList<WorldGenTask>> genTasks;
	protected final StructurePositionMap structures = new StructurePositionMap(this);
	private final VarBlockPos posCache = new VarBlockPos();
	
	public InteritusChunkGenerator(World world) {
		this.genTasks = StructurePack.getGenForDimension(world.provider.getDimension());
		this.world = world;
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
	
	protected Chunk preGenerate(int x, int z) { // thats could mess up vanilla structures though
		Chunk chunk = generateChunk(x, z, true);
		chunkCache.put(ChunkPos.asLong(x, z), chunk);
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
		Biome biome = this.world.getBiomeProvider().getBiome(posCache.set(8+x*16, 0, 8+z*16));
		ArrayList<WorldGenTask> list = genTasks.get(biome);
		if (list==null) {return;}
		for (WorldGenTask task : list) {
			if (task.apply(this, x, z)) {break;}
		}
	}

	public Chunk getChunkFromCache(int x, int z, boolean remove) {
		if (remove) {
			return chunkCache.remove(ChunkPos.asLong(x, z));
		} else {
			return chunkCache.get(ChunkPos.asLong(x, z));
		}
	}

	public int cacheSize() {
		return chunkCache.size();
	}
	
	public void gcCache(int shrink) {
		//int s = chunkCache.size(); //debug
		//int so = shrink;
		if (shrink<=0) {return;}
		while (!chunkCache.isEmpty() && shrink>0) {
			chunkCache.removeFirst(); shrink--;
		}
	}

	public Chunk getChunk(int x, int z) {
		if (world.getChunkProvider().isChunkGeneratedAt(x, z)) {
			return world.getChunkFromChunkCoords(x, z);
		} else {
			long c = ChunkPos.asLong(x, z);
			if (chunkCache.containsKey(c)) {
				return chunkCache.get(c);
			} else {
				return this.preGenerate(x, z);
			}
		}
	}
	
	public boolean isGround(BlockPos pos) {
		Chunk chunk = this.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
		return pos.getY()<=this.getGroundHeight(chunk, pos.getX() & 15, pos.getZ() & 15);
	}

	public int getGroundHeight(Chunk chunk, int x, int z) { // TODO rework conditions?
		int k; IBlockState block = null;
		for (k = chunk.getHeightValue(x, z) + this.heightMapOffset; k > 0; k--) {
			block = chunk.getBlockState(posCache.set(x, k, z));
			if (block.getMaterial().isOpaque() && !(block.getMaterial() == Material.WOOD)) {
				break;
			}
		}
		return k;
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

}
