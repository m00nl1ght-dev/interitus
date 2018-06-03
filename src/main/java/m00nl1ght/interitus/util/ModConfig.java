package m00nl1ght.interitus.util;

import net.minecraft.world.gen.ChunkGeneratorSettings;

public class ModConfig {
	
	public ChunkGeneratorSettings genSettings = ChunkGeneratorSettings.Factory.jsonToFactory("").build();
	
	public int structureTriesPerChunk = 1;			//tries to find a suitable structure position on chunk gen (Default: 1)
	public int chunkCacheMaxSize = 100;				//how many chunks should be cached (Default: 100)
	public int chunkCacheShrink = 20;				//how many chunks should be uncached when the cache is full (Default: 20)
	
}
