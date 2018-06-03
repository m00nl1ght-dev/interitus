package m00nl1ght.interitus.client.gui;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.lwjgl.input.Keyboard;

import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure;
import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure.LootEntryPrimer;
import m00nl1ght.interitus.network.CDefaultPackage;
import m00nl1ght.interitus.structures.BlockRegionStorage.Condition;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.BlockPos;

public class GuiStructureData extends GuiScreen {

	private final DecimalFormat decimalFormat = new DecimalFormat("0.0###");
	protected final TileEntityAdvStructure tileStructure;
	private GuiButton closeButton, condButton, lootButton;
	private ConditionList listCond;
	private LootList listLoot;
	private final GuiScreen parent;
	private boolean state = false;

	public GuiStructureData(TileEntityAdvStructure te, GuiScreen parent) {
		this.tileStructure = te;
		this.parent = parent;
		this.decimalFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
	}

	@Override
	public void updateScreen() {

	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.tileStructure.setAcceptUpdates(false);
		this.buttonList.clear();
		this.condButton = this.addButton(new GuiButton(3, this.width / 2 - 150, 25, 150, 20, "Conditions"));
		this.lootButton = this.addButton(new GuiButton(4, this.width / 2, 25, 150, 20, "Loot Entries"));
		this.closeButton = this.addButton(new GuiButton(0, this.width / 2 - 75, 305, 150, 20, parent == null ? "Close" : "Back"));
		this.listCond = new ConditionList(this.width / 2 - 180, 50, 360, 250, 20);
		listCond.setHeaderInfo(true, 20);
		this.listLoot = new LootList(this.width / 2 - 180, 50, 360, 250, 20);
		listLoot.setHeaderInfo(true, 20);
		this.setState(this.state);
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
		this.tileStructure.setAcceptUpdates(true);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.enabled) {
			if (button.id == 0) {
				if (this.parent==null) {
					if (!CDefaultPackage.sendStructUpdatePacket(this.tileStructure, 0)) {return;}
				}
				this.mc.displayGuiScreen(parent);
			} else if (button.id == 3) {
				this.setState(false);
			} else if (button.id == 4) {
				this.setState(true);
			}
		}
	}

	public void setState(boolean state) {
		this.state = state;
		if (state) {
			this.condButton.enabled = true;
			this.lootButton.enabled = false;
		} else {
			this.condButton.enabled = false;
			this.lootButton.enabled = true;
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

		this.drawCenteredString(this.fontRenderer, "Strcuture Data Overwiew", this.width / 2, 10, 16777215);

		if (state) {
			this.listLoot.drawScreen(mouseX, mouseY, partialTicks);
		} else {
			this.listCond.drawScreen(mouseX, mouseY, partialTicks);
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	public FontRenderer getFontRenderer() {
		return this.fontRenderer;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	private class ConditionList extends GuiList {

		public ConditionList(int x, int y, int w, int h, int entryH) {
			super(mc, x, y, w, h, entryH);
		}

		@Override
		protected int getElementCount() {
			return tileStructure.getConditions().size();
		}

		@Override
		protected void drawHeader(int entryRight, int relativeY, Tessellator tess) {
			int x = entryRight - this.w;
			getFontRenderer().drawString("Type", x + 15, relativeY + 5, 16777215);
			getFontRenderer().drawString("Position in Structure", x + 100, relativeY + 5, 16777215);
			getFontRenderer().drawString("Position in World", x + 220, relativeY + 5, 16777215);
		}

		@Override
		protected boolean drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
			int x = entryRight - this.w;
			Condition cond = tileStructure.getConditions().get(slotIdx);
			BlockPos pos = cond.pos.subtract(tileStructure.getPos().add(tileStructure.getPosition()));
			getFontRenderer().drawString(cond.type.name(), x + 15, slotTop + 5, 16777215);
			getFontRenderer().drawString("@ x " + pos.getX() + " y " + pos.getY() + " z " + pos.getZ(), x + 100, slotTop + 5, 16777215);
			getFontRenderer().drawString("@ x " + cond.pos.getX() + " y " + cond.pos.getY() + " z " + cond.pos.getZ(), x + 220, slotTop + 5, 16777215);
			if (this.drawButton(mc, x+343, slotTop-1, 17, 17, true, "X")) {
				return tileStructure.getConditions().remove(slotIdx)!=null;
			}
			return false;
		}

	}

	private class LootList extends GuiList {

		public LootList(int x, int y, int w, int h, int entryH) {
			super(mc, x, y, w, h, entryH);
		}

		@Override
		protected int getElementCount() {
			return tileStructure.getLoot().size();
		}

		@Override
		protected void drawHeader(int entryRight, int relativeY, Tessellator tess) {
			int x = entryRight - this.w;
			getFontRenderer().drawString("Position in Structure", x + 15, relativeY + 5, 16777215);
			getFontRenderer().drawString("Position in World", x + 140, relativeY + 5, 16777215);
			getFontRenderer().drawString("Items", x + 280, relativeY + 5, 16777215);
		}

		@Override
		protected boolean drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
			int x = entryRight - this.w;
			LootEntryPrimer entry = tileStructure.getLoot().get(slotIdx);
			BlockPos pos = entry.pos.subtract(tileStructure.getPos().add(tileStructure.getPosition()));
			getFontRenderer().drawString("@ x " + pos.getX() + " y " + pos.getY() + " z " + pos.getZ(), x + 15, slotTop + 5, 16777215);
			getFontRenderer().drawString("@ x " + entry.pos.getX() + " y " + entry.pos.getY() + " z " + entry.pos.getZ(), x + 140, slotTop + 5, 16777215);
			getFontRenderer().drawString(entry.gens().size() + " gens", x + 280, slotTop + 5, 16777215);
			if (this.drawButton(mc, x+325, slotTop-1, 17, 17, true, "...")) {
				mc.displayGuiScreen(new GuiEditLootEntry(tileStructure, GuiStructureData.this, entry));
			}
			if (this.drawButton(mc, x+343, slotTop-1, 17, 17, true, "X")) {
				return tileStructure.getLoot().remove(slotIdx)!=null;
			}
			return false;
		}

	}

}
