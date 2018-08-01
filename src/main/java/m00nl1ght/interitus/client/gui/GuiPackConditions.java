package m00nl1ght.interitus.client.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import m00nl1ght.interitus.client.ConditionTypeClient;
import m00nl1ght.interitus.network.CDefaultPackage;
import m00nl1ght.interitus.structures.StructurePackInfo;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;

public class GuiPackConditions extends GuiScreen {

	private GuiButton closeButton, addButton;
	private CondTypeList list;
	private final GuiScreen parent;
	private boolean tbClosed;
	
	public GuiPackConditions(GuiScreen parent) {
        this.parent = parent;
    }
	
	@Override
    public void initGui() {
		this.tbClosed=false;
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.closeButton = this.addButton(new GuiButton(0, this.width / 2 - 75, 305, 150, 20, "Back"));
        this.addButton = this.addButton(new GuiButton(1, this.width / 2 + 157, 33, 17, 17, "+"));
        this.list = new CondTypeList(this.width / 2 - 180, 50, 360, 250, 20);
        this.list.setHeaderInfo(true, 20);
	}
    
    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
    
    @Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.enabled && !this.tbClosed ) {
			if (button.id == 0) {
				this.mc.displayGuiScreen(parent);
			} else if (button.id == 1) {
				this.mc.displayGuiScreen(new GuiEditCondType(this, new ConditionTypeClient.ConditionMaterialSetClient()));
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
		
		this.drawCenteredString(this.fontRenderer, "Condition Types of <"+StructurePackInfo.active.name+">", this.width / 2, 10, 16777215);
		
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
				if (CDefaultPackage.openCondType(type)) {
					tbClosed = true;
				}
			}
			if (this.drawButton(mc, x+343, slotTop-1, 17, 17, !StructurePackInfo.active.read_only, this.isHovering, "X")) {
				if (!CDefaultPackage.packGuiAction(9, type, "")) {return false;}
				return StructurePackInfo.condtypes.remove(slotIdx)!=null;
			}
			return false;
		}
		
	}
	
}
