package m00nl1ght.interitus.structures;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.google.common.collect.Maps;
import m00nl1ght.interitus.Interitus;
import m00nl1ght.interitus.network.SDefaultPackage;
import m00nl1ght.interitus.util.IDebugObject;
import m00nl1ght.interitus.util.InteritusProfiler;
import m00nl1ght.interitus.util.Toolkit;
import m00nl1ght.interitus.world.InteritusChunkGenerator;
import m00nl1ght.interitus.world.capabilities.ICapabilityWorldDataStorage;
import m00nl1ght.interitus.world.capabilities.WorldDataStorageProvider;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;

public class StructurePack implements IDebugObject {
	
	public static final File basePath = new File(Interitus.MODID+"/structurepacks/");
	static final Map<Integer, Map<Biome, ArrayList<WorldGenTask>>> genTasks = Maps.<Integer, Map<Biome, ArrayList<WorldGenTask>>>newHashMap();
	static final Map<Integer, InteritusChunkGenerator> genList = Maps.<Integer, InteritusChunkGenerator>newHashMap();
	static final StructurePack emptyPack = new DefaultPack();
	static StructurePack current = emptyPack; // @Nonnull
	static final HashMap<String, StructurePack> packs = new HashMap<String, StructurePack>();
	public static final FilenameFilter MCSP_FILTER = new MCSPFilter();
	private static EntityPlayer editing = null;
	
	final Map<String, Structure> structures = Maps.<String, Structure>newHashMap();
	final Map<String, LootList> loot = new HashMap<String, LootList>();
	public final RegistryMappings mappings = new RegistryMappings();
	public final String name;
	boolean read_only;
	String author = "Unknown";
	String description = "";
	float version = Interitus.SUPPORTED_PACK_VERSION_MAX;
	boolean loaded = false;
	
	public StructurePack(String name) {
		this.name = name;
	}

	public static boolean playerTryEdit(EntityPlayer player) {
		if (!player.canUseCommandBlock()) {
			if (!player.getEntityWorld().isRemote) {
				Toolkit.sendMessageToPlayer(player, "You don't have permission to do that.");
			}
			return false;
		} else {
			if (!player.getEntityWorld().isRemote) {
				if (editing == null || editing == player || !Toolkit.isPlayerOnServer(player)) {
					editing = player;
					SDefaultPackage.sendStructurePackGui((EntityPlayerMP) player);
				} else {
					Toolkit.sendMessageToPlayer(player, player.getDisplayNameString() + " is currently editing the active structure pack. Please wait.");
					return false;
				}
			}
			return true;
		}
	}
	
	public static void resetEditingPlayer() {
		editing=null;
	}
	
	public static EntityPlayer getEditingPlayer() {
		return editing;
	}
	
	public static boolean canEdit(EntityPlayer player) {
		return editing==null || editing.getName().equals(player.getName());
	}
	
	void preload() throws IOException {
		File file = new File(basePath, name+".mcsp");
		if (!file.exists()) {throw new FileNotFoundException();}
		ZipInputStream zip = new ZipInputStream(new FileInputStream(file));
		DataInputStream data = new DataInputStream(zip);
		
		NBTTagCompound nbtInfo = null;
		ZipEntry zipEntry = zip.getNextEntry();
        while (zipEntry != null) {
            String entry = zipEntry.getName();
            if (entry.equals("pack")) {
            	nbtInfo = CompressedStreamTools.read(data);
            }
            zipEntry = zip.getNextEntry();
        }
        zip.closeEntry();
		data.close();
		zip.close();
		
		if (nbtInfo==null) {throw new IllegalStateException("Invalid pack: no pack info found");}
		this.readInfoFromNBT(nbtInfo);
	}
	
	void load() throws IOException {
		load(genTasks);
	}
	
	void load(Map<Integer, Map<Biome, ArrayList<WorldGenTask>>> genList) throws IOException {
		File file = new File(basePath, name+".mcsp");
		if (!file.exists()) {throw new FileNotFoundException();}
		ZipInputStream zip = new ZipInputStream(new FileInputStream(file));
		DataInputStream data = new DataInputStream(zip);
		
		NBTTagCompound nbtInfo = null, nbtMappings = null; 
		HashMap<String, NBTTagCompound> struct = new HashMap<String, NBTTagCompound>();
		HashMap<String, NBTTagCompound> loot = new HashMap<String, NBTTagCompound>();
		
		ZipEntry zipEntry = zip.getNextEntry();
        while (zipEntry != null) {
            String entry = zipEntry.getName();
            if (entry.equals("pack")) {
            	nbtInfo = CompressedStreamTools.read(data);
            } else if (entry.equals("mappings")) {
            	nbtMappings = CompressedStreamTools.read(data);
            } else if (entry.startsWith("structures/")) {
            	struct.put(entry.substring(11), CompressedStreamTools.read(data));
            } else if (entry.startsWith("loot/")) {
            	loot.put(entry.substring(5), CompressedStreamTools.read(data));
            } else {
            	Interitus.logger.warn("Found unknown file in structure pack: "+entry);
            }
            zipEntry = zip.getNextEntry();
        }
        zip.closeEntry();
        data.close();
		zip.close();
		
		if (nbtInfo==null) {throw new IOException("Missing structure pack info");}
		this.readInfoFromNBT(nbtInfo);
		
		this.mappings.build(nbtMappings);
		if (!mappings.getMissingBlocks().isEmpty()) {
			Interitus.logger.error("Problems occured while loading the structure pack:");
			for (ResourceLocation loc : mappings.getMissingBlocks()) {
				Interitus.logger.error("The block "+loc+" could not be found in your minecraft installation! (missing mod?)");
			}
			Interitus.logger.error("The structures will still work, but the missing blocks will be replaced with air.");
		}
		
		if (genList!=null) {
			for (Map<Biome, ArrayList<WorldGenTask>> map : genList.values()) {
				map.clear();
			}
		}
		
		this.loot.clear();
		for (Entry<String, NBTTagCompound> entry : loot.entrySet()) {
			LootList list = new LootList(entry.getKey());
			list.loadFromNBT(entry.getValue());
			this.loot.put(list.name, list);
		}
		
		this.structures.clear();
		for (Entry<String, NBTTagCompound> entry : struct.entrySet()) {
			Structure str = new Structure(entry.getKey());
			str.readFromNBT(this, genList, entry.getValue());
			this.structures.put(str.name, str);
		}
		
		mappings.reset();
		loaded=true;
	}
	
	public boolean save() throws IOException {
		return this.save(this);
	}
	
	public boolean save(StructurePack target) throws IOException {
		if (!loaded) {return false;}
		File file = new File(basePath, target.name+".mcsp");
		if (file.exists()) {file.delete();}
		basePath.mkdirs(); file.createNewFile();
		ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file));
		DataOutputStream data = new DataOutputStream(zip);
		
		target.version = Interitus.SUPPORTED_PACK_VERSION_MAX;
		this.nbtToZip(target.writeInfoToNBT(), zip, data, "pack");
		
		mappings.reset();
		for (Structure str : this.structures.values()) {
			NBTTagCompound tag = str.writeToNBT(this, new NBTTagCompound());
			this.nbtToZip(tag, zip, data, "structures/"+str.name);
		}
		for (LootList loot : this.loot.values()) {
			NBTTagCompound tag = loot.saveToNBT(new NBTTagCompound());
			this.nbtToZip(tag, zip, data, "loot/"+loot.name);
		}
		NBTTagCompound mappingsTag = this.mappings.save();
		this.nbtToZip(mappingsTag, zip, data, "mappings");
		
		data.close(); // not shure about this
		zip.close();
		mappings.reset();
		return true;
	}
	
	private void nbtToZip(NBTTagCompound nbt, ZipOutputStream zip, DataOutputStream data, String path) throws IOException {
		ZipEntry entry = new ZipEntry(path);
		zip.putNextEntry(entry);
		CompressedStreamTools.write(nbt, data);
		zip.closeEntry();
	}
	
	private NBTTagCompound writeInfoToNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("author", this.author);
		nbt.setString("description", this.description);
		nbt.setFloat("version", this.version);
		nbt.setBoolean("final", this.read_only);
		return nbt;
	}
	
	private void readInfoFromNBT(NBTTagCompound nbt) {
		this.author = nbt.getString("author");
		this.description = nbt.getString("description");
		this.version = nbt.getFloat("version");
		this.read_only = nbt.getBoolean("final");
	}
	
	void unload(boolean finishPending) {
		if (finishPending) for (InteritusChunkGenerator gen : genList.values()) {
			if (gen==null) {continue;}
			gen.finishPendingStructures();
		}
		this.loaded = false;
		this.loot.clear();
		this.structures.clear();
		for (Map<Biome, ArrayList<WorldGenTask>> map : genTasks.values()) {
			map.clear();
		}
		this.mappings.reset();
	}
	
	public boolean delete() {
		if (current==this) {loadDefault();}
		File file = new File(basePath, name+".mcsp");
		if (file.delete()) {
			packs.remove(this.name);
			StructurePackInfo.markDirty();
			return true;
		}
		return false;
	}
	
	public static StructurePack get() {
		return current;
	}
	
	public static boolean isReadOnly() {
		return current.read_only;
	}
	
	public static Structure getStructure(String name) {
		return current.structures.get(name);
	}
	
	public static Structure getOrCreateStructure(String name) {
		Structure str = current.structures.get(name);
		if (str==null && !current.read_only) {
			str = new Structure(name);
			current.structures.put(str.name, str);
			StructurePackInfo.markDirty();
		}
		return str;
	}
	
	public boolean deleteStructure(String name) {
		if (this.read_only) {return false;}
		if (this.structures.remove(name)==null) {return false;};
		StructurePackInfo.markDirty();
		return true;
	}
	
	public static LootList getLootList(String name) {
		return current.loot.get(name);
	}
	
	public static LootList getOrCreateLootList(String name) {
		LootList str = current.loot.get(name);
		if (str==null && !current.read_only) {
			str = new LootList(name);
			current.loot.put(str.name, str);
			StructurePackInfo.markDirty();
		}
		return str;
	}
	
	public boolean deleteLootList(String name) {
		if (this.read_only) {return false;}
		if (this.loot.remove(name)==null) {return false;};
		StructurePackInfo.markDirty();
		return true;
	}
	
	public static NBTTagCompound getGenTaskClientTag(Structure struct) {
		NBTTagCompound tag = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for (WorldGenTask task : struct.tasks) {
			list.appendTag(WorldGenTask.getClientTag(task));
		}
		tag.setTag("u", list);
		return tag;
	}
	
	public static void updateGenTasks(Structure str, NBTTagCompound tag) {
		if (current.read_only) {return;}
		str.tasks.clear();
		for (Map<Biome, ArrayList<WorldGenTask>> map : genTasks.values()) {
			for (ArrayList<WorldGenTask> list : map.values()) {
				Iterator<WorldGenTask> it = list.iterator();
				while (it.hasNext()) {
					if (it.next().structure==str) {it.remove();}
				}
			}
		}
		NBTTagList tlist = tag.getTagList("u", 10);
		for (int i = 0; i < tlist.tagCount(); i++) {
			WorldGenTask task = WorldGenTask.buildFromClient(str, tlist.getCompoundTagAt(i));
			str.tasks.add(task);
			for (int dim : task.dimensions) {
				Map<Biome, ArrayList<WorldGenTask>> map = genTasks.get(dim);
				if (map==null) {
    				map = new HashMap<Biome, ArrayList<WorldGenTask>>();
    				genTasks.put(dim, map);
    			}
				for (Biome biome : task.biomes) {
					ArrayList<WorldGenTask> blist = map.get(biome);
					if (blist==null) {
						blist=new ArrayList<WorldGenTask>();
						map.put(biome, blist);
					}
					blist.add(task);
				}
			}
		}
		StructurePackInfo.markDirty();
	}
	
	public void setDescription(String text) {
		if (this.read_only) {return;}
		this.description=text;
		StructurePackInfo.markDirty();
	}
	
	public boolean sign(EntityPlayer player) {
		if (this.read_only) {return false;}
		this.read_only=true;
		String org = this.author;
		this.author=player.getName();
		try {
			if (!this.save()) {this.read_only=false; this.author=org; return false;}
		} catch (Exception e) {
			Interitus.logger.error("Failed to save pack (signing): ", e);
			this.read_only=false;
			this.author=org;
			return false;
		}
		StructurePackInfo.markDirty();
		return true;
	}
	
	public boolean isLoaded() {
		return this.loaded;
	}
	
	public String getAuthor() {
		return this.author;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public float getVersion() {
		return this.version;
	}
	
	public boolean isVersionSupported() {
		if (version<Interitus.SUPPORTED_PACK_VERSION_MIN) {return false;}
		if (version>Interitus.SUPPORTED_PACK_VERSION_MAX) {return false;}
		return true;
	}
	
	public static Map<Biome, ArrayList<WorldGenTask>> initGen(InteritusChunkGenerator gen) {
		int dim = gen.world.provider.getDimension();
		Map<Biome, ArrayList<WorldGenTask>> map = current.genTasks.get(dim);
		if (map==null) {genTasks.put(dim, map=new HashMap<Biome, ArrayList<WorldGenTask>>());}
		genList.put(dim, gen);
		return map;
	}
	
	public static void serverStopped() {
		genList.clear();
		current.unload(false);
		current = emptyPack;
		StructurePackInfo.markDirty();
	}
	
	public static void loadDefault() {
		if (current!=emptyPack) {
			current.unload(true);
			updateCurrentPack(emptyPack);
		}
	}
	
	public static StructurePack getPack(String name) {
		if (name.equals("Default")) {return emptyPack;}
		return packs.get(name);
	}
	
	public static void create(String name, EntityPlayer player, StructurePack from) {
		if (name.isEmpty()) {throw new IllegalStateException("new pack name is empty!");}
		if (getPack(name)!=null) {throw new IllegalStateException("pack already exists!");}
		StructurePack pack = new StructurePack(name);
		if (from==null || from.name.equals("Default")) {
			pack.author = player.getName();
			pack.description = "";
			pack.loaded = true;
			try {
				if (!pack.save()) {
					throw new IllegalStateException("failed to save new pack!");
				}
			} catch (IOException e) {
				throw new IllegalStateException("Error saving new structure pack <"+pack.name+">: ", e);
			}
		} else {
			File fromFile = new File(basePath, from.name+".mcsp");
			if (!fromFile.exists()) {throw new IllegalStateException("pack file to copy not found!");}
			pack.author = from.read_only?from.author:player.getName();
			pack.description = from.description;
			if (from.loaded) {
				try {
					from.save(pack);
				} catch (Exception e) {
					throw new IllegalStateException("failed to save copy of pack!", e);
				}
			} else {
				try {
					from.load((Map<Integer, Map<Biome, ArrayList<WorldGenTask>>>) null);
					from.save(pack);
					from.unload(false);
				} catch (Exception e) {
					from.unload(false);
					throw new IllegalStateException("failed to load/save copy of pack!", e);
				}
			}
		}
		packs.put(name, pack);
		StructurePackInfo.markDirty();
	}

	public static boolean load(StructurePack pack) {
		current.unload(true);
		try {
			pack.load();
		} catch (IOException e) {
			Interitus.logger.error("Error loading structure pack <"+pack.name+">: ", e);
			loadDefault();
			return false;
		}
		updateCurrentPack(pack);
		return true;
	}
	
	private static void updateCurrentPack(StructurePack pack) {
		current = pack;
		StructurePackInfo.markDirty();
		WorldServer world = DimensionManager.getWorld(0);
		if (world==null) {return;}
		ICapabilityWorldDataStorage data = world.getCapability(WorldDataStorageProvider.INTERITUS_WORLD, null);
		if (data!=null) {
			data.setActivePack(pack.name);
		}
	}
	
	public static void updateAvailbalePacks() {
		packs.clear();
		StructurePackInfo.markDirty();
		basePath.mkdirs();
		for (File file : basePath.listFiles(MCSP_FILTER)) {
			String name = file.getName();
			name = name.substring(0, name.length()-5);
			if (name.isEmpty() || name.equals("Default")) {continue;}
			StructurePack pack = new StructurePack(name);
			try {
				pack.preload();
			} catch (IOException e) {
				Interitus.logger.error("Error pre-loading structure pack <"+pack.name+">: ", e);
				continue;
			}
			if (!pack.isVersionSupported()) {
				Interitus.logger.error("The structure pack <"+pack.name+"> can not be loaded because pack version "+pack.version+" is not supported.");
				return;
			}
			packs.put(name, pack);
		}
	}
	
	public static boolean isDefault() {
		return current==emptyPack;
	}
	
	@Override
	public void debugMsg(ICommandSender sender) {
		InteritusProfiler.send(sender, "StructurePack: "+name+" v"+this.version+(this.loaded?" L ":" ")+(editing!=null?" editing: "+editing.getName():""));
		for (InteritusChunkGenerator gen : genList.values()) {
			gen.debugMsg(sender);
		}
	}
	
	@Override
	public void resetStats() {
		for (InteritusChunkGenerator gen : genList.values()) {
			gen.resetStats();
		}
	}
	
	private static class MCSPFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".mcsp");
		}
	}
	
	private static class DefaultPack extends StructurePack {

		public DefaultPack() {
			super("Default");
			this.loaded=true;
			read_only = true;
			author = "";
			description = "Empty, behaves like vanilla.";
			StructurePackInfo.markDirty();
		}
		
		@Override
		void preload() throws IOException {}
		
		@Override
		void load(Map<Integer, Map<Biome, ArrayList<WorldGenTask>>> genList) throws IOException {
			if (genList!=null) {
				for (Map<Biome, ArrayList<WorldGenTask>> map : genList.values()) {
					map.clear();
				}
			}
		}
		
		@Override
		public boolean save(StructurePack target) throws IOException {return false;}
		
		@Override
		void unload(boolean finishPending) {}
		
		@Override
		public boolean delete() {return false;}
		
	}

}
