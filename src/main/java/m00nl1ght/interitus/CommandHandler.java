package m00nl1ght.interitus;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

import m00nl1ght.interitus.block.BlockAdvStructure;
import m00nl1ght.interitus.structures.LootList;
import m00nl1ght.interitus.structures.StructurePack;
import m00nl1ght.interitus.util.Toolkit;
import m00nl1ght.interitus.world.capabilities.WorldDataStorageProvider;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class CommandHandler implements ICommand {

	@Override
	public String getName() {
		return "interitus";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "interitus <text> <text>";
	}

	@Override
	public List<String> getAliases() {
		return Lists.newArrayList("interitus");
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		
		World world=sender.getEntityWorld();
		EntityPlayer player = (sender instanceof EntityPlayer) ? (EntityPlayer)sender : null;
		
		if(args.length == 0) { 
			sender.sendMessage(new TextComponentString(Interitus.MODNAME+" v"+Interitus.VERSION+" is active."));
			if (!world.hasCapability(WorldDataStorageProvider.INTERITUS_WORLD, null)) {
				sender.sendMessage(new TextComponentString("The current world is not an Interitus world."));
				return;
			}
			sender.sendMessage(new TextComponentString("Enter '/interitus ?' for command usage help."));
			return; 
        }
		
		if (args[0].equals("?") || args[0].equals("help")) {
			this.printInstructions(sender);
			return;
		}
		
		if (args[0].equals("profiler")) {
			Interitus.profiler.printToChat(world, sender);
			return;
		}
		
		if (args[0].equals("resetstats")) {
			Interitus.profiler.resetStats();
			sender.sendMessage(new TextComponentString("Done."));
			return;
		}
		
		if (args[0].equals("transform_vanilla")) {
			BlockPos pos = Toolkit.rayTrace(player, 5D, 1.0F).getBlockPos();
			if (pos==null) {
				sender.sendMessage(new TextComponentString("No block in your view found."));
				return; 
			}
			BlockAdvStructure.transformVanilla(world, pos, player);
			return;
		}
		
		if (args[0].equals("pack")) {
			if(args.length == 1) { 
				StructurePack.playerTryEdit(player);
				return; 
	        }
			switch (args[1]) {
				case "refresh":
					StructurePack.updateAvailbalePacks();
					sender.sendMessage(new TextComponentString("Refreshed structure pack list."));
					return;
				case "create":
					if (args.length<3) {sender.sendMessage(new TextComponentString("Incorrect number of arguments!")); return;}
					StructurePack pack1 = args.length<4?null:StructurePack.getPack(args[3]);
					try {
						StructurePack.create(args[2], player, pack1);
					} catch (Exception e) {
						Interitus.logger.error("Error creating structure pack: ", e);
						sender.sendMessage(new TextComponentString("Failed to create structure pack."));
						return;
					}
					sender.sendMessage(new TextComponentString("Created structure pack."));
					return;
				case "save":
					if (StructurePack.get().isReadOnly()) {
						sender.sendMessage(new TextComponentString("Unable to save pack: read-only."));
						return;
					}
					try {
						if(StructurePack.get().save()) {
							sender.sendMessage(new TextComponentString("Saved structure pack."));
						} else {
							sender.sendMessage(new TextComponentString("Nothing could be saved."));
						}
					} catch (IOException e) {
						Interitus.logger.error("Failed to save structure pack: ", e);
						sender.sendMessage(new TextComponentString("Failed to save structure pack."));
					}
					return;
				case "load":
					if (args.length<3) {sender.sendMessage(new TextComponentString("Incorrect number of arguments!")); return;}
					StructurePack pack = StructurePack.getPack(args[2]);
					if (pack==null) {sender.sendMessage(new TextComponentString("Pack not found!")); return;}
					if (StructurePack.load(pack)) {
						sender.sendMessage(new TextComponentString("Loaded structure pack."));
					} else {
						sender.sendMessage(new TextComponentString("Failed to load structure pack."));
					}
					return;
				case "reload":
					try {
						StructurePack.reload();
						sender.sendMessage(new TextComponentString("Loaded structure pack."));
					} catch (IOException e) {
						Interitus.logger.error("Failed to load structure pack: ", e);
						sender.sendMessage(new TextComponentString("Failed to load structure pack."));
					}
				case "delete":
					if (args.length<3) {sender.sendMessage(new TextComponentString("Incorrect number of arguments!")); return;}
					StructurePack pack0 = StructurePack.getPack(args[2]);
					if (pack0==null) {sender.sendMessage(new TextComponentString("Pack not found!")); return;}
					if (pack0.delete()) {
						sender.sendMessage(new TextComponentString("Deleted structure pack."));
					} else {
						sender.sendMessage(new TextComponentString("Failed to delete structure pack."));
					}
					return;
				default:
					sender.sendMessage(new TextComponentString("Invalid command.")); return;
			}
		}
		
		if(args.length == 1) { 
			sender.sendMessage(new TextComponentString("Invalid command. Enter '/interitus ?' for help."));
			return; 
        }
		
		if (args[0].equals("loot")) {
			switch (args[1]) {
			case "new":
				if (StructurePack.get().isReadOnly()) {sender.sendMessage(new TextComponentString("Unable to modify pack: read-only.")); return;}
				if (args.length<3) {sender.sendMessage(new TextComponentString("Incorrect number of arguments!")); return;}
				StructurePack.getOrCreateLootList(args[2]);
				sender.sendMessage(new TextComponentString("Created loot list.")); 
				return;
			case "export":
				if (args.length<3) {sender.sendMessage(new TextComponentString("Incorrect number of arguments!")); return;}
				LootList list = StructurePack.getOrCreateLootList(args[2]);
				list.saveToFile(StructurePack.basePath);
				sender.sendMessage(new TextComponentString("Saved loot list to file.")); 
				return;
			case "get":
				if (args.length<3) {sender.sendMessage(new TextComponentString("Incorrect number of arguments!")); return;}
				LootList list1 = StructurePack.getOrCreateLootList(args[2]);
				sender.sendMessage(new TextComponentString("Got item from loot list: "+list1.get())); 
				return;
			case "add":
				if (StructurePack.get().isReadOnly()) {sender.sendMessage(new TextComponentString("Unable to modify pack: read-only.")); return;}
				if (args.length<6) {sender.sendMessage(new TextComponentString("Incorrect number of arguments!")); return;}
				LootList list0 = StructurePack.getOrCreateLootList(args[2]);
				list0.add(new LootList.LootEntry(player.getHeldItemMainhand(), Double.parseDouble(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5])));
				sender.sendMessage(new TextComponentString("Added "+player.getHeldItemMainhand()+" with weight "+Double.parseDouble(args[3]))); 
				return;
			default:
				sender.sendMessage(new TextComponentString("Invalid command.")); return;
			}
		}
		
		sender.sendMessage(new TextComponentString("Invalid command. Enter '/interitus ?' for help."));
		return; 
		
	}

	private void printInstructions(ICommandSender sender) {
		sender.sendMessage(new TextComponentString("/interitus profiler"));
		sender.sendMessage(new TextComponentString("/interitus loot <new|export|get|add> <...>"));
		sender.sendMessage(new TextComponentString("/interitus pack <create|save|load|reload|delete|copy> <...>"));
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return sender.canUseCommand(4, "interitus");
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}
	
	@Override
	public int compareTo(ICommand arg0) {
		return 0;
	}

}
