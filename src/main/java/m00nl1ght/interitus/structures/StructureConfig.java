package m00nl1ght.interitus.structures;

import java.util.HashMap;

import m00nl1ght.interitus.world.InteritusChunkGenWrapper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public abstract class StructureConfig {
	
	private static final HashMap<String, Class<?>> registry = new HashMap<String, Class<?>>();
	
	static {
		
	}
	
	public final Structure structure;
	private double chance;
	private int minDistance; // minimal distance a new occurence has to have to another occurence of this template
	
	public StructureConfig(Structure structure) {
		this.structure=structure;
	}
	
	public abstract BlockPos getPotentialPos(InteritusChunkGenWrapper gen);
	
	public void readFromNBT(NBTTagCompound tag) {
		this.chance = tag.getDouble("chance");
		this.minDistance = tag.getInteger("minDistance");
	}
	
//	public static StructureConfig build(NBTTagCompound tag) {
//		
//	}
	
	public void writeToNBT(NBTTagCompound tag) {
		tag.setDouble("chance", this.chance);
		tag.setInteger("minDistance", this.minDistance);
	}
	
	public static NBTTagCompound save(StructureConfig config) {
		NBTTagCompound tag = new NBTTagCompound();
		
		return tag;
	}
	
	public double getChance() {
		return this.chance;
	}
	
	public int getMinDistance() {
		return this.minDistance;
	}
	
	public abstract String getType();
	
	public static void registerType(Class<?> clazz, String id) {
		registry.put(id, clazz);
	}
	
	public static class SimpleYRnageConfig extends StructureConfig {

		public SimpleYRnageConfig(Structure structure) {
			super(structure);
		}

		@Override
		public BlockPos getPotentialPos(InteritusChunkGenWrapper gen) {
			return null;
		}

		@Override
		public String getType() { //TODO
			return "";
		}
		
	}

}
