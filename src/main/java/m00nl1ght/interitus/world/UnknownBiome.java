package m00nl1ght.interitus.world;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

public class UnknownBiome extends Biome {
	
	private static BiomeProperties DEFAULT_PROPERTIES = new BiomeProperties("Unknown");
	
	private ResourceLocation registryName;

	public UnknownBiome(ResourceLocation registryName) {
		super(DEFAULT_PROPERTIES);
		this.registryName = registryName;
	}
	
	public ResourceLocation getIDName() {
		return this.registryName;
	}

}
