package m00nl1ght.interitus.block.tileentity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import m00nl1ght.interitus.Interitus;
import m00nl1ght.interitus.block.BlockAdvStructure;
import m00nl1ght.interitus.item.ItemStructureDataTool;
import m00nl1ght.interitus.item.ModItem;
import m00nl1ght.interitus.network.SDefaultPackage;
import m00nl1ght.interitus.structures.Structure;
import m00nl1ght.interitus.structures.StructurePack;
import m00nl1ght.interitus.structures.StructurePackInfo;
import m00nl1ght.interitus.util.Toolkit;
import m00nl1ght.interitus.structures.BlockRegionStorage.Condition;
import m00nl1ght.interitus.structures.Structure.StructureData;
import m00nl1ght.interitus.world.InteritusChunkGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityAdvStructure extends TileEntity {
	
	private String name = "";
    private String author = "";
    private String metadata = "";
    private BlockPos position = new BlockPos(0, 1, 0);
    private BlockPos size = BlockPos.ORIGIN;
    private Mirror mirror = Mirror.NONE;
    private Rotation rotation = Rotation.NONE;
    private Mode mode = Mode.LOAD;
    private boolean ignoreEntities = true;
    private boolean powered;
    private boolean showAir;
    private boolean showBoundingBox = true;
    private boolean acceptUpdates = true;
    private final ArrayList<Condition> conditions = new ArrayList<Condition>();
    private final ArrayList<LootEntryPrimer> loot = new ArrayList<LootEntryPrimer>();
    private EntityPlayer editing = null;
    
	public void giveDataTool(EntityPlayerMP player) {
		player.inventory.clearMatchingItems(ModItem.STRUCTURE_TOOL, -1, -1, ItemStructureDataTool.addTagForPos(new NBTTagCompound(), pos));
		player.addItemStackToInventory(ItemStructureDataTool.getItemForPos(this.pos));
	}
    
	private void updateBlockState() {
		if (this.world != null) {
			BlockPos blockpos = this.getPos();
			IBlockState iblockstate = this.world.getBlockState(blockpos);

			if (iblockstate.getBlock() instanceof BlockAdvStructure) {
				this.world.setBlockState(blockpos, iblockstate.withProperty(BlockAdvStructure.MODE, this.mode), 2);
			}
		}
	}
	
	public void markDirtyFlagged() {
		this.markDirty();
		IBlockState iblockstate1 = world.getBlockState(this.pos);
		world.notifyBlockUpdate(this.pos, iblockstate1, iblockstate1, 3);
	}
	
	@Nullable @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		if (!acceptUpdates) {return;}
		this.readFromNBT(pkt.getNbtCompound());
    }
	
	@SideOnly(Side.CLIENT)
	public void setAcceptUpdates(boolean flag) {
		this.acceptUpdates = flag;
	}

	@Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

	public boolean usedBy(EntityPlayer player) {
		if (!player.canUseCommandBlock()) {
			if (!player.getEntityWorld().isRemote) {
				Toolkit.sendMessageToPlayer(player, "You don't have permission to use this.");
			}
			return false;
		} else {
			if (!player.getEntityWorld().isRemote) {
				if (editing==null || editing==player || !Toolkit.isPlayerOnServer(player)) {
					this.editing = player;
					SDefaultPackage.sendStructureBlockGui((EntityPlayerMP)player, this);
				} else {
					Toolkit.sendMessageToPlayer(player, player.getDisplayNameString()+" is currently using this.");
					return false;
				}
			}
			return true;
		}
	}
	
	public boolean editLootData(EntityPlayer player, BlockPos pos) {
		if (!player.canUseCommandBlock()) {
			if (!player.getEntityWorld().isRemote) {
				Toolkit.sendMessageToPlayer(player, "You don't have permission to use this.");
			}
			return false;
		} else {
			if (!player.getEntityWorld().isRemote) {
				if (editing==null || editing==player || !Toolkit.isPlayerOnServer(player)) {
					this.editing = player;
					SDefaultPackage.sendStructureLootGui((EntityPlayerMP)player, this, pos);
				} else {
					Toolkit.sendMessageToPlayer(player, player.getDisplayNameString()+" is currently editing this structure block.");
					return false;
				}
			}
			return true;
		}
	}

	public boolean editData(EntityPlayer player) {
		if (!player.canUseCommandBlock()) {
			if (!player.getEntityWorld().isRemote) {
				Toolkit.sendMessageToPlayer(player, "You don't have permission to use this.");
			}
			return false;
		} else {
			if (!player.getEntityWorld().isRemote) {
				if (editing==null || editing==player || !Toolkit.isPlayerOnServer(player)) {
					this.editing = player;
					SDefaultPackage.sendStructureDataGui((EntityPlayerMP)player, this);
				} else {
					Toolkit.sendMessageToPlayer(player, player.getDisplayNameString()+" is currently editing this structure block.");
					return false;
				}
			}
			return true;
		}
	}
	
	public void setEditingPlayer(EntityPlayer player) {
		this.editing = player;
	}
	
	public void resetEditingPlayer() {
		this.editing = null;
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}

    public String getName() {
        return this.name;
    }

    public void setName(String nameIn) {
        String s = nameIn;
        for (char c0 : ChatAllowedCharacters.ILLEGAL_STRUCTURE_CHARACTERS) {
            s = s.replace(c0, '_');
        }
        this.name = s;
    }

    public void createdBy(EntityLivingBase creator) {
        if (!StringUtils.isNullOrEmpty(creator.getName())) {
            this.author = creator.getName();
        }
    }

    public BlockPos getPosition() {
        return this.position;
    }

    public void setPosition(BlockPos posIn) {
        this.position = posIn;
    }

    public BlockPos getStructureSize() {
    	return this.size;
    }

    public void setSize(BlockPos sizeIn) {
        this.size = sizeIn;
    }

    public Mirror getMirror() {
        return this.mirror;
    }

    public void setMirror(Mirror mirrorIn) {
        this.mirror = mirrorIn;
    }

    public void setRotation(Rotation rotationIn) {
        this.rotation = rotationIn;
    }

    public void setMetadata(String metadataIn) {
        this.metadata = metadataIn;
    }

    public Rotation getRotation() {
        return this.rotation;
    }

    public String getMetadata() {
        return this.metadata;
    }

    public Mode getMode() {
        return this.mode;
    }

    public void setMode(Mode modeIn) {
        this.mode = modeIn;
        IBlockState iblockstate = this.world.getBlockState(this.getPos());

        if (iblockstate.getBlock() instanceof BlockAdvStructure) {
            this.world.setBlockState(this.getPos(), iblockstate.withProperty(BlockAdvStructure.MODE, modeIn), 2);
        }
    }

    public void setIgnoresEntities(boolean ignoreEntitiesIn) {
        this.ignoreEntities = ignoreEntitiesIn;
    }
    
    public String getAuthor() {
    	return author;
    }

    public void nextMode() {
        switch (this.getMode()) {
            case SAVE:
                this.setMode(Mode.LOAD);
                break;
            case LOAD:
                this.setMode(Mode.CORNER);
                break;
            case CORNER:
                this.setMode(Mode.SAVE);
                break;
            case DATA:
                this.setMode(Mode.SAVE);
        }
    }

    public boolean ignoresEntities() {
        return this.ignoreEntities;
    }

	public boolean detectSize() { // TODO check: negative size allowed?!
		if (this.mode != Mode.SAVE) {
			return false;
		} else {
			BlockPos blockpos = this.getPos();
			int i = 80;
			BlockPos blockpos1 = new BlockPos(blockpos.getX() - 80, 0, blockpos.getZ() - 80);
			BlockPos blockpos2 = new BlockPos(blockpos.getX() + 80, 255, blockpos.getZ() + 80);
			List<TileEntityAdvStructure> list = this.getNearbyCornerBlocks(blockpos1, blockpos2);
			List<TileEntityAdvStructure> list1 = this.filterRelatedCornerBlocks(list);

			if (list1.size() < 1) {
				return false;
			} else {
				StructureBoundingBox structureboundingbox = this.calculateEnclosingBoundingBox(blockpos, list1);

				if (structureboundingbox.maxX - structureboundingbox.minX > 1 && structureboundingbox.maxY - structureboundingbox.minY > 1 && structureboundingbox.maxZ - structureboundingbox.minZ > 1) {
					this.position = new BlockPos(structureboundingbox.minX - blockpos.getX() + 1, structureboundingbox.minY - blockpos.getY() + 1, structureboundingbox.minZ - blockpos.getZ() + 1);
					this.size = new BlockPos(structureboundingbox.maxX - structureboundingbox.minX - 1, structureboundingbox.maxY - structureboundingbox.minY - 1, structureboundingbox.maxZ - structureboundingbox.minZ - 1);
					this.markDirty();
					IBlockState iblockstate = this.world.getBlockState(blockpos);
					this.world.notifyBlockUpdate(blockpos, iblockstate, iblockstate, 3);
					return true;
				} else {
					return false;
				}
			}
		}
	}

	private List<TileEntityAdvStructure> filterRelatedCornerBlocks(List<TileEntityAdvStructure> p_184415_1_) {
		Iterable<TileEntityAdvStructure> iterable = Iterables.filter(p_184415_1_, new Predicate<TileEntityAdvStructure>() {
			@Override
			public boolean apply(@Nullable TileEntityAdvStructure p_apply_1_) {
				return p_apply_1_.mode == Mode.CORNER && TileEntityAdvStructure.this.name.equals(p_apply_1_.name);
			}
		});
		return Lists.newArrayList(iterable);
	}

	private List<TileEntityAdvStructure> getNearbyCornerBlocks(BlockPos p_184418_1_, BlockPos p_184418_2_) {
		List<TileEntityAdvStructure> list = Lists.<TileEntityAdvStructure> newArrayList();

		for (BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(p_184418_1_, p_184418_2_)) {
			IBlockState iblockstate = this.world.getBlockState(blockpos$mutableblockpos);

			if (iblockstate.getBlock() instanceof BlockAdvStructure) {
				TileEntity tileentity = this.world.getTileEntity(blockpos$mutableblockpos);

				if (tileentity != null && tileentity instanceof TileEntityAdvStructure) {
					list.add((TileEntityAdvStructure) tileentity);
				}
			}
		}

		return list;
	}

	private StructureBoundingBox calculateEnclosingBoundingBox(BlockPos p_184416_1_, List<TileEntityAdvStructure> p_184416_2_) {
		StructureBoundingBox structureboundingbox;

		if (p_184416_2_.size() > 1) {
			BlockPos blockpos = p_184416_2_.get(0).getPos();
			structureboundingbox = new StructureBoundingBox(blockpos, blockpos);
		} else {
			structureboundingbox = new StructureBoundingBox(p_184416_1_, p_184416_1_);
		}

		for (TileEntityAdvStructure tileentitystructure : p_184416_2_) {
			BlockPos blockpos1 = tileentitystructure.getPos();

			if (blockpos1.getX() < structureboundingbox.minX) {
				structureboundingbox.minX = blockpos1.getX();
			} else if (blockpos1.getX() > structureboundingbox.maxX) {
				structureboundingbox.maxX = blockpos1.getX();
			}

			if (blockpos1.getY() < structureboundingbox.minY) {
				structureboundingbox.minY = blockpos1.getY();
			} else if (blockpos1.getY() > structureboundingbox.maxY) {
				structureboundingbox.maxY = blockpos1.getY();
			}

			if (blockpos1.getZ() < structureboundingbox.minZ) {
				structureboundingbox.minZ = blockpos1.getZ();
			} else if (blockpos1.getZ() > structureboundingbox.maxZ) {
				structureboundingbox.maxZ = blockpos1.getZ();
			}
		}

		return structureboundingbox;
	}

    public void writeCoordinates(ByteBuf buf) {
        buf.writeInt(this.pos.getX());
        buf.writeInt(this.pos.getY());
        buf.writeInt(this.pos.getZ());
    }
    
    public boolean save() {
        if (this.mode == Mode.SAVE && !this.world.isRemote && !StringUtils.isNullOrEmpty(this.name)) {
        	if (StructurePack.isReadOnly()) {return false;}
        	BlockPos pos1 = this.pos.add(this.position);
            Structure template = StructurePack.getOrCreateStructure(name);
            try {
            	template.takeBlocksFromWorld(world, pos1, this.size, !this.ignoreEntities);
            } catch (Exception e) {
            	Interitus.logger.error("Error saving structure <"+this.name+">:", e);
            	return false;
            }
            template.setConditions(this.conditions, pos1);
            template.setLoot(this.loot, pos1);
            template.setAuthor(this.author);
            StructurePackInfo.markDirty();
            return true;
        } else {
            return false; 
        }
    }
    
	public int replaceBlocks(Block from, Block to) {
		int count = 0;
		BlockPos pos1 = pos.add(this.position);
		for (BlockPos p : BlockPos.getAllInBoxMutable(pos1, pos1.add(this.size.add(-1, -1, -1)))) {
			if (world.getBlockState(p).getBlock()==from) {
				world.setBlockState(p, to.getDefaultState()); count++;
			}
		}
		return count;
	}
    
    public boolean load() {
		if (this.mode == Mode.LOAD && !this.world.isRemote && !StringUtils.isNullOrEmpty(this.name)) {
			BlockPos tePos = this.getPos();
			BlockPos pos = tePos.add(this.position);
			Structure template = StructurePack.getStructure(this.name);

			if (template == null) {
				return false;
			} else {
				if (!StringUtils.isNullOrEmpty(template.getAuthor())) {
					this.author = template.getAuthor();
				}

				BlockPos strSize = template.getSize(this.rotation);
				boolean flag = this.size.equals(strSize);

				if (!flag) {
					this.size = strSize;
					this.markDirty();
					IBlockState iblockstate = this.world.getBlockState(tePos);
					this.world.notifyBlockUpdate(tePos, iblockstate, iblockstate, 3);
				}

				if (!flag) {
					return false;
				} else {
					template.getConditions(this.conditions, this.pos.add(this.position));
					template.getLoot(this.loot, this.pos.add(this.position));
					InteritusChunkGenerator gen = InteritusChunkGenerator.get(world);
					if (gen==null) {
						Interitus.logger.error("Placing structures in the world is only possible in Interitus worlds!"); return false;
					}
					return gen.getStructurePositionMap().create(new StructureData(template, pos, mirror, rotation), true, true);
				}
			}
		} else {
			return false;
		}
	}

	public boolean isStructureLoadable() {
		if (this.mode == Mode.LOAD && !this.world.isRemote) {
			return StructurePack.getStructure(this.name) != null;
		} else {
			return false;
		}
	}

    public boolean isPowered() {
        return this.powered;
    }

    public void setPowered(boolean poweredIn) {
        this.powered = poweredIn;
    }

    public boolean showsAir() {
        return this.showAir;
    }

    public void setShowAir(boolean showAirIn) {
        this.showAir = showAirIn;
    }

    public boolean showsBoundingBox() {
        return this.showBoundingBox;
    }

    public void setShowBoundingBox(boolean showBoundingBoxIn) {
        this.showBoundingBox = showBoundingBoxIn;
    }
    
    public ArrayList<Condition> getConditions() {
    	return this.conditions;
    }
    
    public ArrayList<LootEntryPrimer> getLoot() {
    	return this.loot;
    }
    
	public LootEntryPrimer getLootOrNew(BlockPos target) {
		for (LootEntryPrimer primer : this.loot) {
			if (primer.pos.equals(target)) {return primer;}
		}
		LootEntryPrimer primer = new LootEntryPrimer(target);
		this.getLoot().add(primer);
		return primer;
	}

    @Override
	@Nullable
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation("structure_block.hover." + this.mode.modeName, new Object[] {this.mode == Mode.DATA ? this.metadata : this.name});
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString("name", this.name);
        compound.setString("author", this.author);
        compound.setString("metadata", this.metadata);
        compound.setInteger("posX", this.position.getX());
        compound.setInteger("posY", this.position.getY());
        compound.setInteger("posZ", this.position.getZ());
        compound.setInteger("sizeX", this.size.getX());
        compound.setInteger("sizeY", this.size.getY());
        compound.setInteger("sizeZ", this.size.getZ());
        compound.setString("rotation", this.rotation.toString());
        compound.setString("mirror", this.mirror.toString());
        compound.setString("mode", this.mode.toString());
        compound.setBoolean("ignoreEntities", this.ignoreEntities);
        compound.setBoolean("powered", this.powered);
        compound.setBoolean("showair", this.showAir);
        compound.setBoolean("showboundingbox", this.showBoundingBox);
        
        NBTTagList nbtConditions = new NBTTagList();
        for (Condition condition : conditions) {
            nbtConditions.appendTag(condition.writeToNbt(new NBTTagCompound()));
        }
        compound.setTag("conditions", nbtConditions);
        
        NBTTagList nbtLoot = new NBTTagList();
        for (LootEntryPrimer entry: this.loot) {
        	nbtLoot.appendTag(entry.toNBT());
        }
        compound.setTag("loot", nbtLoot);
        return compound;
    }
    
    @Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.setName(compound.getString("name"));
		this.author = compound.getString("author");
		this.metadata = compound.getString("metadata");
		int i = MathHelper.clamp(compound.getInteger("posX"), -64, 64);
		int j = MathHelper.clamp(compound.getInteger("posY"), -64, 64);
		int k = MathHelper.clamp(compound.getInteger("posZ"), -64, 64);
		this.position = new BlockPos(i, j, k);
		int l = MathHelper.clamp(compound.getInteger("sizeX"), 0, 128);
		int i1 = MathHelper.clamp(compound.getInteger("sizeY"), 0, 128);
		int j1 = MathHelper.clamp(compound.getInteger("sizeZ"), 0, 128);
		this.size = new BlockPos(l, i1, j1);

		try {
			this.rotation = Rotation.valueOf(compound.getString("rotation"));
		} catch (IllegalArgumentException var11) {
			this.rotation = Rotation.NONE;
		}

		try {
			this.mirror = Mirror.valueOf(compound.getString("mirror"));
		} catch (IllegalArgumentException var10) {
			this.mirror = Mirror.NONE;
		}
		
		try {
			this.mode = Mode.valueOf(compound.getString("mode"));
		} catch (IllegalArgumentException var9) {
			this.mode = Mode.DATA;
		}


		this.ignoreEntities = compound.getBoolean("ignoreEntities");
		this.powered = compound.getBoolean("powered");
		this.showAir = compound.getBoolean("showair");
		this.showBoundingBox = compound.getBoolean("showboundingbox");
		
		conditions.clear();
		NBTTagList nbtConditions = compound.getTagList("conditions", 10);
        for (int c = 0; c < nbtConditions.tagCount(); ++c) {
            conditions.add(Condition.readFromNBT(nbtConditions.getCompoundTagAt(c)));
        }
        
        loot.clear();
        NBTTagList nbtLoot = compound.getTagList("loot", 10);
        for (int c = 0; c < nbtLoot.tagCount(); ++c) {
            loot.add(new LootEntryPrimer(nbtLoot.getCompoundTagAt(c)));
        }

		this.updateBlockState();
	}
    
    public static final class LootEntryPrimer {
    	
    	public final BlockPos pos;
    	private ArrayList<LootGenPrimer> gens;
    	
    	public LootEntryPrimer(BlockPos pos) {
    		this.pos = pos;
    		this.gens = new ArrayList<LootGenPrimer>();
    	}
    	
    	public LootEntryPrimer(NBTTagCompound tag) {
    		this.pos = BlockPos.fromLong(tag.getLong("pos"));
    		this.gens = new ArrayList<LootGenPrimer>();
    		NBTTagList gens = tag.getTagList("gens", 10);
            for (int v = 0; v < gens.tagCount(); v++) {
            	this.gens.add(new LootGenPrimer(gens.getCompoundTagAt(v)));
            }
    	}
    	
    	public NBTTagCompound toNBT() {
    		NBTTagCompound tag = new NBTTagCompound();
        	tag.setLong("pos", this.pos.toLong());
        	NBTTagList nbtLootGens = new NBTTagList();
        	for (LootGenPrimer gen : this.gens) {
        		nbtLootGens.appendTag(gen.toNBT());
        	}
        	tag.setTag("gens", nbtLootGens);
        	return tag;
    	}
    	
    	public ArrayList<LootGenPrimer> gens() {
    		return gens;
    	}
    	
    }
    
    public static final class LootGenPrimer {
    	
    	private int amount;
    	private String list;
    	
    	public LootGenPrimer(int amount, String list) {
    		this.amount = amount; this.list = list;
    	}
    	
    	public LootGenPrimer(NBTTagCompound tag) {
			this.amount = tag.getInteger("count");
			this.list = tag.getString("list");
		}

		public NBTBase toNBT() {
    		NBTTagCompound tag = new NBTTagCompound();
    		tag.setInteger("count", this.amount);
    		tag.setString("list", this.list);
			return tag;
		}

		public int amount() {return amount;}
    	public String list() {return list;}

		public void setAmount(int i) {
			this.amount = i;
		}

		public void setList(String name) {
			this.list=name;
		}
    	
    }
	
	public static enum Mode implements IStringSerializable {
		
		SAVE("save", "Save", 0), LOAD("load", "Load", 1), CORNER("corner", "Corner", 2), DATA("data", "Data", 3);

		private static final Mode[] MODES = new Mode[values().length];
		private final String modeName, title;
		private final int modeId;

		private Mode(String modeNameIn, String title, int modeIdIn) {
			this.modeName = modeNameIn;
			this.modeId = modeIdIn;
			this.title = title;
		}

		@Override
		public String getName() {
			return this.modeName;
		}

		public int getModeId() {
			return this.modeId;
		}

		public static Mode getById(int id) {
			return id >= 0 && id < MODES.length ? MODES[id] : MODES[0];
		}

		static {
			for (Mode tileentitystructure$mode : values()) {
				MODES[tileentitystructure$mode.getModeId()] = tileentitystructure$mode;
			}
		}

		public String title() {
			return title;
		}
	}

}
