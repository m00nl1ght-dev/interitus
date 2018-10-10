package m00nl1ght.interitus.client.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import m00nl1ght.interitus.network.ServerPackage;
import m00nl1ght.interitus.structures.StructurePackInfo;
import m00nl1ght.interitus.structures.StructurePackInfo.PackInfo;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;

public class GuiStructurePacks extends GuiEditor {
	
	public GuiStructurePacks() {
		super(GuiEditor.PACK_EDITOR);
	}

	private GuiButton closeButton, addButton;
	private PackList list;
	private boolean saved = false;
	private PackInfo toBeDeleted;

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		this.closeButton = this.addButton(new GuiButton(0, this.width / 2 - 75, 305, 150, 20, "Close"));
		this.addButton = this.addButton(new GuiButton(1, this.width / 2 + 157, 83, 17, 17, "+"));
		this.list = new PackList(this.width / 2 - 180, 100, 360, 200, 38);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.enabled) {
			if (button.id == 0) {
				this.close();
			} else if (button.id == 1) {
				this.transition(new GuiCreatePack(null, this));
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

		this.drawCenteredString(this.fontRenderer, "Structure Packs", this.width / 2, 10, 16777215);
		this.drawString(this.fontRenderer, "Active Pack", this.width / 2 - 177, 24, 16777215);
		this.drawString(this.fontRenderer, "Available Packs", this.width / 2 - 177, 89, 16777215);
		this.list.drawScreen(mouseX, mouseY, Mouse.isButtonDown(0));
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	private boolean drawActivePack(PackInfo pack, int x, int y) {
		list.drawRect(x, y, list.w-7, 36, 0.5F, 0.8F, 0.5F, 0.5F, 0.3F, 0.5F, 0.3F, 0.5F);
		getFontRenderer().drawString(pack.name, x + 15 - 7, y + 5, 16777215);
		getFontRenderer().drawString("v"+pack.version, x + 150 - 7, y + 5, 16777215);
		getFontRenderer().drawString(pack.description, x + 15 - 7, y + 20, 16777215);
		if (this.list.drawButton(mc, x+310-7, y-1, 50, 19, true, true, "Reload")) {
			if (ServerPackage.sendPackAction(1, pack.name, "")) {
				this.closeSilent();
				return false;
			}
		}
		if (this.list.drawButton(mc, x+260-7, y-1, 50, 19, !pack.read_only && !saved, true, "Save")) {
			if (ServerPackage.sendPackAction(4, "", "")) {
				this.saved=true;
				this.closeSilent();
				return false;
			}
		}
		if (this.list.drawButton(mc, x+260-7, y+18, 50, 18, true, true, "Edit")) {
			this.transition(new GuiEditStructurePack(this));
			return false;
		}
		if (this.list.drawButton(mc, x+310-7, y+18, 50, 18, true, true, "Copy")) {
			this.transition(new GuiCreatePack(pack, this));
			return false;
		}
		return false;
	}
	
	private boolean drawPack(PackInfo pack, int x, int y, boolean isHovering) {
		boolean isDefault = pack.name.equals("Default");
		list.drawRect(x+5, y-1, list.w-6, 36, 0.5F, 0.5F, 0.5F, 0.5F, 0.3F, 0.3F, 0.3F, 0.5F);
		getFontRenderer().drawString(pack.name, x + 15, y + 5, 16777215);
		getFontRenderer().drawString("v"+pack.version, x + 150, y + 5, 16777215);
		getFontRenderer().drawString(pack.description, x + 15, y + 20, 16777215);
		if (this.list.drawButton(mc, x+310, y-2, 50, 19, true, isHovering, "Load")) {
			if (ServerPackage.sendPackAction(1, pack.name, "")) {
				this.closeSilent();
				return false;
			}
		}
		if (this.list.drawButton(mc, x+260, y+17, 50, 18, !isDefault, isHovering, "Delete")) {
			this.toBeDeleted = pack;
			this.transition(new GuiConfirm(this, GuiEditor.PACK_EDITOR, "Do you really want to delete the pack <"+pack.name+">?", "", "Cancel", "Confirm", this::confirmCallback));
		}
		if (this.list.drawButton(mc, x+310, y+17, 50, 18, true, isHovering, "Copy")) {
			this.transition(new GuiCreatePack(pack, this));
			return false;
		}
		return false;
	}
	
	public boolean confirmCallback(int i) {
		if (i==1 && toBeDeleted!=null) {
			if (ServerPackage.sendPackAction(2, toBeDeleted.name, "")) {
				StructurePackInfo.packs.remove(toBeDeleted);
			}
		}
		toBeDeleted = null;
		return true;
	}

	public FontRenderer getFontRenderer() {
		return this.fontRenderer;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	private class PackList extends GuiList {

		public PackList(int x, int y, int w, int h, int entryH) {
			super(mc, x, y, w, h, entryH);
		}

		@Override
		protected int getElementCount() {
			return StructurePackInfo.packs.size();
		}

		@Override
		protected void drawHeader(int entryRight, int relativeY, Tessellator tess) {
			int x = entryRight - this.w;
			getFontRenderer().drawString("Name", x + 15, relativeY + 5, 16777215);
			
		}

		@Override
		protected boolean drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
			int x = entryRight - this.w;
			return drawPack(StructurePackInfo.packs.get(slotIdx), x, slotTop, this.isHovering);
		}

		@Override
		protected void drawExtra() {
			drawActivePack(StructurePackInfo.active, width / 2 - 180, 35);
		}

	}

}
