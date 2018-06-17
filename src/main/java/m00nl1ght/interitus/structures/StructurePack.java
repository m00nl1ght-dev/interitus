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
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import m00nl1ght.interitus.Interitus;
import m00nl1ght.interitus.network.SDefaultPackage;
import m00nl1ght.interitus.util.Toolkit;
import m00nl1ght.interitus.world.capabilities.ICapabilityWorldDataStorage;
import m00nl1ght.interitus.world.capabilities.WorldDataStorageProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;

public class StructurePack {
	
	public static final File basePath = new File(Interitus.MODID+"/structurepacks/");
	private static final ArrayList<StructureConfig> UNKNOWN_BIOME = Lists.newArrayList();
	static final StructurePack emptyPack = createDefaultPack();
	static StructurePack current = emptyPack; // @Nonnull
	static final HashMap<String, StructurePack> packs = new HashMap<String, StructurePack>();
	public static final FilenameFilter MCSP_FILTER = new MCSPFilter();
	private static EntityPlayer editing = null;
	
	final Map<String, Structure> structures = Maps.<String, Structure>newHashMap();
	final Map<Biome, ArrayList<StructureConfig>> spawns = Maps.<Biome, ArrayList<StructureConfig>>newHashMap();
	final Map<String, LootList> loot = new HashMap<String, LootList>();
	public final RegistryMappings mappings = new RegistryMappings();
	public final String name;
	private boolean read_only;
	private String author = "Unknown";
	private String description = "";
	private float version = Interitus.SUPPORTED_PACK_VERSION_MAX;
	private boolean loaded = false;
	
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
					Toolkit.sendMessageToPlayer(player, player.getDisplayNameString() + " is currently configuring Interitus. Please wait.");
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
	
	private void preload() throws IOException {
		if (this.name.equals("Default")) {return;}
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
	
	private void load() throws IOException {
		if (this.name.equals("Default")) {return;}
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
		
		if (nbtMappings==null) {throw new IOException("Missing block mappings");}
		this.mappings.build(nbtMappings);
		if (!mappings.getMissingBlocks().isEmpty()) {
			Interitus.logger.error("Problems occured while loading the structure pack:");
			for (ResourceLocation loc : mappings.getMissingBlocks()) {
				Interitus.logger.error("The block "+loc+" could not be found in your minecraft installation! (missing mod?)");
			}
			Interitus.logger.error("The structures will still work, but the missing blocks will be replaced with air.");
		}
		
		this.structures.clear();
		for (Entry<String, NBTTagCompound> entry : struct.entrySet()) {
			Structure str = new Structure(entry.getKey());
			str.readFromNBT(entry.getValue(), mappings);
			this.structures.put(str.name, str);
		}
		
		this.loot.clear();
		for (Entry<String, NBTTagCompound> entry : struct.entrySet()) {
			LootList list = new LootList(entry.getKey());
			list.loadFromNBT(entry.getValue());
			this.loot.put(list.name, list);
		}
		
		mappings.reset();
		loaded=true;
	}
	
	public boolean save() throws IOException {
		return this.save(new File(basePath, name+".mcsp"));
	}
	
	public boolean save(File file) throws IOException {
		if (!loaded) {return false;}
		if (this.name.equals("Default")) {return false;}
		if (file.exists()) {file.delete();}
		basePath.mkdirs(); file.createNewFile();
		ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file));
		DataOutputStream data = new DataOutputStream(zip);
		
		this.nbtToZip(this.writeInfoToNBT(), zip, data, "pack");
		
		mappings.reset();
		for (Structure str : this.structures.values()) {
			NBTTagCompound tag = str.writeToNBT(new NBTTagCompound());
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
	
	private void unload() {
		if (this.name.equals("Default")) {return;}
		this.loaded = false;
		this.loot.clear();
		this.structures.clear();
		this.spawns.clear();
		this.mappings.reset();
	}
	
	public boolean delete() {
		if (this.name.equals("Default")) {return false;}
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
	
	public static ArrayList<StructureConfig> getStrcutureConfigFor(Biome biome) {
		return current.spawns.getOrDefault(biome, UNKNOWN_BIOME);
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
	
	public static StructurePack createDefaultPack() {
		StructurePack pack = new StructurePack("Default");
		pack.loaded=true;
		pack.read_only = true;
		pack.author = "";
		pack.description = "Empty, behaves like vanilla.";
		StructurePackInfo.markDirty();
		return pack;
	}
	
	public static void loadDefault() {
		if (current!=emptyPack) {
			current.unload();
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
		File file = new File(basePath, name+".mcsp");
		if (file.exists()) {throw new IllegalStateException("pack file already exists!");}
		StructurePack pack = new StructurePack(name);
		if (from==null || from.name.equals("Default")) {
			pack.author = player.getName();
			pack.description = "";
			pack.loaded = true;
			try {
				if (!pack.save(file)) {
					throw new IllegalStateException("failed to save new pack!");
				}
			} catch (IOException e) {
				throw new IllegalStateException("Error saving new structure pack <"+pack.name+">: ", e);
			}
		} else {
			File fromFile = new File(basePath, from.name+".mcsp");
			if (!fromFile.exists()) {throw new IllegalStateException("pack file to copy not found!");}
			pack.author = from.author;
			pack.description = from.description;
			pack.version = from.version;
			try {
				Files.copy(fromFile, file);
			} catch (IOException e) {
				throw new IllegalStateException("failed to copy structure pack file!");
			}
		}
		packs.put(name, pack);
		StructurePackInfo.markDirty();
	}
	
	public static boolean load(StructurePack pack) {
		current.unload();
		try {
			pack.load();
		} catch (IOException e) {
			Interitus.logger.error("Error loading structure pack <"+pack.name+">: ", e);
			pack.unload();
			loadDefault();
			return false;
		}
		updateCurrentPack(pack);
		return true;
	}
	
	public static void reload() throws IOException {
		current.load();
	}
	
	private static void updateCurrentPack(StructurePack pack) {
		current = pack;
		StructurePackInfo.markDirty();
		WorldServer world = DimensionManager.getWorld(0);
		ICapabilityWorldDataStorage data = world.getCapability(WorldDataStorageProvider.INTERITUS_WORLD, null);
		if (data!=null) {
			data.setActivePack(pack.name);
		}
	}
	
	public static void updateAvailbalePacks() {
		packs.clear();
		StructurePackInfo.markDirty();
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
	
	private static class MCSPFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".mcsp");
		}
	}

}
