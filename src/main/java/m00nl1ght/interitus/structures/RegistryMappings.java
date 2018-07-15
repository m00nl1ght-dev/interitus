package m00nl1ght.interitus.structures;

import java.util.ArrayList;
import java.util.List;

import m00nl1ght.interitus.world.UnknownBiome;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

public class RegistryMappings {
	
	private final ArrayList<IBlockState> data = new ArrayList<IBlockState>();
	private final ArrayList<Biome> biomes = new ArrayList<Biome>();
	private final ArrayList<ResourceLocation> missing = new ArrayList<ResourceLocation>();

	public int idFor(IBlockState state) {
		int id = data.indexOf(state);
		if (id<0) {
			data.add(state);
			id = data.size()-1;
		}
		return id;
	}

	public IBlockState get(int id) {
		return data.get(id);
	}
	
	public int idForBiome(Biome biome) {
		int id = biomes.indexOf(biome);
		if (id<0) {
			biomes.add(biome);
			id = biomes.size()-1;
		}
		return id;
	}

	public Biome getBiome(int id) {
		return biomes.get(id);
	}
	
	public void reset() {
		data.clear();
		biomes.clear();
		missing.clear();
	}

	public NBTTagCompound save() {
		NBTTagCompound nbt = new NBTTagCompound();
		for (int i = 0; i < data.size(); i++) {
			IBlockState state = data.get(i);
			Block block = state.getBlock();
			int meta = block.getMetaFromState(state);
			NBTBase tag = nbt.getTag(block.getRegistryName().toString());
			
			if (tag instanceof NBTTagCompound) {
				((NBTTagCompound) tag).setInteger(Integer.toString(meta), i);
			} else if (tag instanceof NBTTagInt) {
				NBTTagCompound tag0 = new NBTTagCompound();
				tag0.setTag("0", tag);
				tag0.setInteger(Integer.toString(meta), i);
				nbt.setTag(block.getRegistryName().toString(), tag0);
			} else {
				if (meta == 0) {
					nbt.setInteger(block.getRegistryName().toString(), i);
				} else {
					NBTTagCompound tag0 = new NBTTagCompound();
					tag0.setInteger(Integer.toString(meta), i);
					nbt.setTag(block.getRegistryName().toString(), tag0);
				}
			}
		}
		NBTTagCompound nbtBiomes = new NBTTagCompound();
		for (int i = 0; i < biomes.size(); i++) {
			Biome biome = biomes.get(i);
			if (biome instanceof UnknownBiome) {
				nbtBiomes.setInteger(((UnknownBiome)biome).getIDName().toString(), i);
			} else {
				nbtBiomes.setInteger(biome.getRegistryName().toString(), i);
			}
		}
		nbt.setTag("biomes", nbtBiomes);
		return nbt;
	}
	
	public void build(NBTTagCompound nbt) {
		data.clear(); biomes.clear(); missing.clear();
		NBTTagCompound nbtBiomes = nbt.getCompoundTag("biomes");
		nbt.removeTag("biomes");
		for (String key : nbtBiomes.getKeySet()) {
			ResourceLocation name = new ResourceLocation(key);
			Biome biome = Biome.REGISTRY.getObject(name);
			int id = nbtBiomes.getInteger(key);
			resizeMap(biomes, id);
			biomes.set(id, biome==null?new UnknownBiome(name):biome);
		}
		for (String key : nbt.getKeySet()) {
			ResourceLocation name = new ResourceLocation(key);
			if (Block.REGISTRY.containsKey(name)) {
				Block block = Block.REGISTRY.getObject(name);
				NBTBase tag = nbt.getTag(key);
				if (tag instanceof NBTTagInt) {
					int idx = ((NBTTagInt) tag).getInt();
					resizeMap(data, idx);
					data.set(idx, block.getStateFromMeta(0));
				} else if (tag instanceof NBTTagCompound) {
					NBTTagCompound tag0 = (NBTTagCompound) tag;
					for (String key0 : tag0.getKeySet()) {
						int meta = Integer.parseInt(key0);
						if (meta<0 || meta>15) {throw new IllegalStateException("Invaild structure pack mapping: Metadata out of range ("+meta+"): "+name);}
						int idx = ((NBTTagInt) tag0.getTag(key0)).getInt();
						resizeMap(data, idx);
						data.set(idx, block.getStateFromMeta(meta));
					}
				} else {
					throw new IllegalStateException("Invaild structure mapping: Unknown tag found: "+tag);
				}
			} else {
				missing.add(name);
			}
		}
	}
	
	private void resizeMap(List list, int idx) {
		while (list.size()<idx+1) {
			list.add(null);
		}
	}
	
	public ArrayList<ResourceLocation> getMissingBlocks() {
		return this.missing;
	}
	
	@Override
	public String toString() {
		return data.toString();
	}

}
