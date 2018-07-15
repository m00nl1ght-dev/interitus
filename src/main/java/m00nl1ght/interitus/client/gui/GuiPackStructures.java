package m00nl1ght.interitus.client.gui;

import java.io.IOException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import m00nl1ght.interitus.network.CDefaultPackage;
import m00nl1ght.interitus.structures.StructurePackInfo;
import m00nl1ght.interitus.structures.StructurePackInfo.StructureInfo;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;

public class GuiPackStructures extends GuiScreen{
	
	private GuiButton closeButton;
	private StructureList list;
	private final GuiScreen parent;
	protected final StructurePackInfo packInfo;
	private boolean tbClosed;
	
	public GuiPackStructures(GuiScreen parent, StructurePackInfo packInfo) {
        this.parent = parent; this.packInfo = packInfo;
    }
	
	@Override
    public void initGui() {
		this.tbClosed=false;
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.closeButton = this.addButton(new GuiButton(0, this.width / 2 - 75, 305, 150, 20, "Back"));
        this.list = new StructureList(this.width / 2 - 180, 50, 360, 250, 20);
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
		
		this.drawCenteredString(this.fontRenderer, "Structure List of <"+packInfo.active.name+">", this.width / 2, 10, 16777215);
		
		this.list.drawScreen(mouseX, mouseY, Mouse.isButtonDown(0), partialTicks);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	public FontRenderer getFontRenderer() {
		return this.fontRenderer;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	private class StructureList extends GuiList {

		public StructureList(int x, int y, int w, int h, int entryH) {
			super(mc, x, y, w, h, entryH);
		}

		@Override
		protected int getElementCount() {
			return packInfo.structures.size();
		}
		
		@Override
		protected void drawHeader(int entryRight, int relativeY, Tessellator tess) {
			int x = entryRight-this.w;
			getFontRenderer().drawString("Structure", x+15, relativeY+5, 16777215);
			getFontRenderer().drawString("Size", x+150, relativeY+5, 16777215);
			getFontRenderer().drawString("genTasks", x+250, relativeY+5, 16777215);
		}

		@Override
		protected boolean drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
			int x = entryRight-this.w;
			StructureInfo str = packInfo.structures.get(slotIdx);
			getFontRenderer().drawString(str.name, x+15, slotTop+5, 16777215);
			this.drawString(fontRenderer, str.size[0]+"x"+str.size[1]+"x"+str.size[2], x+150, slotTop+5, 16777215);
			this.drawString(fontRenderer, str.genTasks+" tasks", x+270, slotTop+5, 16777215);
			if (this.drawButton(mc, x+249, slotTop-1, 17, 17, true, this.isHovering, "...")) {
				if (CDefaultPackage.openGenTasks(str.name)) {
					tbClosed = true;
				}
			}
			if (this.drawButton(mc, x+343, slotTop-1, 17, 17, !packInfo.active.read_only, this.isHovering, "X")) {
				if (!CDefaultPackage.packGuiAction(5, str.name, "")) {return false;}
				return packInfo.structures.remove(slotIdx)!=null;
			}
			return false;
		}
		
	}

}
