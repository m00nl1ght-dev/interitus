package m00nl1ght.interitus.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import m00nl1ght.interitus.Interitus;
import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure.LootEntryPrimer;
import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure.LootGenPrimer;
import m00nl1ght.interitus.structures.BlockRegionStorage.Condition;
import m00nl1ght.interitus.structures.BlockRegionStorage.EntityInfo;
import m00nl1ght.interitus.structures.BlockRegionStorage.LootGen;
import m00nl1ght.interitus.util.VarBlockPos;
import m00nl1ght.interitus.world.InteritusChunkGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;


public class Structure {
	
	private static final VarBlockPos posInStruct = new VarBlockPos();
	private static final VarBlockPos posInWorld = new VarBlockPos();
    protected final BlockRegionStorage storage = new BlockRegionStorage();
    final ArrayList<WorldGenTask> tasks = new ArrayList<WorldGenTask>();
    final ArrayList<StructureData> instances = new ArrayList<StructureData>();
	public final String name;
    private String author = "?";
    
	public static final Predicate<Entity> EXCLUDE_PLAYERS = new Predicate<Entity>() {
        @Override
		public boolean apply(@Nullable Entity entity) {
            return !(entity instanceof EntityPlayer);
        }
    };
	
	public Structure(String name) {
		this.name=name;
	}
    
	public void placeInChunk(Chunk chunk, StructureData data) {
		
		if (!this.storage.valid()) {
			Interitus.logger.warn("Structure could not be placed because of invalid block storage: "+data);
			return;
		}
		
		int xmin = Math.max(0, chunk.x*16-data.pos.getX());
		int zmin = Math.max(0, chunk.z*16-data.pos.getZ());
		int xmax = Math.min(this.storage.sizeXswapped(data.rotation), chunk.x*16-data.pos.getX()+16);
		int zmax = Math.min(this.storage.sizeZswapped(data.rotation), chunk.z*16-data.pos.getZ()+16);
		    	
    	for (int x = xmin; x < xmax; x++) {
        	for (int z = zmin; z < zmax; z++) {
        		for (int y = 0; y < this.storage.sizeY(); y++) {
        			
        			this.storage.reversePos(posInStruct, x, y, z, data.mirror, data.rotation);
        			IBlockState block = storage.getBlock(posInStruct);
        			if (block.getBlock()==Blocks.STRUCTURE_VOID) {continue;}
        			block = block.withMirror(data.mirror).withRotation(data.rotation);
        			posInWorld.set(data.pos.getX()+x, data.pos.getY()+y, data.pos.getZ()+z);
        			
        			NBTTagCompound nbtTE = storage.getNBT(posInStruct);
        			if (placeBlock(chunk, posInWorld, block) && nbtTE != null) {
        				TileEntity newTE = chunk.getTileEntity(posInWorld, EnumCreateEntityType.IMMEDIATE);
        				if (newTE != null) { // set NBT data of the new TE if it has spawned
        					nbtTE.setInteger("x", posInWorld.getX());
        					nbtTE.setInteger("y", posInWorld.getY());
        					nbtTE.setInteger("z", posInWorld.getZ());
        					newTE.readFromNBT(nbtTE);
        					newTE.mirror(data.mirror);
        					newTE.rotate(data.rotation);
        					if (newTE instanceof TileEntityChest) {
        						LootGen[] loot = storage.getLootGen(posInStruct);
        						if (loot!=null && loot.length>0) {
        							for (LootGen gen : loot) {
        								for (int i=0; i<gen.count; i++) {
        									if (!LootList.putInRandomEmptySlot((TileEntityChest)newTE, gen.list.get(), Interitus.random)) {break;}
        								}
        							}
        						}
        					}
        					newTE.markDirty();
        				}
        			}
        		}
        	}	
    	}
    	
    	for (EntityInfo entityinfo : this.storage.getEntities()) {

    		Vec3d pos = this.storage.transformVec3d(entityinfo.pos, data.mirror, data.rotation);
    		if (xmin>pos.x || pos.x>=xmax+1) {continue;}
    		if (zmin>pos.z || pos.z>=zmax+1) {continue;}
    		double dx = pos.x+data.pos.getX();
    		double dy = pos.y+data.pos.getY();
    		double dz = pos.z+data.pos.getZ();
    		NBTTagList nbtPos = new NBTTagList();
    		nbtPos.appendTag(new NBTTagDouble(dx));
    		nbtPos.appendTag(new NBTTagDouble(dy));
    		nbtPos.appendTag(new NBTTagDouble(dz));
    		entityinfo.entityData.setTag("Pos", nbtPos);
    		entityinfo.entityData.setUniqueId("UUID", UUID.randomUUID());

    		Entity entity;
    		try {
    			entity = EntityList.createEntityFromNBT(entityinfo.entityData, chunk.getWorld());
    		} catch (Exception var15) {
    			entity = null;
    		}

    		if (entity != null) {
    			entity.setLocationAndAngles(dx, dy, dz, this.storage.transformRotationYaw(entity.rotationYaw, data.rotation, data.mirror), entity.rotationPitch);
    			if (entity instanceof EntityHanging) { // fix paintings (hanging position, align, ...)
    				EntityHanging hanging = (EntityHanging) entity;
    				hanging.facingDirection = this.storage.transformFacing(hanging.facingDirection, data.mirror, data.rotation);
    			}
    			chunk.getWorld().spawnEntity(entity);
    		} else {
    			Interitus.logger.error("Failed to spawn entity from structure data!");
    		}
    	}
    	
	}

	public void takeBlocksFromWorld(World worldIn, BlockPos startPos, BlockPos size, boolean takeEntities) {

		if (size.getX() < 1 || size.getY() < 1 || size.getZ() < 1) {
			this.storage.prepare(0, 0, 0, false);
			return;
		}
		if (size.getX()>128 || size.getY()>128 || size.getZ()>128) {
			size = new BlockPos(Math.min(128, size.getX()), Math.min(128, size.getY()), Math.min(128, size.getZ()));
		}
		
		this.storage.prepare(size.getX(), size.getY(), size.getZ(), false);
		BlockPos endPos = startPos.add(size).add(-1, -1, -1);
		BlockPos posXY1 = new BlockPos(Math.min(startPos.getX(), endPos.getX()), Math.min(startPos.getY(), endPos.getY()), Math.min(startPos.getZ(), endPos.getZ()));
		BlockPos posXY2 = new BlockPos(Math.max(startPos.getX(), endPos.getX()), Math.max(startPos.getY(), endPos.getY()), Math.max(startPos.getZ(), endPos.getZ()));
		// posXY1 / posXY2 -> actual corner points of structure in the world

		for (VarBlockPos pointer : VarBlockPos.getBoxIterator(posXY1, posXY2)) {
			IBlockState block = worldIn.getBlockState(pointer);
			this.storage.registerBlockstate(block);
		}
		
		this.storage.init();
		
		for (VarBlockPos pointer : VarBlockPos.getBoxIterator(posXY1, posXY2)) {
			IBlockState block = worldIn.getBlockState(pointer);
			if (block.getBlock()==Blocks.AIR) {continue;}
			posInStruct.setSubtract(pointer, posXY1);
			TileEntity tileentity = worldIn.getTileEntity(pointer);
			if (tileentity != null) {
				NBTTagCompound nbttagcompound = tileentity.writeToNBT(new NBTTagCompound());
				nbttagcompound.removeTag("x");
				nbttagcompound.removeTag("y");
				nbttagcompound.removeTag("z");
				storage.addBlock(posInStruct, block, nbttagcompound);
			} else {
				storage.addBlock(posInStruct, block, null);
			}
		}
		
		if (takeEntities) {
			List<Entity> list = worldIn.<Entity>getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(posXY1, posXY2.add(1, 1, 1)), EXCLUDE_PLAYERS);
			for (Entity entity : list) {
	            Vec3d pos = new Vec3d(entity.posX - posXY1.getX(), entity.posY - posXY1.getY(), entity.posZ - posXY1.getZ());
	            NBTTagCompound nbtEntity = new NBTTagCompound();
	            entity.writeToNBTOptional(nbtEntity);
	            nbtEntity.removeTag("Pos");
	            this.storage.addEntity(pos, nbtEntity);
	        }
		}
	}
	
    public boolean checkConditions(InteritusChunkGenerator gen, BlockPos pos) {
		for (Condition cond : storage.getConditions()) {
			if (!cond.fullfilled(gen, pos)) {
				return false;
			}
		}
		return true;
	}
    
	public boolean nearOccurence(BlockPos pos, int maxDistSqr) {
		for (StructureData str1 : this.instances) {
			int a = str1.pos.getX() - pos.getX(), b = str1.pos.getZ() - pos.getZ();
			if (a*a+b*b<maxDistSqr) {return true;}
		}
		return false;
	}
    
	public boolean placeBlock(Chunk chunk, BlockPos pos, IBlockState state) {
		if (pos.getY() > 255 || pos.getY() < 0) { return false; }
		World world = chunk.getWorld();
		
		world.captureBlockSnapshots = true; // hacky way to prevent block updates of non-TEs :/
		IBlockState oldState = chunk.setBlockState(pos, state);
		world.captureBlockSnapshots = false;

		if (oldState == null) {
			return false;
		} else {
			if (state.getLightOpacity(world, pos) != oldState.getLightOpacity(world, pos) || state.getLightValue(world, pos) != oldState.getLightValue(world, pos)) {
				world.checkLight(pos);
			}
			if (chunk.isPopulated()) {
				world.notifyBlockUpdate(pos, oldState, state, 2);
			}
			return true;
		}
	}

    public NBTTagCompound writeToNBT(StructurePack pack, NBTTagCompound nbt) {
        nbt.setString("author", this.author);
        this.storage.writeToNBT(pack.mappings, nbt);
        NBTTagList list = new NBTTagList();
        for (WorldGenTask task : this.tasks) {
        	list.appendTag(WorldGenTask.save(task, pack.mappings));
        }
        nbt.setTag("gen", list);
        return nbt;
    }

    public void readFromNBT(StructurePack pack, Map<Integer, Map<Biome, ArrayList<WorldGenTask>>> genList, NBTTagCompound nbt) {
        this.storage.readFromNBT(pack, nbt);
        this.author = nbt.getString("author");
        this.tasks.clear();
        NBTTagList list = nbt.getTagList("gen", 10);
        for (int i = 0; i < list.tagCount(); i++) {
        	WorldGenTask task = WorldGenTask.build(this, pack.mappings, list.getCompoundTagAt(i));
        	tasks.add(task);
        	if (genList!=null) {
        		for (int dim : task.dimensions) {
        			Map<Biome, ArrayList<WorldGenTask>> map = genList.get(dim);
        			if (map==null) {
        				map = new HashMap<Biome, ArrayList<WorldGenTask>>();
        				genList.put(dim, map);
        			}
        			for (Biome biome : task.biomes) {
        				ArrayList<WorldGenTask> blist = map.get(biome);
    					if (blist==null) {
    						blist=new ArrayList<WorldGenTask>();
    						map.put(biome, blist);
    					}
    					blist.add(task);
        			}
        		}
        	}
        }
    }

    public void getSize(VarBlockPos target, Rotation rotation) {
        this.storage.size(target, rotation);
    }

    public void setAuthor(String authorIn) {
        this.author = authorIn;
    }

    public String getAuthor() {
        return this.author;
    }

    public static class StructureData {
    	
    	public final Structure str;
    	public final BlockPos pos;
    	public final Mirror mirror;
    	public final Rotation rotation;
    	NBTTagList pendingChunks;
    	
    	public StructureData(Structure str, BlockPos pos, Mirror mirror, Rotation rotation) {
    		this.str=str; this.pos=pos; this.mirror=mirror; this.rotation=rotation;
    	}
    	
    	public StructureData(Structure str, BlockPos pos, int transform) {
    		this.str=str; this.pos=pos; 
    		this.mirror=transform>20?Mirror.LEFT_RIGHT:transform>10?Mirror.FRONT_BACK:Mirror.NONE;
    		transform=transform % 10;
    		this.rotation=transform==1?Rotation.CLOCKWISE_90:transform==2?Rotation.CLOCKWISE_180:transform==3?Rotation.COUNTERCLOCKWISE_90:Rotation.NONE;
    	}
    	
    	@Override
    	public String toString() {
    		return str.name+"@"+pos+" ("+mirror.name()+", "+rotation.name()+")";
    	}

		public int getTransformByte() {
			int a = mirror==Mirror.FRONT_BACK?10:mirror==Mirror.LEFT_RIGHT?20:0;
			a += (rotation==Rotation.CLOCKWISE_90?1:rotation==Rotation.CLOCKWISE_180?2:rotation==Rotation.COUNTERCLOCKWISE_90?3:0);
			return a;
		}
    	
    }

	public void setConditions(ArrayList<Condition> conditions, BlockPos structPos) {
		this.storage.clearConditions();
		for (Condition cond : conditions) {
			this.storage.addCondition(cond.toAbsolute(structPos));
		}
	}
	
	public void getConditions(ArrayList<Condition> conditions, BlockPos structPos) {
		conditions.clear();
		for (Condition cond : this.storage.getConditions()) {
			conditions.add(cond.toRelative(structPos));
		}
	}
	
	public void setLoot(ArrayList<LootEntryPrimer> entries, BlockPos structPos) {
		this.storage.clearLootData();
		for (LootEntryPrimer primer : entries) {
			LootGen[] gens = new LootGen[primer.gens().size()];
			for (int i = 0; i < gens.length; i++) {
				gens[i] = new LootGen(StructurePack.current, primer.gens().get(i));
			}
			this.storage.addLootGen(primer.pos.subtract(structPos), gens);
		}
	}
	
	public void getLoot(ArrayList<LootEntryPrimer> entries, BlockPos structPos) {
		entries.clear();
		for (Entry<Long, LootGen[]> entry : this.storage.getLootEntries()) {
			LootEntryPrimer primer = new LootEntryPrimer(BlockPos.fromLong(entry.getKey()).add(structPos));
			for (LootGen gen : entry.getValue()) {
				primer.gens().add(new LootGenPrimer(gen.count, gen.list.name));
			}
			entries.add(primer);
		}
	}

	protected BlockRegionStorage getStorage() {
		return this.storage;
	}

}
