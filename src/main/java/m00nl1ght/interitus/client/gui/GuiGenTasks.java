package m00nl1ght.interitus.client.gui;

import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import m00nl1ght.interitus.client.GenTaskClient;
import m00nl1ght.interitus.network.CDefaultPackage;
import m00nl1ght.interitus.structures.StructurePackInfo.StructureInfo;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class GuiGenTasks extends GuiScreen {
	
	private GuiButton closeButton, addButton;
	private GenTaskList list;
	private final GuiPackStructures parent;
	private final String struct;
	private final ArrayList<GenTaskClient> tasks = new ArrayList<GenTaskClient>();
	private final boolean read_only;
	
	public GuiGenTasks(GuiPackStructures parent, String struct, NBTTagCompound nbt) {
        this.parent = parent; this.read_only = parent.packInfo.active.read_only; this.struct = struct;
        NBTTagList list = nbt.getTagList("u", 10);
        for (int i = 0; i < list.tagCount(); i++) {
        	tasks.add(GenTaskClient.build(list.getCompoundTagAt(i)));
        }
    }
	
	@Override
	public void updateScreen() {
		for (GenTaskClient task : tasks) {
			task.updateGui();
		}
	}
	
	@Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.closeButton = this.addButton(new GuiButton(0, this.width / 2 - 75, 305, 150, 20, "Back"));
        this.addButton = this.addButton(new GuiButton(1, this.width / 2 + 157, 33, 17, 17, "+"));
        this.list = new GenTaskList(this.width / 2 - 180, 50, 360, 250, 110);
	}
    
    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        GuiDropdown.close();
    }
    
    @Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.enabled && !GuiDropdown.isOpen()) {
			if (button.id == 0) {
				if (read_only || CDefaultPackage.updateGenTasks(getUpdateTag(), struct)) {
					for (StructureInfo str : parent.packInfo.structures) {
						if (str.name.equals(struct)) {str.genTasks=tasks.size(); break;}
					}
					this.mc.displayGuiScreen(parent);
				}
			} else if (button.id == 1) {
				tasks.add(new GenTaskClient.DefaultOnGroundTaskC().init(getFontRenderer()));
			}
		}
	}
    
    private NBTTagCompound getUpdateTag() {
    	NBTTagCompound tag = new NBTTagCompound();
    	NBTTagList list = new NBTTagList();
    	for (GenTaskClient task : tasks) {
    		list.appendTag(GenTaskClient.getTag(task));
    	}
    	tag.setTag("u", list);
    	return tag;
    }
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		for (GenTaskClient task : tasks) {
			task.keyTyped(typedChar, keyCode);
		}
		if (keyCode == 1) {
			this.actionPerformed(this.closeButton);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		boolean mBtn = Mouse.isButtonDown(0);
		
		this.drawCenteredString(this.fontRenderer, "Worldgen tasks for <"+struct+">", this.width / 2, 10, 16777215);
		
		this.list.drawScreen(mouseX, mouseY, mBtn, partialTicks);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
		GuiDropdown.drawDropdown(mouseX, mouseY, mBtn);
	}
	
	public FontRenderer getFontRenderer() {
		return this.fontRenderer;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	private class GenTaskList extends GuiList {

		public GenTaskList(int x, int y, int w, int h, int entryH) {
			super(mc, x, y, w, h, entryH);
			this.setHighlightSelection(false);
		}

		@Override
		protected int getElementCount() {
			return tasks.size();
		}

		@Override
		protected boolean drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
			int x = entryRight-this.w;
			list.drawRect(x+5, slotTop-1, list.w-6, 108, 0.5F, 0.5F, 0.5F, 0.5F, 0.3F, 0.3F, 0.3F, 0.5F);
			GenTaskClient task = tasks.get(slotIdx);
			GenTaskClient task1 = task.drawGui(x, slotTop, mouseX, mouseY, mDown && isHovering && !GuiDropdown.isOpen(), getFontRenderer());
			if (task!=task1) {tasks.set(slotIdx, task1);}
			if (this.drawButton(mc, x+337, slotTop+5, 17, 20, !read_only, this.isHovering && !GuiDropdown.isOpen(), "X")) {
				return tasks.remove(slotIdx)!=null;
			}
			return false;
		}
		
	}

}
