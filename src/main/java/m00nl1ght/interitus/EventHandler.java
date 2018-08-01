package m00nl1ght.interitus;

import m00nl1ght.interitus.block.ModBlock;
import m00nl1ght.interitus.crafting.ModCrafting;
import m00nl1ght.interitus.item.ModItem;
import m00nl1ght.interitus.structures.StructurePack;
import m00nl1ght.interitus.util.Toolkit;
import m00nl1ght.interitus.world.InteritusChunkGenWrapper;
import m00nl1ght.interitus.world.capabilities.ICapabilityWorldDataStorage;
import m00nl1ght.interitus.world.capabilities.WorldDataStorage;
import m00nl1ght.interitus.world.capabilities.WorldDataStorageProvider;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

@EventBusSubscriber()
public class EventHandler {
	
	@SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        ModBlock.initBlocks(event);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        ModItem.initItems(event);
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
    	ModCrafting.init(event);
    }
    
    @SubscribeEvent(receiveCanceled=true)
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		ItemStack stack = event.getPlayer().getHeldItemMainhand();
		if (stack.getItem() == ModItem.STRUCTURE_TOOL) {
			event.setCanceled(true);
			ModItem.STRUCTURE_TOOL.onBlockLeftClicked(event.getPlayer(), event.getPos(), stack);
			return;
		}
	}
	
	@SubscribeEvent
	public static void attachCapabilityWorld(AttachCapabilitiesEvent<World> event) {
		IChunkProvider provider = event.getObject().getChunkProvider();
		if (!event.getObject().isRemote && provider instanceof ChunkProviderServer) {
			try {
				ChunkProviderServer prov = (ChunkProviderServer) provider;
				IChunkGenerator gen = new InteritusChunkGenWrapper(event.getObject(), prov.chunkGenerator);
				ReflectionHelper.setPrivateValue(ChunkProviderServer.class, prov, gen, "chunkGenerator", "field_186029_c", "c");
				event.addCapability(WorldDataStorage.NAME, new WorldDataStorageProvider((WorldServer)event.getObject()));
				Interitus.logger.info("Replaced chunk provider for dimension "+event.getObject().provider.getDimension()+".");
			} catch (Exception e) {
				Interitus.logger.error("Failed to replace chunk generator for dimension "+event.getObject().provider.getDimension()+" using reflection: ", e);
				return;
			}
		}
	}
	
	public static void onLoadOverworld(ICapabilityWorldDataStorage instance) {
		WorldServer world = instance.getWorld();
		String name = instance.getActivePack();
		if (name.isEmpty()) {
			Interitus.logger.error("This world save contains invalid interitus data, loading the default structure pack.");
			StructurePack.loadDefault();
			instance.setActivePack("Default");
		} else {
			StructurePack pack = StructurePack.getPack(name);
			if (pack == null) {
				Interitus.logger.error("Structure pack <" + name + "> not found. Loading the default pack instead...");
				StructurePack.loadDefault();
				//TODO warning for glitchy structures
				return;
			}
			if (StructurePack.load(pack)) {
				Interitus.logger.info("Structure pack <" + name + "> loaded successfully.");
			} else {
				//TODO warning for glitchy structures
			}
			
		}
	}

	@SubscribeEvent
	public static void onPlayerJoin(PlayerLoggedInEvent event) {
		ICapabilityWorldDataStorage data = event.player.world.getCapability(WorldDataStorageProvider.INTERITUS_WORLD, null);
		if (data==null) {
			Toolkit.sendMessageToPlayer(event.player, "Something is wrong with this world save, it has no valid Interitus data.");
		} else {
			if (StructurePack.get().name.equals("Default")) {
				Toolkit.sendMessageToPlayer(event.player, "Welcome to Interitus. Currently no structure pack is active. Use '/interitus pack' to load or create one.");
			} else {
				Toolkit.sendMessageToPlayer(event.player, "Welcome to Interitus. The structure pack <"+StructurePack.get().name+"> has been loaded successfully.");
			}
			Toolkit.sendMessageToPlayer(event.player, "WARNING: This is an experimental alpha version of Interitus. Important features are missing or incomplete, and a lot of things will change in future versions!");
		}
	}

}
