package m00nl1ght.interitus.world.capabilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;


public class WorldDataStorageProvider implements ICapabilitySerializable<NBTBase> {
	
	@CapabilityInject(ICapabilityWorldDataStorage.class)
	public static final Capability<ICapabilityWorldDataStorage> INTERITUS_WORLD = null;
	
	private WorldServer world;
	private ICapabilityWorldDataStorage instance;
	
	public WorldDataStorageProvider(WorldServer world) {
		this.world=world;
		instance = INTERITUS_WORLD.getDefaultInstance();
		instance.init(world);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == INTERITUS_WORLD;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == INTERITUS_WORLD ? INTERITUS_WORLD.<T> cast(this.instance) : null;
	}

	@Override
	public NBTBase serializeNBT() {
		return INTERITUS_WORLD.getStorage().writeNBT(INTERITUS_WORLD, this.instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		INTERITUS_WORLD.getStorage().readNBT(INTERITUS_WORLD, this.instance, null, nbt);
	}

}
