package m00nl1ght.interitus.structures;

import java.util.HashMap;
import java.util.Random;

import m00nl1ght.interitus.structures.Structure.IStructureData;
import m00nl1ght.interitus.structures.Structure.StructureData;
import m00nl1ght.interitus.util.VarBlockPos;

import m00nl1ght.interitus.world.InteritusChunkGenerator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

public abstract class WorldGenTask {
	
	private static final HashMap<String, Class<?>> regServer = new HashMap<String, Class<?>>();
	private static final VarBlockPos posCache = new VarBlockPos(), posCache2 = new VarBlockPos();
	
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
		if (clazz==null) {throw new IllegalStateException("Invalid worldgen task type: "+type);}
		WorldGenTask task;
		try {
			task = (WorldGenTask) clazz.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to build worldgen task of type <"+type+">: ", e);
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
		if (clazz==null) {throw new IllegalStateException("Invalid worldgen task type: "+type);}
		WorldGenTask task;
		try {
			task = (WorldGenTask) clazz.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to build worldgen task of type <"+type+">: ", e);
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
			throw new IllegalStateException("Failed to register worldgen task type: "+clazz+" ", e);
		}
		String type = task.getType();
		if (type.isEmpty()) {throw new IllegalStateException("Failed to register worldgen task type: "+task+" (name is empty)");}
		if (regServer.containsKey(type)) {throw new IllegalStateException("Failed to register worldgen task type <"+type+"> (type already registered)");}
		regServer.put(type, clazz);
	}
	
	public abstract boolean apply(InteritusChunkGenerator gen, int x, int z);
	
	public boolean isChunkSuitable(int x, int z, IStructureData data) {
		return data==null;
	}
	
	protected abstract void readFromNBT(Structure structure, NBTTagCompound tag);
	
	protected abstract void writeToNBT(NBTTagCompound tag);
	
	public Structure getStructure() {return this.structure;}
	
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
			posCache.set(x * 16 + gen.random.nextInt(16), 0, z * 16 + gen.random.nextInt(16));
			if (structure.nearOccurence(posCache, minDistance)) {gen.gRange++; return false;}
			// 1 find suitable position
			if (!gen.getStructurePositionMap().findSuitablePosition(this, posCache, Rotation.NONE)) {gen.gNoPos++; return false;}
			Chunk chunk = gen.world.getChunkFromChunkCoords(posCache.chunkX(), posCache.chunkZ());
			int h = chunk.getHeightValue(posCache.inChunkX(), posCache.inChunkZ());
			if (h<=0) {gen.gNoPos++; return false;}
			posCache.setY(randomY(gen.random, h - this.hBase, this.hRange));
			// 2 check structure conditions
			if (!structure.checkConditions(gen, posCache)) {gen.gCond++; return false;}
			// 3 create structure
			StructureData data = new StructureData(structure, posCache.toImmutable(), Mirror.NONE, Rotation.NONE);
			gen.getStructurePositionMap().create(data);
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
			gen.gAll++;
			posCache.set(x * 16 + gen.random.nextInt(16), 0, z * 16 + gen.random.nextInt(16));
			if (structure.nearOccurence(posCache, minDistance)) {gen.gRange++; return false;}
			// 1 find suitable position
			if (!gen.getStructurePositionMap().findSuitablePosition(this, posCache, Rotation.NONE)) {gen.gNoPos++; return false;}
			posCache.setY(randomY(gen.random, this.hBase, this.hRange));
			// 2 check structure conditions
			if (!structure.checkConditions(gen, posCache)) {gen.gCond++; return false;}
			// 3 create structure
			StructureData data = new StructureData(structure, posCache.toImmutable(), Mirror.NONE, Rotation.NONE);
			gen.getStructurePositionMap().create(data);
			gen.gDone++;
			return true;
		}

		@Override
		public String getType() {
			return "underGround";
		}

	}

}
