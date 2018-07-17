package m00nl1ght.interitus.structures;

import java.util.HashMap;
import java.util.Random;

import m00nl1ght.interitus.structures.Structure.StructureData;
import static m00nl1ght.interitus.util.VarBlockPos.PUBLIC_CACHE;
import m00nl1ght.interitus.world.InteritusChunkGenerator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

public abstract class WorldGenTask {
	
	private static final HashMap<String, Class<?>> regServer = new HashMap<String, Class<?>>();
	
	static {
		registerType(DefaultOnGroundTask.class);
		registerType(DefaultUnderGroundTask.class);
	}
	
	protected Structure structure;
	protected Biome[] biomes;
	protected int[] dimensions;

	public static NBTTagCompound save(WorldGenTask task, RegistryMappings mappings) {
		NBTTagCompound tag = new NBTTagCompound();
		task.writeToNBT(tag);
		tag.setString("type", task.getType());
		int[] bIDs = new int[task.biomes.length];
		for (int i = 0; i<bIDs.length; i++) {
			bIDs[i] = mappings.idForBiome(task.biomes[i]);
		}
		tag.setIntArray("biomes", bIDs);
		tag.setIntArray("dim", task.dimensions);
		return tag;
	}
	
	public static NBTTagCompound getClientTag(WorldGenTask task) {
		NBTTagCompound tag = new NBTTagCompound();
		task.writeToNBT(tag);
		tag.setString("t", task.getType());
		int[] bIDs = new int[task.biomes.length];
		for (int i = 0; i<bIDs.length; i++) {
			bIDs[i] = Biome.getIdForBiome(task.biomes[i]);
		}
		tag.setIntArray("b", bIDs);
		tag.setIntArray("d", task.dimensions);
		return tag;
	}

	public static WorldGenTask build(Structure structure, RegistryMappings mappings, NBTTagCompound tag) {
		String type = tag.getString("type");
		Class<?> clazz = regServer.get(type);
		if (clazz==null) {throw new IllegalStateException("Invalid worldgen task type (c): "+type);}
		WorldGenTask task;
		try {
			task = (WorldGenTask) clazz.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to build worldgen task of type (c) <"+type+">: ", e);
		}
		task.readFromNBT(structure, tag);
		int[] bIDs = tag.getIntArray("biomes");
		task.biomes = new Biome[bIDs.length];
		for (int i = 0; i<bIDs.length; i++) {
			task.biomes[i] = mappings.getBiome(bIDs[i]);
		}
		task.dimensions = tag.getIntArray("dim");
		return task;
	}
	
	public static WorldGenTask buildFromClient(Structure structure, NBTTagCompound tag) {
		String type = tag.getString("t");
		Class<?> clazz = regServer.get(type);
		if (clazz==null) {throw new IllegalStateException("Invalid worldgen task type (c): "+type);}
		WorldGenTask task;
		try {
			task = (WorldGenTask) clazz.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to build worldgen task of type (c) <"+type+">: ", e);
		}
		task.readFromNBT(structure, tag);
		int[] bIDs = tag.getIntArray("b");
		task.biomes = new Biome[bIDs.length];
		for (int i = 0; i<bIDs.length; i++) {
			task.biomes[i] = Biome.getBiomeForId(bIDs[i]);
		}
		task.dimensions = tag.getIntArray("d");
		return task;
	}
	
	public static void registerType(Class<?> clazz) {
		WorldGenTask task;
		try {
			task = (WorldGenTask) clazz.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to register worldgen task type (c): "+clazz+" ", e);
		}
		String type = task.getType();
		if (type.isEmpty()) {throw new IllegalStateException("Failed to register worldgen task type (c): "+task+" (name is empty)");}
		if (regServer.containsKey(type)) {throw new IllegalStateException("Failed to register worldgen task type (c) <"+type+"> (type already registered)");}
		regServer.put(type, clazz);
	}
	
	public abstract boolean apply(InteritusChunkGenerator gen, int x, int z);
	
	protected abstract void readFromNBT(Structure structure, NBTTagCompound tag);
	
	protected abstract void writeToNBT(NBTTagCompound tag);
	
	public abstract String getType();

	protected static int randomY(Random random, int base, int range) {
		return range<=0?base:base+random.nextInt(range+1);
	}

	public static class DefaultOnGroundTask extends WorldGenTask {

		protected double chance;
		protected int minDistance;
		protected int hBase, hRange;

		@Override
		public void writeToNBT(NBTTagCompound tag) {
			tag.setDouble("c", this.chance);
			tag.setInteger("mD", this.minDistance);
			tag.setInteger("hB", this.hBase);
			tag.setInteger("hR", this.hRange);
		}

		@Override
		public void readFromNBT(Structure structure, NBTTagCompound tag) {
			this.structure = structure;
			this.chance = tag.getDouble("c");
			this.minDistance = tag.getInteger("mD");
			this.hBase = tag.getInteger("hB");
			this.hRange = tag.getInteger("hR");
		}

		@Override
		public boolean apply(InteritusChunkGenerator gen, int x, int z) {
			if (gen.random.nextDouble() > this.chance) { return false; }
			gen.gAll++;
			PUBLIC_CACHE.set(x * 16 + gen.random.nextInt(16), 0, z * 16 + gen.random.nextInt(16));
			if (structure.nearOccurence(PUBLIC_CACHE, minDistance)) {gen.gRange++; return false;}
			Chunk chunk = gen.getChunk(x, z);
			PUBLIC_CACHE.setY(randomY(gen.random, gen.getGroundHeight(chunk, PUBLIC_CACHE.inChunkX(), PUBLIC_CACHE.inChunkZ()) - this.hBase, this.hRange));
			if (!structure.checkConditions(gen, PUBLIC_CACHE)) {gen.gCond++; return false;}
			gen.getStructurePositionMap().create(new StructureData(structure, PUBLIC_CACHE.toImmutable(), Mirror.NONE, Rotation.NONE));
			gen.gDone++;
			return true;
		}

		@Override
		public String getType() {
			return "onGround";
		}

	}
	
	public static class DefaultUnderGroundTask extends DefaultOnGroundTask {

		@Override
		public boolean apply(InteritusChunkGenerator gen, int x, int z) {
			if (gen.random.nextDouble() > this.chance) { return false; }
			PUBLIC_CACHE.set(x * 16 + gen.random.nextInt(16), 0, z * 16 + gen.random.nextInt(16));
			if (structure.nearOccurence(PUBLIC_CACHE, minDistance)) { return false; }
			Chunk chunk = gen.getChunk(x, z);
			PUBLIC_CACHE.setY(randomY(gen.random, this.hBase, this.hRange));
			if (!structure.checkConditions(gen, PUBLIC_CACHE)) { return false; }
			gen.getStructurePositionMap().create(new StructureData(structure, PUBLIC_CACHE.toImmutable(), Mirror.NONE, Rotation.NONE));
			return true;
		}

		@Override
		public String getType() {
			return "underGround";
		}

	}

}
