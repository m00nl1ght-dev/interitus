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

import m00nl1ght.interitus.Main;
import m00nl1ght.interitus.world.capabilities.ICapabilityWorldDataStorage;
import m00nl1ght.interitus.world.capabilities.WorldDataStorageProvider;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;

public class StructurePack {
	
	public static final File basePath = new File(Main.MODID+"/structurepacks/");
	private static final ArrayList<StructureConfig> UNKNOWN_BIOME = Lists.newArrayList();
	static final StructurePack emptyPack = createDefaultPack();
	static StructurePack current = emptyPack; // @Nonnull
	static final HashMap<String, StructurePack> packs = new HashMap<String, StructurePack>();
	public static final FilenameFilter MCSP_FILTER = new MCSPFilter();
	
	final Map<String, Structure> structures = Maps.<String, Structure>newHashMap();
	final Map<Biome, ArrayList<StructureConfig>> spawns = Maps.<Biome, ArrayList<StructureConfig>>newHashMap();
	final Map<String, LootList> loot = new HashMap<String, LootList>();
	public final RegistryMappings mappings = new RegistryMappings();
	public final String name;
	private boolean read_only;
	private String title = "Interitus Structure Pack";
	private String description = "";
	private float version = Main.SUPPORTED_PACK_VERSION_MAX;
	private boolean loaded = false;
	
	public StructurePack(String name) {
		this.name = name;
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
            	Main.logger.warn("Found unknown file in structure pack: "+entry);
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
			Main.logger.error("Problems occured while loading the structure pack:");
			for (ResourceLocation loc : mappings.getMissingBlocks()) {
				Main.logger.error("The block "+loc+" could not be found in your minecraft installation! (missing mod?)");
			}
			Main.logger.error("The structures will still work, but the missing blocks will be replaced with air.");
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
		nbt.setString("title", this.title);
		nbt.setString("description", this.description);
		nbt.setFloat("version", this.version);
		nbt.setBoolean("final", this.read_only);
		return nbt;
	}
	
	private void readInfoFromNBT(NBTTagCompound nbt) {
		this.title = nbt.getString("title");
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
		return file.delete();
	}
	
	public boolean copy(String dest) {
		if (this.name.equals("Default")) {
			return create(dest);
		}
		File file = new File(basePath, dest+".mcsp");
		if (file.exists()) {return false;}
		try {
			this.save(file);
		} catch (IOException e) {
			Main.logger.error("Failed to copy structure pack "+this.name+" to "+dest+": ", e);
			return false;
		}
		return true;
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
		if (str==null) {
			str = new Structure(name);
			current.structures.put(str.name, str);
		}
		return str;
	}
	
	public static LootList getLootList(String name) {
		return current.loot.get(name);
	}
	
	public static LootList getOrCreateLootList(String name) {
		LootList str = current.loot.get(name);
		if (str==null) {
			str = new LootList(name);
			current.loot.put(str.name, str);
		}
		return str;
	}
	
	public boolean isLoaded() {
		return this.loaded;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public float getVersion() {
		return this.version;
	}
	
	public boolean isVersionSupported() {
		if (version<Main.SUPPORTED_PACK_VERSION_MIN) {return false;}
		if (version>Main.SUPPORTED_PACK_VERSION_MAX) {return false;}
		return true;
	}
	
	public static StructurePack createDefaultPack() {
		StructurePack pack = new StructurePack("Default");
		pack.loaded=true;
		pack.read_only = true;
		pack.title = "Default Structure Pack";
		pack.description = "An empty structure pack that behaves like vanilla minecraft.";
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
	
	public static boolean create(String name) {
		File file = new File(basePath, name+".mcsp");
		if (file.exists()) {return false;}
		if (name.isEmpty() || name.equals("Default")) {return false;}
		StructurePack pack = new StructurePack(name);
		pack.title = name;
		pack.description = "";
		pack.loaded = true;
		try {
			if (!pack.save()) {
				return false;
			}
		} catch (IOException e) {
			Main.logger.error("Error saving new structure pack <"+pack.name+">: ", e);
			return false;
		}
		packs.put(name, pack);
		return true;
	}
	
	public static boolean load(StructurePack pack) {
		try {
			pack.load();
		} catch (IOException e) {
			Main.logger.error("Error loading structure pack <"+pack.name+">: ", e);
			pack.unload();
			return false;
		}
		current.unload();
		updateCurrentPack(pack);
		return true;
	}
	
	public static void reload() throws IOException {
		current.load();
	}
	
	private static void updateCurrentPack(StructurePack pack) {
		current = pack;
		WorldServer world = DimensionManager.getWorld(0);
		ICapabilityWorldDataStorage data = world.getCapability(WorldDataStorageProvider.INTERITUS_WORLD, null);
		if (data!=null) {
			data.setActivePack(pack.name);
		}
	}
	
	public static void updateAvailbalePacks() {
		packs.clear();
		for (File file : basePath.listFiles(MCSP_FILTER)) {
			String name = file.getName();
			name = name.substring(0, name.length()-5);
			if (name.isEmpty() || name.equals("Default")) {continue;}
			StructurePack pack = new StructurePack(name);
			try {
				pack.preload();
			} catch (IOException e) {
				Main.logger.error("Error pre-loading structure pack <"+pack.name+">: ", e);
				continue;
			}
			if (!pack.isVersionSupported()) {
				Main.logger.error("The structure pack <"+pack.name+"> can not be loaded because pack version "+pack.version+" is not supported.");
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
