package m00nl1ght.interitus.world.capabilities;

import net.minecraft.world.WorldServer;

public interface ICapabilityWorldDataStorage {
	
	public void init(WorldServer world);
	
	public WorldServer getWorld();
	
	public String getActivePack();
	
	public void setActivePack(String pack_name);

}
