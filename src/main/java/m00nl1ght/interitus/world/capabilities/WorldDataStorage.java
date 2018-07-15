package m00nl1ght.interitus.world.capabilities;

import m00nl1ght.interitus.Interitus;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;


public class WorldDataStorage implements ICapabilityWorldDataStorage {
	
	public static final ResourceLocation NAME = new ResourceLocation(Interitus.MODID, "interitus_world");
	
	private WorldServer world;
	private String pack = "Default";
	
	@Override
	public WorldServer getWorld() {
		return world;
	}
	
	@Override
	public void init(WorldServer world) {
		this.world=world;
	}

	@Override
	public String getActivePack() {
		return pack;
	}

	@Override
	public void setActivePack(String pack_name) {
		this.pack = pack_name;
	}

}
