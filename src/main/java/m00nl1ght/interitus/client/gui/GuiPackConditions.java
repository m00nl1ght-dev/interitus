package m00nl1ght.interitus.client.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import m00nl1ght.interitus.network.ServerPackage;
import m00nl1ght.interitus.structures.StructurePackInfo;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;

public class GuiPackConditions extends GuiEditor {

	private GuiButton closeButton, addButton;
	private CondTypeList list;
	private final GuiScreen parent;
	private String active;
	private boolean tbClosed;
	
	public GuiPackConditions(GuiScreen parent) {
        super(GuiEditor.PACK_EDITOR); this.parent = parent;
    }
	
	public GuiPackConditions(String active) {
		super(GuiEditor.PACK_EDITOR); 
        this.parent = null;
        this.active = active;
    }
	
	@Override
    public void initGui() {
		this.tbClosed=false;
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.closeButton = this.addButton(new GuiButton(0, this.width / 2 - 75, 305, 150, 20, parent==null?"Done":"Back"));
        this.addButton = this.addButton(new GuiButton(1, this.width / 2 + 157, 33, 17, 17, "+"));
        this.list = new CondTypeList(this.width / 2 - 180, 50, 360, 250, 20);
        this.list.setHeaderInfo(true, 20);
        if (active != null) {
        	int idx = StructurePackInfo.condtypes.indexOf(active);
        	if (idx>=0) {
        		this.list.selectedIndex = idx;
        	}
        }
	}
    
    @Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.enabled && !this.tbClosed ) {
			if (button.id == 0) {
//				if (this.list.selectedIndex>=0) { TODO use this screen also for cond type selection (tool item)?
//					this.active = StructurePackInfo.condtypes.get(this.list.selectedIndex);
//					ServerPackage.sendSetToolCond(this.active);
//				}
				this.transition(parent);
			} else if (button.id == 1) {
				if (this.list.selectedIndex>=0) this.active = StructurePackInfo.condtypes.get(this.list.selectedIndex);
				if (ServerPackage.sendOpenCondType("")) {
					tbClosed = true; expectTransition();
				}
			}
		}
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == 1 && !this.tbClosed) {
			this.actionPerformed(this.closeButton);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		
		if (this.tbClosed) {
			return;
		}
		
		this.drawCenteredString(this.fontRenderer, parent==null?"Select Condition Type":("Condition Types of <"+StructurePackInfo.active.name+">"), this.width / 2, 10, 16777215);
		
		this.list.drawScreen(mouseX, mouseY, Mouse.isButtonDown(0));
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	public FontRenderer getFontRenderer() {
		return this.fontRenderer;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	private class CondTypeList extends GuiList {

		public CondTypeList(int x, int y, int w, int h, int entryH) {
			super(mc, x, y, w, h, entryH);
		}

		@Override
		protected int getElementCount() {
			return StructurePackInfo.condtypes.size();
		}
		
		@Override
		protected void drawHeader(int entryRight, int relativeY, Tessellator tess) {
			int x = entryRight-this.w;
			getFontRenderer().drawString("Condition Type", x+15, relativeY+5, 16777215);
		}

		@Override
		protected boolean drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
			int x = entryRight-this.w;
			String type = StructurePackInfo.condtypes.get(slotIdx);
			getFontRenderer().drawString(type, x+15, slotTop+5, 16777215);
			if (this.drawButton(mc, x+249, slotTop-1, 17, 17, true, this.isHovering, "...")) {
				if (this.selectedIndex>=0) active = StructurePackInfo.condtypes.get(this.selectedIndex);
				if (ServerPackage.sendOpenCondType(type)) {
					tbClosed = true; expectTransition();
				}
			}
			if (this.drawButton(mc, x+343, slotTop-1, 17, 17, !StructurePackInfo.active.read_only, this.isHovering, "X")) {
				if (!ServerPackage.sendPackAction(9, type, "")) {return false;}
				this.selectedIndex = -1; active = null;
				return StructurePackInfo.condtypes.remove(slotIdx)!=null;
			}
			return false;
		}
		
	}
	
}
