package m00nl1ght.interitus.world.capabilities;

import m00nl1ght.interitus.EventHandler;
import m00nl1ght.interitus.structures.StructurePack;
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
		if (instance.getWorld().isRemote || instance.getWorld().provider.getDimension()!=0) {return tag;}
		tag.setString("active_pack", instance.getActivePack());
		InteritusChunkGenWrapper.get(instance.getWorld()).writeToNBT(tag);
		return tag;
	}

	@Override
	public void readNBT(Capability<ICapabilityWorldDataStorage> capability, ICapabilityWorldDataStorage instance, EnumFacing side, NBTBase nbt) {
		if (instance.getWorld().isRemote || instance.getWorld().provider.getDimension()!=0) {return;}
		NBTTagCompound tag = (NBTTagCompound) nbt;
		instance.setActivePack(tag.getString("active_pack"));
		EventHandler.onLoadOverworld(instance);
		if (!StructurePack.isDefault()) {
			InteritusChunkGenWrapper.get(instance.getWorld()).readFromNBT(tag);
		}
	}

}
