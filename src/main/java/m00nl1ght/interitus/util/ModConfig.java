package m00nl1ght.interitus.util;

import m00nl1ght.interitus.Interitus;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraftforge.common.ForgeModContainer;

public class ModConfig {
	
	public ChunkGeneratorSettings genSettings = ChunkGeneratorSettings.Factory.jsonToFactory("").build();
	
	public int structureTriesPerChunk = 1;			//tries to find a suitable structure position on chunk gen (Default: 1)
	public int chunkCacheMaxSize = 100;				//how many chunks should be cached (Default: 100)
	public int chunkCacheShrink = 20;				//how many chunks should be uncached when the cache is full (Default: 20)
	public boolean debugWorldgen = true;					//send debug msg to chat when structures are generated
	public boolean debugCascadingLag = true;
	
	public static void enshureCascadingFix() {
		if (!ForgeModContainer.fixVanillaCascading) {
    		Interitus.logger.warn("The 'fixVanillaCascading' option in your Forge config is disabled.");
    		Interitus.logger.warn("Overriding it... (Interitus needs it to work correctly)");
    		ForgeModContainer.fixVanillaCascading = true;
    	}
	}
	
}
