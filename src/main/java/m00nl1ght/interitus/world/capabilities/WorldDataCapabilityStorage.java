package m00nl1ght.interitus.world.capabilities;

import m00nl1ght.interitus.world.InteritusChunkGenWrapper;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.Capability.IStorage;


public class WorldDataCapabilityStorage implements IStorage<ICapabilityWorldDataStorage> {
	
	public static void register() {
		CapabilityManager.INSTANCE.register(ICapabilityWorldDataStorage.class, new WorldDataCapabilityStorage(), WorldDataStorage::new);
	}
	
	@Override
	public NBTBase writeNBT(Capability<ICapabilityWorldDataStorage> capability, ICapabilityWorldDataStorage instance, EnumFacing side) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("active_pack", instance.getActivePack());
		InteritusChunkGenWrapper.get(instance.getWorld()).writeToNBT(tag);
		return tag;
	}

	@Override
	public void readNBT(Capability<ICapabilityWorldDataStorage> capability, ICapabilityWorldDataStorage instance, EnumFacing side, NBTBase nbt) {
		NBTTagCompound tag = (NBTTagCompound) nbt;
		instance.setActivePack(tag.getString("active_pack"));
		InteritusChunkGenWrapper.get(instance.getWorld()).readFromNBT(tag);
	}

}
