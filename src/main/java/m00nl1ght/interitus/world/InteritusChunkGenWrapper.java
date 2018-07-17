package m00nl1ght.interitus.world;

import java.util.List;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;

public class InteritusChunkGenWrapper extends InteritusChunkGenerator {
	
	private final IChunkGenerator wrapped;
	
	public InteritusChunkGenWrapper(World world, IChunkGenerator wrapped) {
		super(world); this.wrapped = wrapped;
	}
	
	@Override
	public Chunk generateChunk(int x, int z, boolean pre) {
		return wrapped.generateChunk(x, z);
	}

	@Override
	public void populate(int x, int z) {
		wrapped.populate(x, z);
		this.structures.place(x, z);
	}

	@Override
	public boolean generateStructures(Chunk chunkIn, int x, int z) {
		return wrapped.generateStructures(chunkIn, x, z);
	}

	@Override
	public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
		return wrapped.getPossibleCreatures(creatureType, pos);
	}

	@Override
	public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
		return wrapped.getNearestStructurePos(worldIn, structureName, position, findUnexplored);
	}

	@Override
	public void recreateStructures(Chunk chunkIn, int x, int z) {
		wrapped.recreateStructures(chunkIn, x, z);
	}

	@Override
	public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
		return wrapped.isInsideStructure(worldIn, structureName, pos);
	}
	
	@Override
	public String toString() {
		return "InteritusChunkGenWrapper[dim="+world.provider.getDimension()+", gen="+wrapped.getClass().getSimpleName()+"]";
	}

}
