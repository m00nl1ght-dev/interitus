package m00nl1ght.interitus.client.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import m00nl1ght.interitus.network.CDefaultPackage;
import m00nl1ght.interitus.structures.StructurePackInfo;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;

public class GuiPackLootLists extends GuiScreen {
	
	private GuiButton closeButton;
	private LootListsList list;
	private final GuiScreen parent;
	
	public GuiPackLootLists(GuiScreen parent) {
        this.parent = parent;
    }
	
	@Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.closeButton = this.addButton(new GuiButton(0, this.width / 2 - 75, 305, 150, 20, "Back"));
        this.list = new LootListsList(this.width / 2 - 180, 50, 360, 250, 20);
        this.list.setHeaderInfo(true, 20);
	}
    
    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
    
    @Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.enabled) {
			if (button.id == 0) {
				this.mc.displayGuiScreen(parent);
			}
		}
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == 1) {
			this.actionPerformed(this.closeButton);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		
		this.drawCenteredString(this.fontRenderer, "Loot Lists of <"+StructurePackInfo.active.name+">", this.width / 2, 10, 16777215);
		
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
	
	private class LootListsList extends GuiList {

		public LootListsList(int x, int y, int w, int h, int entryH) {
			super(mc, x, y, w, h, entryH);
		}

		@Override
		protected int getElementCount() {
			return StructurePackInfo.lootlists.size();
		}
		
		@Override
		protected void drawHeader(int entryRight, int relativeY, Tessellator tess) {
			int x = entryRight-this.w;
			getFontRenderer().drawString("Loot List", x+15, relativeY+5, 16777215);
		}

		@Override
		protected boolean drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
			int x = entryRight-this.w;
			getFontRenderer().drawString(StructurePackInfo.lootlists.get(slotIdx), x+15, slotTop+5, 16777215);
			if (this.drawButton(mc, x+300, slotTop-1, 17, 17, false, this.isHovering, "...")) {
				//TODO loot list editor
			}
			if (this.drawButton(mc, x+343, slotTop-1, 17, 17, !StructurePackInfo.active.read_only, this.isHovering, "X")) {
				if (!CDefaultPackage.packGuiAction(8, StructurePackInfo.lootlists.get(slotIdx), "")) {return false;}
				return StructurePackInfo.lootlists.remove(slotIdx)!=null;
			}
			return false;
		}
		
	}

}
