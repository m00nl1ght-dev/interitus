package m00nl1ght.interitus.client;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;

import m00nl1ght.interitus.client.gui.GuiDropdown;
import m00nl1ght.interitus.client.gui.GuiDropdown.GuiCheckboxDropdown;
import m00nl1ght.interitus.client.gui.GuiTextBox;
import m00nl1ght.interitus.util.Toolkit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GenTaskClient {
	
	private static final HashMap<String, Class<?>> regClient = new HashMap<String, Class<?>>();
	private static final ArrayList<String> types = new ArrayList<String>();
	private static final ArrayList<Biome> biomeList = new ArrayList<Biome>();
	
	static {
		registerType(DefaultOnGroundTaskC.class);
		registerType(DefaultUnderGroundTaskC.class);
	}
	
	public static void initBiomes() {
		biomeList.clear();
		for (Biome b : Biome.REGISTRY) {
			biomeList.add(b);
		}
	}
	
	protected Biome[] biomes;
	protected int[] dimensions;
	protected TypeDropdown typeDD;
	protected BiomeDropdown biomeDD;
	protected DimDropdown dimDD;
	protected boolean type_dirty = false;
	
	public static NBTTagCompound getTag(GenTaskClient task) {
		NBTTagCompound tag = new NBTTagCompound();
		task.writeToNBT(tag);
		tag.setString("t", task.getType());
		int[] bIDs = new int[task.biomes.length];
		for (int i = 0; i<bIDs.length; i++) {
			bIDs[i] = Biome.getIdForBiome(task.biomes[i]);
		}
		tag.setIntArray("b", bIDs);
		tag.setIntArray("d", task.dimensions);
		return tag;
	}
	
	public static GenTaskClient create(String type, GenTaskClient from) {
		Class<?> clazz = regClient.get(type);
		if (clazz==null) {throw new IllegalStateException("Invalid worldgen task type (client): "+type);}
		GenTaskClient task;
		try {
			task = (GenTaskClient) clazz.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to build worldgen task of type (client) <"+type+">: ", e);
		}
		task.biomes = from.biomes;
		task.dimensions = from.dimensions;
		task.typeDD = from.typeDD;
		task.typeDD.task = task;
		task.biomeDD = from.biomeDD;
		task.biomeDD.task = task;
		task.dimDD = from.dimDD;
		task.dimDD.task = task;
		task.type_dirty = false;
		task.init(Minecraft.getMinecraft().fontRenderer);
		return task;
	}
	
	public static GenTaskClient build(NBTTagCompound tag) {
		String type = tag.getString("t");
		Class<?> clazz = regClient.get(type);
		if (clazz==null) {throw new IllegalStateException("Invalid worldgen task type (client): "+type);}
		GenTaskClient task;
		try {
			task = (GenTaskClient) clazz.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to build worldgen task of type (client) <"+type+">: ", e);
		}
		int[] bIDs = tag.getIntArray("b");
		task.biomes = new Biome[bIDs.length];
		for (int i = 0; i<bIDs.length; i++) {
			task.biomes[i] = Biome.getBiomeForId(bIDs[i]);
		}
		task.dimensions = tag.getIntArray("d");
		task.init(Minecraft.getMinecraft().fontRenderer);
		task.readFromNBT(tag);
		return task;
	}
	
	public static void registerType(Class<?> clazz) {
		GenTaskClient task;
		try {
			task = (GenTaskClient) clazz.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to register worldgen task type (client): "+clazz+" ", e);
		}
		String type = task.getType();
		if (type.isEmpty()) {throw new IllegalStateException("Failed to register worldgen task type (client): "+task+" (name is empty)");}
		if (regClient.containsKey(type)) {throw new IllegalStateException("Failed to register worldgen task type (client) <"+type+"> (type already registered)");}
		regClient.put(type, clazz);
		types.add(type);
	}
	
	@SideOnly(Side.CLIENT)
	public String biomeString(int maxLen) {
		String s = biomes.length>0?"Biomes: ":"No biomes selected";
		for (int i = 0; i<biomes.length; i++) {
			if (i>0) {s+=", ";}
			s+=biomes[i].getBiomeName();
			if (s.length()>=maxLen) {
				s=s.substring(0, maxLen); s+="...";
				break;
			}
		}
		return s;
	}
	
	@SideOnly(Side.CLIENT)
	public String dimString() {
		String s = "dim [";
		for (int i = 0; i<dimensions.length; i++) {
			if (i>0) {s+=", ";}
			s+=dimensions[i];
		}
		return s+"]";
	}
	
	public GenTaskClient init(FontRenderer renderer) {
		if (dimensions==null) dimensions = new int[] {0};
		if (biomes==null) biomes = new Biome[] {};
		if (typeDD==null) {
			typeDD = new TypeDropdown(this, renderer, 100, 20, 50);
			typeDD.setText(getType());
		}
		if (biomeDD==null) {
			biomeDD = new BiomeDropdown(this, renderer, 339, 20, 200);
			biomeDD.setText(biomeString(60));
		}
		if (dimDD==null) {
			dimDD = new DimDropdown(this, renderer, 100, 20, 50);
			dimDD.setText(dimString());
		}
		return this;
	}
	
	public GenTaskClient drawGui(int x, int y, int mX, int mY, boolean clicked, FontRenderer fontRenderer) {
		biomeDD.draw(x+13, y+30, mX, mY, clicked);
		dimDD.draw(x+233, y+5, mX, mY, clicked);
		typeDD.draw(x+13, y+5, mX, mY, clicked);
		if (type_dirty) {
			type_dirty = false;
			return create(typeDD.getText(), this);
		}
		return this;
	}
	
	public void updateGui() {
		//NOOP
	}
	
	public void keyTyped(char typedChar, int keyCode) {
		//NOOP
	}
	
	protected abstract void readFromNBT(NBTTagCompound tag);
	
	protected abstract void writeToNBT(NBTTagCompound tag);
	
	public abstract String getType();
	
	private static class TypeDropdown extends GuiDropdown {
		
		protected GenTaskClient task;

		public TypeDropdown(GenTaskClient task, FontRenderer renderer, int w, int h, int listH) {
			super(renderer, w, h, listH); this.task=task;
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
			if (!org.equals(this.text)) {task.type_dirty = true;}
		}
		
	}
	
	private static class BiomeDropdown extends GuiCheckboxDropdown {
		
		private boolean[] states = new boolean[biomeList.size()];
		protected GenTaskClient task;

		public BiomeDropdown(GenTaskClient task, FontRenderer renderer, int w, int h, int listH) {
			super(renderer, w, h, listH); this.task=task;
			for (Biome b : task.biomes) {
				states[biomeList.indexOf(b)]=true;
			}
		}

		@Override
		protected int getElementCount() {
			return states.length;
		}

		@Override
		protected String getElement(int id) {
			return biomeList.get(id).getBiomeName();
		}
		
		@Override
		protected void slotClicked(int id) {
			states[id] = !states[id];
			int s = 0;
			for (Boolean b : states) {if (b) s++;}
			task.biomes = new Biome[s];
			s = 0;
			for (int i = 0; i < states.length; i++) {
				if (states[i]) {task.biomes[s]=biomeList.get(i); s++;}
			}
			this.setText(task.biomeString(60));
		}

		@Override
		protected boolean getElementState(int id) {
			return states[id];
		}
		
	}
	
	private static class DimDropdown extends GuiCheckboxDropdown {
		
		private boolean[] states = new boolean[DimensionType.values().length];
		protected GenTaskClient task;

		public DimDropdown(GenTaskClient task, FontRenderer renderer, int w, int h, int listH) {
			super(renderer, w, h, listH); this.task=task;
			for (int i=0; i<DimensionType.values().length; i++) {
				for (int n : task.dimensions) {
					if (n==DimensionType.values()[i].getId()) {
						states[i]=true;
					}
				}
			}
		}

		@Override
		protected int getElementCount() {
			return states.length;
		}

		@Override
		protected String getElement(int id) {
			return DimensionType.values()[id].getName();
		}
		
		@Override
		protected void slotClicked(int id) {
			states[id] = !states[id];
			int s = 0;
			for (Boolean b : states) {if (b) s++;}
			task.dimensions = new int[s];
			s = 0;
			for (int i = 0; i < states.length; i++) {
				if (states[i]) {task.dimensions[s]=DimensionType.values()[i].getId(); s++;}
			}
			this.setText(task.dimString());
		}

		@Override
		protected boolean getElementState(int id) {
			return states[id];
		}
		
	}
	
	public static class DefaultOnGroundTaskC extends GenTaskClient {

		protected GuiTextBox chanceEdit, distEdit, hBedit, hRedit;
		
		@Override
		public GenTaskClient init(FontRenderer renderer) {
			super.init(renderer);
			chanceEdit = new GuiTextBox(renderer, 50, 20);
			chanceEdit.setValidator(Toolkit.FLOAT_VALIDATOR);
			chanceEdit.setText("0.3");
			distEdit = new GuiTextBox(renderer, 50, 20);
			distEdit.setValidator(Toolkit.INT_VALIDATOR);
			distEdit.setText("32");
			hBedit = new GuiTextBox(renderer, 50, 20);
			hBedit.setValidator(Toolkit.INT_VALIDATOR);
			hBedit.setText("3");
			hRedit = new GuiTextBox(renderer, 50, 20);
			hRedit.setValidator(Toolkit.INT_VALIDATOR);
			hRedit.setText("5");
			return this;
		}

		@Override
		public void writeToNBT(NBTTagCompound tag) {
			Float chance = Floats.tryParse(this.chanceEdit.getText());
			Integer dist = Ints.tryParse(this.distEdit.getText());
			Integer hb = Ints.tryParse(this.hBedit.getText());
			Integer hr = Ints.tryParse(this.hRedit.getText());
			tag.setFloat("c", chance==null?0F:chance);
			tag.setInteger("mD", dist==null?0:dist);
			tag.setInteger("hB", hb==null?0:hb);
			tag.setInteger("hR", hr==null?0:hr);
		}

		@Override
		public void readFromNBT(NBTTagCompound tag) {
			this.chanceEdit.setText(""+tag.getFloat("c"));
			this.distEdit.setText(""+tag.getInteger("mD"));
			this.hBedit.setText(""+tag.getInteger("hB"));
			this.hRedit.setText(""+tag.getInteger("hR"));
		}

		@Override
		public String getType() {
			return "onGround";
		}

		@Override
		public GenTaskClient drawGui(int x, int y, int mX, int mY, boolean clicked, FontRenderer fontRenderer) {
			fontRenderer.drawStringWithShadow("chance", x + 20, y + 60, 14737632);
			chanceEdit.drawTextBox(x+105, y+55, mX, mY, clicked);
			fontRenderer.drawStringWithShadow("min distance", x + 165, y + 60, 14737632);
			distEdit.drawTextBox(x+255, y+55, mX, mY, clicked);
			fontRenderer.drawStringWithShadow("base height", x + 20, y + 85, 14737632);
			hBedit.drawTextBox(x+105, y+80, mX, mY, clicked);
			fontRenderer.drawStringWithShadow("height variation", x + 165, y + 85, 14737632);
			hRedit.drawTextBox(x+255, y+80, mX, mY, clicked);
			return super.drawGui(x, y, mX, mY, clicked, fontRenderer);
		}

		@Override
		public void updateGui() {
			chanceEdit.updateCursorCounter();
			distEdit.updateCursorCounter();
			hBedit.updateCursorCounter();
			hRedit.updateCursorCounter();
		}

		@Override
		public void keyTyped(char typedChar, int keyCode) {
			chanceEdit.textboxKeyTyped(typedChar, keyCode);
			distEdit.textboxKeyTyped(typedChar, keyCode);
			hBedit.textboxKeyTyped(typedChar, keyCode);
			hRedit.textboxKeyTyped(typedChar, keyCode);
		}

	}
	
	public static class DefaultUnderGroundTaskC extends DefaultOnGroundTaskC {

		@Override
		public String getType() {
			return "underGround";
		}

	}


}
