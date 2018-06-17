package m00nl1ght.interitus.structures;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import m00nl1ght.interitus.Interitus;
import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure.LootGenPrimer;
import m00nl1ght.interitus.world.InteritusChunkGenerator;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BitArray;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;


public class BlockRegionStorage {

	private static final IBlockState DEFAULT_STATE = Blocks.AIR.getDefaultState();
	
	private BitArray blocks;
	private HashMap<Long, NBTTagCompound> nbt;
	private HashMap<Long, LootGen[]> loot;
    private ArrayList<Condition> conditions;
	private ArrayList<EntityInfo> entities;
	private int blockStateBits, sizeX, sizeY, sizeZ;
	private ArrayList<IBlockState> blockstateIDs;
	
	public BlockRegionStorage() {
		nbt = new HashMap<Long, NBTTagCompound>();
		loot = new HashMap<Long, LootGen[]>();
		conditions = new ArrayList<Condition>();
		entities = new ArrayList<EntityInfo>();
		blockstateIDs = new ArrayList<IBlockState>();
	}
	
	public BlockRegionStorage(int sizeX, int sizeY, int sizeZ) {
		this.prepare(sizeX, sizeY, sizeZ, false);
	}
	
	public void prepare(int sizeX, int sizeY, int sizeZ, boolean fullReset) {
		this.sizeX = sizeX; this.sizeY = sizeY; this.sizeZ = sizeZ;
		nbt.clear(); entities.clear(); 
		blocks = null; blockStateBits = 0; blockstateIDs.clear();
		blockstateIDs.add(Blocks.AIR.getDefaultState()); // ID -> 0
		blockstateIDs.add(Blocks.STRUCTURE_VOID.getDefaultState()); // ID -> 1
		if (fullReset) {conditions.clear(); loot.clear();}
	}
	
	public void init() {
		blockStateBits = MathHelper.log2DeBruijn(blockstateIDs.size());
		blocks = new BitArray(blockStateBits, sizeX*sizeY*sizeZ);
	}
	
	public void registerBlockstate(IBlockState state) {
		if (blocks!=null) {throw new IllegalStateException();}
		if (blockstateIDs.indexOf(state)<0) {
			blockstateIDs.add(state);
		}
	}
	
	public boolean valid() {
		if (blocks==null || blocks.size()!=sizeX*sizeY*sizeZ) {return false;}
		if (this.loot==null || this.nbt==null || this.conditions==null || this.entities==null) {return false;}
		return true;
	}
	
	public int blockCount() {
		return sizeX*sizeY*sizeZ;
	}
	
	public BlockPos size(Rotation rotation) {
		return (rotation==Rotation.CLOCKWISE_90 || rotation==Rotation.COUNTERCLOCKWISE_90)?new BlockPos(sizeZ, sizeY, sizeX):new BlockPos(sizeX, sizeY, sizeZ);
	}
	
	public int sizeX() {return sizeX;}
	public int sizeY() {return sizeY;}
	public int sizeZ() {return sizeZ;}
	
	public int sizeXswapped(Rotation rotation) {
		return (rotation==Rotation.CLOCKWISE_90 || rotation==Rotation.COUNTERCLOCKWISE_90)?sizeZ:sizeX;
	}
	
	public int sizeZswapped(Rotation rotation) {
		return (rotation==Rotation.CLOCKWISE_90 || rotation==Rotation.COUNTERCLOCKWISE_90)?sizeX:sizeZ;
	}
	
	// Read & Write NBT

	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		
		int[] mappings = new int[blockstateIDs.size()-2];
		for (int i=2; i<blockstateIDs.size(); i++) {
			mappings[i-2] = StructurePack.current.mappings.idFor(blockstateIDs.get(i));
		}

		ByteBuffer blockBytes = ByteBuffer.allocate(blocks.getBackingLongArray().length * Long.BYTES);
		for (Long l : this.blocks.getBackingLongArray()) {
			blockBytes.putLong(l);
		}
		
		NBTTagList nbtTileEntities = new NBTTagList();
		for (Entry<Long, NBTTagCompound> entry : this.nbt.entrySet()) {

			NBTTagCompound nbtTE = entry.getValue().copy();
			nbtTE.setLong("structPos", entry.getKey());

			LootGen[] loot = this.loot.get(entry.getKey());
			if (loot != null && loot.length > 0) {
				NBTTagList lootGens = new NBTTagList();
				for (LootGen gen : loot) {
					lootGens.appendTag(gen.toNBT());
				}
				nbtTE.setTag("structLoot", lootGens);
			}

			nbtTileEntities.appendTag(nbtTE);
		}

        NBTTagList nbtEntities = new NBTTagList();
        for (EntityInfo entityinfo : getEntities()) {
            NBTTagCompound tag = entityinfo.entityData.copy();
            NBTTagList posTag = new NBTTagList();
            posTag.appendTag(new NBTTagDouble(entityinfo.pos.x));
            posTag.appendTag(new NBTTagDouble(entityinfo.pos.y));
            posTag.appendTag(new NBTTagDouble(entityinfo.pos.z));
            tag.setTag("Pos", posTag);
            nbtEntities.appendTag(tag);
        }
        
        NBTTagList nbtConditions = new NBTTagList();
        for (Condition condition : conditions) {
            nbtConditions.appendTag(condition.writeToNbt(new NBTTagCompound()));
        }

        nbt.setIntArray("size", new int[] {sizeX, sizeY, sizeZ});
        nbt.setByteArray("blocks", blockBytes.array());
        nbt.setIntArray("mappings", mappings);
        nbt.setTag("tileentities", nbtTileEntities);
        nbt.setTag("entities", nbtEntities);
        nbt.setTag("conditions", nbtConditions);
        
        return nbt;
    }

    public void readFromNBT(NBTTagCompound nbt, RegistryMappings registry) {
        
        int[] size = nbt.getIntArray("size");
        if (size.length==3) {
        	prepare(size[0], size[1], size[2], true);
        	if (this.blockCount()<=0) {
        		throw new IllegalStateException("Invalid structure data: size is 0");
        	}
        } else {
        	prepare(0, 0, 0, true);
        	throw new IllegalStateException("Invalid structure data: missing size tag.");
        }
        
        int[] mappings = nbt.getIntArray("mappings");
        for (int i=0; i<mappings.length; i++) {
        	IBlockState state = registry.get(mappings[i]);
        	if (state==null) {state=DEFAULT_STATE;}
        	blockstateIDs.add(state);
        }
        
        init();
        byte[] blockBytes = nbt.getByteArray("blocks");
        if (blockBytes.length!=blocks.getBackingLongArray().length*Long.BYTES) {
        	throw new IllegalStateException("Invalid structure data: block array has incorrect size ("+blockBytes.length+", should be "+blocks.getBackingLongArray().length*Long.BYTES+")");
        }
        ByteBuffer.wrap(blockBytes).asLongBuffer().get(this.blocks.getBackingLongArray());

        NBTTagList nbtTileEntities = nbt.getTagList("tileentities", 10);
        for (int i=0; i<nbtTileEntities.tagCount(); i++) {
        	NBTTagCompound tag = nbtTileEntities.getCompoundTagAt(i);
        	long pos = tag.getLong("structPos");
        	tag.removeTag("structPos");
        	this.nbt.put(pos, tag);
        	if (tag.hasKey("structLoot")) {
        		NBTTagList nbtLoot = tag.getTagList("structLoot", 10);
				for (int j = 0; j < nbtLoot.tagCount(); j++) {
					try {
						NBTTagCompound lootTag = nbtLoot.getCompoundTagAt(j);
						LootGen[] gens = new LootGen[nbtLoot.tagCount()];
						for (int k = 0; k < nbtLoot.tagCount(); k++) {
							gens[k] = new LootGen(nbtLoot.getCompoundTagAt(k));
						}
						loot.put(pos, gens);
					} catch (Exception e) {
						Interitus.logger.warn("Error loading loot container: ", e);
					}
        		}
        		tag.removeTag("structLoot");
        	}
        }

        NBTTagList nbtEntities = nbt.getTagList("entities", 10);
        for (int k = 0; k < nbtEntities.tagCount(); ++k) {
            NBTTagCompound nbtEntity = nbtEntities.getCompoundTagAt(k);
            NBTTagList nbtPos = nbtEntity.getTagList("Pos", 6);
            Vec3d pos = new Vec3d(nbtPos.getDoubleAt(0), nbtPos.getDoubleAt(1), nbtPos.getDoubleAt(2));
            nbtEntity.removeTag("Pos");
            addEntity(pos, nbtEntity);
        }
        
        NBTTagList nbtConditions = nbt.getTagList("conditions", 10);
        for (int k = 0; k < nbtConditions.tagCount(); ++k) {
            addCondition(Condition.readFromNBT(nbtConditions.getCompoundTagAt(k)));
        }
    }
	
	// Blockstates
	
	public void addBlock(BlockPos pos, IBlockState block, @Nullable NBTTagCompound tileentity) {
		int id = blockstateIDs.indexOf(block);
		if (id<0) {throw new IllegalStateException("Cannot add unregistered blockstate to storage: "+block);}
		blocks.setAt(indexOf(pos.getX(), pos.getY(), pos.getZ()), id);
		if (tileentity!=null) {nbt.put(pos.toLong(), tileentity);}
	}
	
	public IBlockState getBlock(int idx) {
		return blockstateIDs.get(blocks.getAt(idx));
	}
	
	public IBlockState getBlock(int x, int y, int z) {
		return blockstateIDs.get(blocks.getAt(indexOf(x, y, z)));
	}
	
	public IBlockState getBlock(BlockPos pos) {
		return getBlock(pos.getX(), pos.getY(), pos.getZ());
	}
	
	public int indexOf(int posX, int posY, int posZ) {
		return posZ*sizeX*sizeY+posY*sizeX+posX;
	}
	
	public NBTTagCompound getNBT(BlockPos pos) {
		return nbt.get(pos.toLong());
	}
	
	// Entities
	
	public void addEntity(Vec3d pos, NBTTagCompound nbt) {
		entities.add(new EntityInfo(pos, nbt));
	}
	
	public ArrayList<EntityInfo> getEntities() {
		return this.entities;
	}
	
	public static class EntityInfo {
		
        public final Vec3d pos;
        public final NBTTagCompound entityData;

        public EntityInfo(Vec3d vecIn, NBTTagCompound compoundIn) {
            this.pos = vecIn;
            this.entityData = compoundIn;
        }
    }
	
	// Conditions
	
	public void addCondition(Condition cond) {
		if (cond==null) {throw new IllegalArgumentException("Cannot add null condition!");}
		conditions.add(cond);
	}
	
	public ArrayList<Condition> getConditions() {
		return this.conditions;
	}
	
	public void clearConditions() {
		this.conditions.clear();
	}
	
    public enum ConditionType {
    	inGround, inWater, inStone, inAir, overSurface;
    }
	
	public static class Condition {
		
		public final BlockPos pos;
		public final ConditionType type;
		
		public Condition(ConditionType type, BlockPos pos) {
			this.type=type; this.pos=pos;
		}
		
		public static boolean isValidType(String type) {
			try {ConditionType.valueOf(type);} catch (Exception e) {return false;}
			return true;
		}
		
		public boolean fullfilled(InteritusChunkGenerator gen, BlockPos origin) {
			BlockPos pos1 = origin.add(this.pos);
			switch (type) {
			case inGround: return gen.isGround(pos1);
			case inWater: return gen.getBlockState(pos1).getMaterial()==Material.WATER;
			case inStone: return gen.getBlockState(pos1).getBlock()==Blocks.STONE;
			case inAir: return gen.getBlockState(pos1).getMaterial()==Material.AIR;
			case overSurface: return pos1.getY()>=gen.getHeight(pos1);
			default: return false;
			}
		}
		
		public Condition toAbsolute(BlockPos structurePos) {
			return new Condition(this.type, this.pos.subtract(structurePos));
		}

		public Condition toRelative(BlockPos structurePos) {
			return new Condition(this.type, this.pos.add(structurePos));
		}

		public NBTTagCompound writeToNbt(NBTTagCompound tag) {
			tag.setInteger("x", pos.getX());
			tag.setInteger("y", pos.getY());
			tag.setInteger("z", pos.getZ());
			tag.setString("id", type.name());
			return tag;
		}
		
		public static Condition readFromNBT(NBTTagCompound tag) {
			ConditionType type = ConditionType.valueOf(tag.getString("id"));
			if (type==null) {throw new IllegalStateException("Failed toload condition from nbt, invalid condition type: "+tag.getString("id"));}
			return new Condition(type, new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z")));
		}
		
		@Override
		public String toString() {
			return pos.toString()+" "+type.name();
		}
		
	}
	
	// Loot Gen
	
	public void addLootGen(BlockPos pos, LootGen[] lootgen) {
		if (lootgen.length==0) {throw new IllegalArgumentException("Cannot add empty LootGen array!");}
		loot.put(pos.toLong(), lootgen);
	}
	
	public LootGen[] getLootGen(BlockPos pos) {
		return loot.get(pos.toLong());
	}
	
	public Set<Entry<Long, LootGen[]>> getLootEntries() {
		return this.loot.entrySet();
	}
	
	public void clearLootData() {
		this.loot.clear();
	}
	
	public static class LootGen {
		
		public final LootList list;
		public final int count;
		
		public LootGen(LootList list, int count) {
			this.list=list; this.count=count;
			if (list==null) {throw new IllegalStateException("Invalid loot gen: LootList is null!");}
			if (count<=0) {throw new IllegalStateException("Invalid loot gen: count can not be zero or negative!");} 
		}
		
		public LootGen(NBTTagCompound tag) {
			String name=tag.getString("list");
			this.list = StructurePack.getLootList(name);
			this.count=tag.getInteger("count");
			if (list==null) {throw new IllegalStateException("Invalid loot gen (from NBT): LootList <"+name+"> not found!");}
			if (count<=0) {throw new IllegalStateException("Invalid loot gen (from NBT): count can not be zero or negative!");} 
		}

		public LootGen(LootGenPrimer gen) {
			this.list = StructurePack.getLootList(gen.list());
			this.count=gen.amount();
			if (list==null) {throw new IllegalStateException("Invalid loot gen (from primer): LootList <"+gen.list()+"> not found!");}
			if (count<=0) {throw new IllegalStateException("Invalid loot gen (from primer): count can not be zero or negative!");} 
		}

		public NBTTagCompound toNBT() {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("list", list.name);
			tag.setInteger("count", count);
			return tag;
		}
		
	}
	
	// Position Transform
	
	public void transformPos(MutableBlockPos target, int x, int y, int z, Mirror mirror, Rotation rotation) {
		switch (mirror) {
		case LEFT_RIGHT:
			z = sizeZ-z-1;
			break;
		case FRONT_BACK:
			x = sizeX-x-1;
			break;
		default:
		}
		switch (rotation) {
		case COUNTERCLOCKWISE_90:
			target.setPos(z, y, sizeZ-x-1); break;
		case CLOCKWISE_90:
			target.setPos(sizeX-z-1, y, x); break;
		case CLOCKWISE_180:
			target.setPos(sizeX-x-1, y, sizeZ-z-1); break;
		default: 
			target.setPos(x, y, z);
		}
	}
	
	public void reversePos(MutableBlockPos target, int x, int y, int z, Mirror mirror, Rotation rotation) {
		switch (rotation) {
			case CLOCKWISE_90:
				int i=x; x=z; z=sizeZ-i-1; break;
			case COUNTERCLOCKWISE_90:
				int k=x; x=sizeX-z-1; z=k; break;
			case CLOCKWISE_180:
				x=sizeX-x-1; z=sizeZ-z-1; break;
			default:
		}
		switch (mirror) {
			case LEFT_RIGHT:
				z = sizeZ-z-1;
				break;
			case FRONT_BACK:
				x = sizeX-x-1;
				break;
			default:
		}
		target.setPos(x, y, z);
	}

    public Vec3d transformVec3d(Vec3d vec, Mirror mirror, Rotation rotation) {
        double x = vec.x, y = vec.y, z = vec.z;
        switch (mirror) {
            case LEFT_RIGHT:
            	z = sizeZ-z;
    			break;
            case FRONT_BACK:
            	x = sizeX-x;
    			break;
            default:
            	if (rotation==Rotation.NONE) {return vec;}
        }
        switch (rotation) {
            case COUNTERCLOCKWISE_90:
            	return new Vec3d(z, y, sizeX-x);
            case CLOCKWISE_90:
            	return new Vec3d(sizeZ-z, y, x);
            case CLOCKWISE_180:
                return new Vec3d(sizeX-x, y, sizeZ-z);
            default: 
            	return new Vec3d(x, y, z);
        }
    }
    
    public float transformRotationYaw(float f, Rotation rotation, Mirror mirror) {
    	
    	f = MathHelper.wrapDegrees(f);
    	
		switch (mirror) {
			case LEFT_RIGHT:
				f = 180 - f; break;
			case FRONT_BACK:
				f = -f; break;
			default: break;
		}
		
		switch (rotation) {
			case CLOCKWISE_180:
				f += 180.0F; break;
			case COUNTERCLOCKWISE_90:
				f -= 90.0F; break;
			case CLOCKWISE_90:
				f += 90.0F; break;
			default: break;
		}
    	
        return MathHelper.wrapDegrees(f);
    }

	public EnumFacing transformFacing(EnumFacing facing, Mirror mirror, Rotation rotation) {
		
		switch (mirror) {
			case LEFT_RIGHT:
				if (facing==EnumFacing.NORTH) {facing=EnumFacing.SOUTH;}
				else if (facing==EnumFacing.SOUTH) {facing=EnumFacing.NORTH;} break;
			case FRONT_BACK:
				if (facing==EnumFacing.EAST) {facing=EnumFacing.WEST;}
				else if (facing==EnumFacing.WEST) {facing=EnumFacing.EAST;} break;
			default: break;
		}
		
		switch (rotation) {
			case CLOCKWISE_180:
				return facing.getOpposite();
			case COUNTERCLOCKWISE_90:
				return facing.rotateYCCW();
			case CLOCKWISE_90:
				return facing.rotateY();
			default: 
				return facing;
		}
	}

}
