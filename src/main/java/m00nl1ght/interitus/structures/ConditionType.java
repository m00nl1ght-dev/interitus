package m00nl1ght.interitus.structures;

import java.util.ArrayList;
import java.util.HashMap;

import m00nl1ght.interitus.util.VarBlockPos;
import m00nl1ght.interitus.world.InteritusChunkGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

public abstract class ConditionType {
	
	private static final HashMap<String, Class<?>> regServer = new HashMap<String, Class<?>>();
	private static final ArrayList<String> materialNames = new ArrayList<String>();
	private static final ArrayList<Material> materialList = new ArrayList<Material>();
	
	public static final ConditionType IN_AIR = new ConditionMaterialSet("inAir", Material.AIR);
	
	static {
		registerType(ConditionMaterialSet.class);
		registerType(ConditionBlockSet.class);
	}
	
	private String name;
	
	public ConditionType() {}
	public ConditionType(String name) {this.name = name;}

	public static void writeMaterialList(NBTTagCompound matTag) {
		NBTTagList list = new NBTTagList();
		for (String s : materialNames) {
			list.appendTag(new NBTTagString(s));
		}
		matTag.setTag("m", list);
	}

	public static NBTTagCompound save(ConditionType cond, StructurePack pack) {
		NBTTagCompound tag = new NBTTagCompound();
		cond.writeToNBT(tag, pack);
		tag.setString("type", cond.getType());
		return tag;
	}
	
	public static ConditionType build(String name, NBTTagCompound tag, StructurePack pack) {
		String type = tag.getString("type");
		Class<?> clazz = regServer.get(type);
		if (clazz==null) {throw new IllegalStateException("Invalid condition type: "+type);}
		ConditionType ct;
		try {
			ct = (ConditionType) clazz.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to build condition type <"+type+">: ", e);
		}
		ct.name = name;
		ct.readFromNBT(tag, pack);
		return ct;
	}
	
	public static void registerType(Class<?> clazz) {
		ConditionType ct;
		try {
			ct = (ConditionType) clazz.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to register condition type (c): "+clazz+" ", e);
		}
		String type = ct.getType();
		if (type.isEmpty()) {throw new IllegalStateException("Failed to register condition type: "+ct+" (name is empty)");}
		if (regServer.containsKey(type)) {throw new IllegalStateException("Failed to register condition type <"+type+"> (type already registered)");}
		regServer.put(type, clazz);
	}
	
	protected abstract void writeToNBT(NBTTagCompound tag, StructurePack pack);

	protected abstract void readFromNBT(NBTTagCompound tag, StructurePack pack);

	public abstract boolean apply(InteritusChunkGenerator gen, VarBlockPos pos);
	
	public abstract String getType();
	
	public final String getName() {
		return this.name;
	}
	
	public static class ConditionMaterialSet extends ConditionType {
		
		private Material[] materials;
		
		public ConditionMaterialSet() {}
		public ConditionMaterialSet(String name) {super(name);}
		public ConditionMaterialSet(String name, Material... materials) {super(name); this.materials = materials;}

		@Override
		protected void writeToNBT(NBTTagCompound tag, StructurePack pack) {
			if (pack==null) {
				int[] ids = new int[materials.length];
				for (int i = 0; i < ids.length; i++) {
					ids[i] = materialList.indexOf(materials[i]);
				}
				tag.setIntArray("b", ids);
			} else {
				NBTTagList list = new NBTTagList();
				for (Material m : materials) {
					list.appendTag(new NBTTagString(materialNames.get(materialList.indexOf(m))));
				}
				tag.setTag("m", list);
			}
		}

		@Override
		protected void readFromNBT(NBTTagCompound tag, StructurePack pack) {
			if (pack==null) {
				int[] ids = tag.getIntArray("b");
				materials = new Material[ids.length];
				for (int i = 0; i < ids.length; i++) {
					materials[i] = materialList.get(ids[i]);
				}
			} else {
				NBTTagList list = tag.getTagList("m", 8);
				materials = new Material[list.tagCount()];
				for (int i = 0; i < list.tagCount(); i++) {
					int idx = materialNames.indexOf(list.getStringTagAt(i));
					materials[i] = materialList.get(idx);
				}
			}
		}

		@Override
		public boolean apply(InteritusChunkGenerator gen, VarBlockPos pos) {
			Material m = gen.getBlockState(pos).getMaterial();
			for (Material x : materials) {if (m==x) return true;}
			return false;
		}

		@Override
		public String getType() {
			return "materialSet";
		}
		
	}
	
	public static class ConditionBlockSet extends ConditionType {
		
		private Block[] blocks;
		
		public ConditionBlockSet() {}
		public ConditionBlockSet(String name) {super(name);}

		@Override
		protected void writeToNBT(NBTTagCompound tag, StructurePack pack) {
			int[] ids = new int[blocks.length];
			if (pack==null) {
				for (int i = 0; i < ids.length; i++) {
					ids[i] = Block.getIdFromBlock(blocks[i]);
				}
			} else {
				for (int i = 0; i < ids.length; i++) {
					ids[i] = pack.mappings.idFor(blocks[i]);
				}
			}
			tag.setIntArray("b", ids);
		}

		@Override
		protected void readFromNBT(NBTTagCompound tag, StructurePack pack) {
			int[] ids = tag.getIntArray("b");
			blocks = new Block[ids.length];
			if (pack==null) {
				for (int i = 0; i < ids.length; i++) {
					blocks[i] = Block.getBlockById(ids[i]);
				}
			} else {
				for (int i = 0; i < ids.length; i++) {
					blocks[i] = pack.mappings.getBlock(ids[i]);
				}
			}
		}

		@Override
		public boolean apply(InteritusChunkGenerator gen, VarBlockPos pos) {
			Block block = gen.getBlockState(pos).getBlock();
			for (Block b : blocks) {
				if (block==b) {return true;}
			}
			return false;
		}

		@Override
		public String getType() {
			return "blockSet";
		}
		
	}
	
	public static void init() {
		registerMaterial(Material.AIR, "air");
		registerMaterial(Material.ANVIL, "anvil");
		registerMaterial(Material.BARRIER, "barrier");
		registerMaterial(Material.CACTUS, "cactus");
		registerMaterial(Material.CAKE, "cake");
		registerMaterial(Material.CARPET, "carpet");
		registerMaterial(Material.CIRCUITS, "circuits");
		registerMaterial(Material.CLAY, "clay");
		registerMaterial(Material.CLOTH, "cloth");
		registerMaterial(Material.CORAL, "coral");
		registerMaterial(Material.CRAFTED_SNOW, "crafted_snow");
		registerMaterial(Material.DRAGON_EGG, "dragon_egg");
		registerMaterial(Material.FIRE, "fire");
		registerMaterial(Material.GLASS, "glass");
		registerMaterial(Material.GOURD, "gourd");
		registerMaterial(Material.GRASS, "grass");
		registerMaterial(Material.GROUND, "ground");
		registerMaterial(Material.ICE, "ice");
		registerMaterial(Material.IRON, "iron");
		registerMaterial(Material.LAVA, "lava");
		registerMaterial(Material.LEAVES, "leaves");
		registerMaterial(Material.PACKED_ICE, "packed_ice");
		registerMaterial(Material.PISTON, "piston");
		registerMaterial(Material.PLANTS, "plants");
		registerMaterial(Material.PORTAL, "portal");
		registerMaterial(Material.REDSTONE_LIGHT, "redstone_light");
		registerMaterial(Material.ROCK, "rock");
		registerMaterial(Material.SAND, "sand");
		registerMaterial(Material.SNOW, "snow");
		registerMaterial(Material.SPONGE, "sponge");
		registerMaterial(Material.STRUCTURE_VOID, "structure_void");
		registerMaterial(Material.TNT, "tnt");
		registerMaterial(Material.VINE, "vine");
		registerMaterial(Material.WATER, "water");
		registerMaterial(Material.WEB, "web");
		registerMaterial(Material.WOOD, "wood");
	}
	
	private static void registerMaterial(Material material, String name) {
		materialList.add(material);
		materialNames.add(name);
	}

}
