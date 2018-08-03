package m00nl1ght.interitus.client;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.primitives.Ints;

import m00nl1ght.interitus.client.gui.GuiDropdown;
import m00nl1ght.interitus.client.gui.GuiList;
import m00nl1ght.interitus.client.gui.GuiTextBox;
import m00nl1ght.interitus.client.gui.GuiUtils;
import m00nl1ght.interitus.structures.ConditionType;
import m00nl1ght.interitus.structures.StructurePackInfo;
import m00nl1ght.interitus.util.Toolkit;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;

public abstract class ConditionTypeClient {
	
	private static final HashMap<String, Class<?>> regClient = new HashMap<String, Class<?>>();
	private static final ArrayList<String> types = new ArrayList<String>();
	private static final ArrayList<Block> blockList = new ArrayList<Block>();
	private static ArrayList<String> materialNames = null;
	
	static {
		registerType(ConditionMaterialSetClient.class);
		registerType(ConditionBlockSetClient.class);
		registerType(ConditionHeightClient.class);
		registerType(ConditionCompoundClient.class);
	}
	
	private String name = "";
	protected TypeDropdown typeDD;
	protected boolean negated = false;
	protected boolean type_dirty = false;
	protected int x, y;
	
	public ConditionTypeClient() {}
	public ConditionTypeClient(String name) {this.name = name;}

	public static NBTTagCompound save(ConditionTypeClient cond) {
		NBTTagCompound tag = new NBTTagCompound();
		cond.writeToNBT(tag);
		tag.setString("type", cond.getType());
		tag.setBoolean("n", cond.negated);
		return tag;
	}
	
	public static ConditionTypeClient build(String name, NBTTagCompound tag) {
		String type = tag.getString("type");
		Class<?> clazz = regClient.get(type);
		if (clazz==null) {throw new IllegalStateException("Invalid condition type (client): "+type);}
		ConditionTypeClient ct;
		try {
			ct = (ConditionTypeClient) clazz.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to build condition type (client) <"+type+">: ", e);
		}
		ct.name = name;
		ct.negated = tag.getBoolean("n");
		ct.readFromNBT(tag);
		return ct;
	}
	
	public static ConditionTypeClient create(String type, ConditionTypeClient from) {
		Class<?> clazz = regClient.get(type);
		if (clazz==null) {throw new IllegalStateException("Invalid condition type (client): "+type);}
		ConditionTypeClient ct;
		try {
			ct = (ConditionTypeClient) clazz.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to build condition of type (client) <"+type+">: ", e);
		}
		ct.typeDD = from.typeDD;
		ct.typeDD.ct = ct;
		ct.type_dirty = false;
		ct.negated = false;
		ct.init(from.x, from.y, Minecraft.getMinecraft().fontRenderer);
		return ct;
	}
	
	public static void registerType(Class<?> clazz) {
		ConditionTypeClient ct;
		try {
			ct = (ConditionTypeClient) clazz.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to register condition type (client): "+clazz+" ", e);
		}
		String type = ct.getType();
		if (type.isEmpty()) {throw new IllegalStateException("Failed to register condition type (client): "+ct+" (name is empty)");}
		if (regClient.containsKey(type)) {throw new IllegalStateException("Failed to register condition type (client) <"+type+"> (type already registered)");}
		regClient.put(type, clazz);
		types.add(type);
	}
	
	protected abstract void writeToNBT(NBTTagCompound tag);

	protected abstract void readFromNBT(NBTTagCompound tag);

	public abstract String getType();
	
	public final String getName() {
		return this.name;
	}
	
	public ConditionTypeClient init(int x, int y, FontRenderer renderer) {
		this.x = x; this.y = y;
		if (typeDD==null) {
			typeDD = new TypeDropdown(this, renderer, 100, 20, 50);
			typeDD.setText(getType());
		}
		return this;
	}
	
	public ConditionTypeClient drawGui(int mX, int mY, boolean clicked, FontRenderer fontRenderer) {
		fontRenderer.drawStringWithShadow("Type", x+2, y-2, 16777215);
		typeDD.draw(x, y+10, mX, mY, clicked);
		if (type_dirty) {
			type_dirty = false;
			return create(typeDD.getText(), this);
		}
		fontRenderer.drawStringWithShadow("Negate", x + 135, y + 23, 16777215);
		if (GuiUtils.drawCheckbox(x + 120, y + 20, 11, 11, this.negated, mX, mY, clicked)) {
			this.negated = !this.negated;
		}
		return this;
	}
	
	public void updateGui() {
		//NOOP
	}
	
	public void keyTyped(char typedChar, int keyCode) {
		//NOOP
	}
	
	private static class TypeDropdown extends GuiDropdown {
		
		protected ConditionTypeClient ct;

		public TypeDropdown(ConditionTypeClient ct, FontRenderer renderer, int w, int h, int listH) {
			super(renderer, w, h, listH); this.ct=ct;
		}

		@Override
		protected int getElementCount() {
			return types.size();
		}

		@Override
		protected String getElement(int id) {
			return types.get(id);
		}
		
		@Override
		protected void slotClicked(int id) {
			String org = this.text;
			this.setText(this.getElement(id));
			this.close();
			if (!org.equals(this.text)) {ct.type_dirty = true;}
		}
		
	}
	
	public static class ConditionMaterialSetClient extends ConditionTypeClient {
		
		private boolean[] states;
		private MaterialList list;
		
		public ConditionMaterialSetClient() {}
		public ConditionMaterialSetClient(String name) {super(name);}
		
		@Override
		public ConditionTypeClient init(int x, int y, FontRenderer renderer) {
			if (states==null) states = new boolean[materialNames.size()];
			list = new MaterialList(Minecraft.getMinecraft(), x, y + 40, 308, 180, 15);
			return super.init(x, y, renderer);
		}
		
		@Override
		public ConditionTypeClient drawGui(int mX, int mY, boolean clicked, FontRenderer fontRenderer) {
			list.drawScreen(mX, mY, clicked);
			return super.drawGui(mX, mY, clicked, fontRenderer);
		}

		@Override
		protected void writeToNBT(NBTTagCompound tag) {
			int l = 0;
			for (boolean b : states) {if (b) l++;}
			int[] ids = new int[l]; l = 0;
			for (int i = 0; i < states.length; i++) {
				if (states[i]) {
					ids[l] = i;
					l++;
				}
			}
			tag.setIntArray("b", ids);
		}

		@Override
		protected void readFromNBT(NBTTagCompound tag) {
			if (states==null) states = new boolean[materialNames.size()];
			int[] ids = tag.getIntArray("b");
			for (int i = 0; i < ids.length; i++) {
				states[ids[i]] = true;
			}
		}

		@Override
		public String getType() {
			return "materialSet";
		}
		
		private class MaterialList extends GuiList.GuiCheckboxList {

			public MaterialList(Minecraft client, int x, int y, int w, int h, int entryHeight) {
				super(client, x, y, w, h, entryHeight);
			}

			@Override
			protected void elementClicked(int id, boolean dclick) {
				states[id] = !states[id];
			}

			@Override
			protected String getElement(int id) {
				return materialNames.get(id);
			}

			@Override
			protected boolean getElementState(int id) {
				return states[id];
			}

			@Override
			protected int getElementCount() {
				return materialNames.size();
			}
			
		}
		
	}
	
	public static class ConditionBlockSetClient extends ConditionTypeClient {
		
		private boolean[] states;
		private BlockList list;
		
		public ConditionBlockSetClient() {}
		public ConditionBlockSetClient(String name) {super(name);}
		
		@Override
		public ConditionTypeClient init(int x, int y, FontRenderer renderer) {
			if (states==null) states = new boolean[blockList.size()];
			list = new BlockList(Minecraft.getMinecraft(), x, y + 40, 308, 180, 15);
			return super.init(x, y, renderer);
		}
		
		@Override
		public ConditionTypeClient drawGui(int mX, int mY, boolean clicked, FontRenderer fontRenderer) {
			list.drawScreen(mX, mY, clicked);
			return super.drawGui(mX, mY, clicked, fontRenderer);
		}

		@Override
		protected void writeToNBT(NBTTagCompound tag) {
			int l = 0;
			for (boolean b : states) {if (b) l++;}
			int[] ids = new int[l]; l = 0;
			for (int i = 0; i < states.length; i++) {
				if (states[i]) {
					ids[l] = Block.getIdFromBlock(blockList.get(i));
					l++;
				}
			}
			tag.setIntArray("b", ids);
		}

		@Override
		protected void readFromNBT(NBTTagCompound tag) {
			if (states==null) states = new boolean[blockList.size()];
			int[] ids = tag.getIntArray("b");
			for (int i = 0; i < ids.length; i++) {
				states[blockList.indexOf(Block.getBlockById(ids[i]))] = true;
			}
		}

		@Override
		public String getType() {
			return "blockSet";
		}
		
		private class BlockList extends GuiList.GuiCheckboxList {

			public BlockList(Minecraft client, int x, int y, int w, int h, int entryHeight) {
				super(client, x, y, w, h, entryHeight);
			}

			@Override
			protected void elementClicked(int id, boolean dclick) {
				states[id] = !states[id];
			}

			@Override
			protected String getElement(int id) {
				ResourceLocation name = blockList.get(id).getRegistryName();
				return name.getResourceDomain().equals("minecraft")?name.getResourcePath():"["+name.getResourceDomain()+"] "+name.getResourcePath();
			}

			@Override
			protected boolean getElementState(int id) {
				return states[id];
			}

			@Override
			protected int getElementCount() {
				return blockList.size();
			}
			
		}
		
	}
	
	public static class ConditionHeightClient extends ConditionTypeClient {
		
		private int minV, maxV;
		private GuiTextBox min, max;
		
		public ConditionHeightClient() {}
		public ConditionHeightClient(String name) {super(name);}
		
		@Override
		public ConditionTypeClient init(int x, int y, FontRenderer renderer) {
			min = new GuiTextBox(renderer, 50, 20);
			min.setValidator(Toolkit.INT_VALIDATOR_P);
			min.setText(""+minV);
			max = new GuiTextBox(renderer, 50, 20);
			max.setValidator(Toolkit.INT_VALIDATOR_P);
			max.setText(""+maxV);
			return super.init(x, y, renderer);
		}
		
		@Override
		public ConditionTypeClient drawGui(int mX, int mY, boolean clicked, FontRenderer fontRenderer) {
			fontRenderer.drawStringWithShadow("min", x, y + 40, 14737632);
			min.drawTextBox(x, y + 50, mX, mY, clicked);
			fontRenderer.drawStringWithShadow("max", x, y + 75, 14737632);
			max.drawTextBox(x, y + 85, mX, mY, clicked);
			return super.drawGui(mX, mY, clicked, fontRenderer);
		}
		
		@Override
		public void updateGui() {
			min.updateCursorCounter();
			max.updateCursorCounter();
		}

		@Override
		public void keyTyped(char typedChar, int keyCode) {
			min.textboxKeyTyped(typedChar, keyCode);
			max.textboxKeyTyped(typedChar, keyCode);
		}

		@Override
		protected void writeToNBT(NBTTagCompound tag) {
			Integer i1 = Ints.tryParse(min.getText());
			Integer i2 = Ints.tryParse(max.getText());
			tag.setInteger("min", i1==null?0:i1);
			tag.setInteger("max", i2==null?0:i2);
		}

		@Override
		protected void readFromNBT(NBTTagCompound tag) {
			minV = tag.getInteger("min");
			maxV = tag.getInteger("max");
		}

		@Override
		public String getType() {
			return "groundHeight";
		}
		
	}
	
	public static class ConditionCompoundClient extends ConditionTypeClient {
		
		private boolean[] states;
		private ConditionList list;
		
		public ConditionCompoundClient() {}
		public ConditionCompoundClient(String name) {super(name);}
		
		@Override
		public ConditionTypeClient init(int x, int y, FontRenderer renderer) {
			if (states==null) states = new boolean[StructurePackInfo.condtypes.size()];
			list = new ConditionList(Minecraft.getMinecraft(), x, y + 40, 308, 180, 15);
			return super.init(x, y, renderer);
		}
		
		@Override
		public ConditionTypeClient drawGui(int mX, int mY, boolean clicked, FontRenderer fontRenderer) {
			list.drawScreen(mX, mY, clicked);
			return super.drawGui(mX, mY, clicked, fontRenderer);
		}

		@Override
		protected void writeToNBT(NBTTagCompound tag) {
			NBTTagList list = new NBTTagList();
			for (int i = 0; i < states.length; i++) {
				if (states[i]) {
					list.appendTag(new NBTTagString(StructurePackInfo.condtypes.get(i)));
				}
			}
			tag.setTag("c", list);
		}

		@Override
		protected void readFromNBT(NBTTagCompound tag) {
			if (states==null) states = new boolean[StructurePackInfo.condtypes.size()];
			if (tag.hasKey("c")) {
				NBTTagList list = tag.getTagList("c", 8);
				for (int i = 0; i < list.tagCount(); i++) {
					int idx = StructurePackInfo.condtypes.indexOf(list.getStringTagAt(i));
					if (idx>=0) states[idx] = true;
				}
			}
		}

		@Override
		public String getType() {
			return "compound";
		}
		
		private class ConditionList extends GuiList.GuiCheckboxList {

			public ConditionList(Minecraft client, int x, int y, int w, int h, int entryHeight) {
				super(client, x, y, w, h, entryHeight);
			}

			@Override
			protected void elementClicked(int id, boolean dclick) {
				states[id] = !states[id];
			}

			@Override
			protected String getElement(int id) {
				return StructurePackInfo.condtypes.get(id);
			}

			@Override
			protected boolean getElementState(int id) {
				return states[id];
			}

			@Override
			protected int getElementCount() {
				return StructurePackInfo.condtypes.size();
			}
			
		}
		
	}
	
	public static void initBlocks() {
		blockList.clear();
		for (Block b : Block.REGISTRY) {
			blockList.add(b);
		}
	}
	
	public static boolean isMaterialListPopulated() {
		return materialNames!=null;
	}
	
	public static void setMaterialList(NBTTagCompound tag) {
		if (materialNames!=null) {throw new IllegalStateException();}
		materialNames = new ArrayList<String>();
		NBTTagList list = tag.getTagList("m", 8);
		for (int i = 0; i < list.tagCount(); i++) {
			materialNames.add(list.getStringTagAt(i));
		}
	}
	
	public static void clearMaterialList() {
		materialNames = null;
	}

}
