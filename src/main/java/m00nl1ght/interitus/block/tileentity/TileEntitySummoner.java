package m00nl1ght.interitus.block.tileentity;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import m00nl1ght.interitus.Main;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class TileEntitySummoner extends TileEntity implements ITickable {
	
	private int particleTicks = 10, spawnTicks = 100, spawnDelayBase = 100, spawnDelayRange = 100, maxEntitiesInRange = 4;
	private EntityEntry entityEntry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation("zombie"));
	private float playerDistance = 16F, rangeH = 8F, rangeVN = 4F, rangeVP = 4F;
	private AxisAlignedBB bbRange;

	@Override
	public void update() {
		if (world instanceof WorldServer) {
			WorldServer sworld = (WorldServer) world;
			
			if (particleTicks<=0) {
				particleTicks=5;
				sworld.spawnParticle(EnumParticleTypes.PORTAL, this.pos.getX()+0.5, this.pos.getY()+0.1, this.pos.getZ()+0.5, 3, 0.3D, 0D, 0.3D, 0.5D);
			} particleTicks--;
			
			if (spawnTicks<=0) {
				spawnTicks=spawnDelayBase+(spawnDelayRange>0?Main.random.nextInt(spawnDelayRange):0);
				this.trySpawn(sworld);
			} spawnTicks--;
		}
	}
	
	public boolean usedBy(EntityPlayer player) {
		if (!player.canUseCommandBlock()) {
			return false;
		} else {
			if (player.getEntityWorld().isRemote) {
				Main.proxy.displaySummonerScreen(this);
			}
			return true;
		}
	}
	
	@Nullable @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		this.readFromNBT(pkt.getNbtCompound());
    }
	
	@Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }
	
	public boolean trySpawn(WorldServer sworld) {
		boolean flag = false;
		for (EntityPlayer player : sworld.playerEntities) {
			if (player.getDistance(pos.getX(), pos.getY(), pos.getZ())<=playerDistance) {
				flag=true; break;
			}
		}
		if (!flag) {return false;}
		int a = sworld.getEntitiesWithinAABB(entityEntry.getEntityClass(), this.bbRange).size();
		if (a>=maxEntitiesInRange) {return false;}
		for (BlockPos bpos : BlockPos.getAllInBoxMutable(pos.getX()-1, pos.getY(), pos.getZ()-1, pos.getX()+1, pos.getY()+1, pos.getZ()+1)) {
			if (!sworld.getBlockState(bpos).getBlock().isPassable(sworld, bpos)) {
				sworld.destroyBlock(bpos, true);
			}
		}
		spawnAt(sworld, pos.getX()+0.5, pos.getY()+0.1, pos.getZ()+0.5);
		return true;
	}
	
	public void spawnAt(WorldServer sworld, double x, double y, double z) {
        Entity entityRaw = entityEntry.newInstance(sworld);
        if (entityRaw instanceof EntityLiving) {
        	EntityLiving entity = (EntityLiving) entityRaw;
        	sworld.spawnParticle(EnumParticleTypes.SMOKE_LARGE, x, y, z, 10, 0.5D, 0.5D, 0.5D, 0.02D);
    		entity.setLocationAndAngles(x, y, z, MathHelper.wrapDegrees(sworld.rand.nextFloat() * 360.0F), 0.0F);
    		entity.rotationYawHead = entity.rotationYaw;
    		entity.renderYawOffset = entity.rotationYaw;
    		entity.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entity)), (IEntityLivingData)null);
    		world.spawnEntity(entity);
    		entity.playLivingSound();
        } else {throw new IllegalStateException("Summoner block failed to spawn the requested entity: "+entityEntry.getName());}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		String id = compound.getString("entity");
		if (id.isEmpty()) {throw new IllegalStateException("TileEntity Summoner could not be loaded from NBT (invalid)");}
		this.entityEntry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
		if (entityEntry==null) {throw new IllegalStateException("TileEntity Summoner could not be loaded from NBT (entity not found: "+id+")");}
		this.maxEntitiesInRange=getInt(compound, "maxEntitiesInRange", 4);
		this.spawnDelayBase=getInt(compound, "spawnDelayBase", 100);
		this.spawnDelayRange=getInt(compound, "spawnDelayRange", 100);
		this.playerDistance=getFloat(compound, "playerDistance", 16F);
		this.rangeH=getFloat(compound, "rangeH", 8F);
		this.rangeVN=getFloat(compound, "rangeVN", 4F);
		this.rangeVP=getFloat(compound, "rangeVP", 4F);
		this.setPos(this.pos); // to update bbRange
	}
	
	private int getInt(NBTTagCompound compound, String key, int def) {
		return compound.hasKey(key)?compound.getInteger(key):def;
	}
	
	private float getFloat(NBTTagCompound compound, String key, float def) {
		return compound.hasKey(key)?compound.getFloat(key):def;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("maxEntitiesInRange", this.maxEntitiesInRange);
		compound.setInteger("spawnDelayBase", this.spawnDelayBase);
		compound.setInteger("spawnDelayRange", this.spawnDelayRange);
		compound.setFloat("playerDistance", this.playerDistance);
		compound.setFloat("rangeH", this.rangeH);
		compound.setFloat("rangeVN", this.rangeVN);
		compound.setFloat("rangeVP", this.rangeVP);
		compound.setString("entity", this.entityEntry.getRegistryName().toString());
		return compound;
	}
	
	@Override
	public void setPos(BlockPos posIn) {
        super.setPos(posIn);
        this.bbRange=new AxisAlignedBB(posIn.getX()-rangeH, posIn.getY()-rangeVN, posIn.getZ()-rangeH, posIn.getX()+rangeH, posIn.getY()+rangeVP, posIn.getZ()+rangeH);
    }

	public String getEntityName() {
		return entityEntry.getName();
	}
	
	public float getPlayerRange() {
		return this.playerDistance;
	}
	
	public int getMaxMobCountInRange() {
		return this.maxEntitiesInRange;
	}
	
	public int getDelayBase() {
		return this.spawnDelayBase;
	}
	
	public int getDelayRange() {
		return this.spawnDelayRange;
	}
	
	public float getRangeH() {
		return this.rangeH;
	}
	
	public float getRangeVN() {
		return this.rangeVN;
	}
	
	public float getRangeVP() {
		return this.rangeVP;
	}
	
	public void writeCoordinates(ByteBuf buf) {
        buf.writeInt(this.pos.getX());
        buf.writeInt(this.pos.getY());
        buf.writeInt(this.pos.getZ());
    }

	public boolean setEntity(String id) {
		EntityEntry newEntity = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
		if (newEntity==null) {return false;}
		this.entityEntry=newEntity;
		return true;
	}
	
	public void setData(float playerDistance, int maxEntitiesInRange, int delayBase, int delayRange, float rangeH, float rangeVN, float rangeVP) {
		this.playerDistance=playerDistance; this.maxEntitiesInRange=maxEntitiesInRange; this.spawnDelayBase=delayBase; this.spawnDelayRange=delayRange; this.rangeH=rangeH; this.rangeVN=rangeVN; this.rangeVP=rangeVP;
	}

}
