package m00nl1ght.interitus.client.gui;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure;
import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure.LootEntryPrimer;
import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure.LootGenPrimer;
import m00nl1ght.interitus.network.CDefaultPackage;
import m00nl1ght.interitus.structures.StructurePackInfo;

public class GuiEditLootEntry extends GuiScreen {
	
	private final DecimalFormat decimalFormat = new DecimalFormat("0.0###");
	protected final TileEntityAdvStructure tileStructure;
	private GuiButton closeButton;
	private LootGenList list;
	private final GuiStructureData parent;
	private final LootEntryPrimer primer;
	private final StructurePackInfo packInfo;
	
	public GuiEditLootEntry(TileEntityAdvStructure te, GuiStructureData parent, LootEntryPrimer primer, StructurePackInfo packInfo) {
        this.tileStructure = te; this.parent = parent; this.primer = primer; this.packInfo = packInfo;
        this.decimalFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
    }
	
	@Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.tileStructure.setAcceptUpdates(false);
        this.buttonList.clear();
        this.closeButton = this.addButton(new GuiButton(0, this.width / 2 - 75, 305, 150, 20, parent==null?"Close":"Back"));
        this.list = new LootGenList(this.width / 2 - 180, 50, 360, 250, 20);
        this.list.setHeaderInfo(true, 20);
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
				if (parent!=null) {
					parent.setState(true);
				} else {
					if (!CDefaultPackage.sendStructUpdatePacket(this.tileStructure, 0)) {return;}
				}
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
		
		this.drawCenteredString(this.fontRenderer, parent==null?"Create Loot Entry":"Edit Loot Entry", this.width / 2, 10, 16777215);
		
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
	
	private class LootGenList extends GuiList {

		public LootGenList(int x, int y, int w, int h, int entryH) {
			super(mc, x, y, w, h, entryH);
		}

		@Override
		protected int getElementCount() {
			return primer.gens().size();
		}
		
		@Override
		protected void drawHeader(int entryRight, int relativeY, Tessellator tess) {
			int x = entryRight-this.w;
			getFontRenderer().drawString("Loot List", x+15, relativeY+5, 16777215);
			getFontRenderer().drawString("Amount", x+181, relativeY+5, 16777215);
			if (this.drawButton(mc, x+343, relativeY-1, 17, 17, primer.gens().size()<16, true, "+")) {
				LootGenPrimer p = new LootGenPrimer(1, "undefined");
				primer.gens().add(p);
				mc.displayGuiScreen(new GuiEditLootGen(tileStructure, GuiEditLootEntry.this, p, packInfo));
			}
		}

		@Override
		protected boolean drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
			int x = entryRight-this.w;
			LootGenPrimer gen = primer.gens().get(slotIdx);
			getFontRenderer().drawString(gen.list(), x+15, slotTop+5, 16777215);
			this.drawCenteredString(fontRenderer, ""+gen.amount(), x+210, slotTop+5, 16777215);
			if (this.drawButton(mc, x+120, slotTop-1, 17, 17, true, this.isHovering, "...")) {
				mc.displayGuiScreen(new GuiEditLootGen(tileStructure, GuiEditLootEntry.this, gen, packInfo));
			}
			if (this.drawButton(mc, x+180, slotTop-1, 17, 17, gen.amount()>0, this.isHovering, "-")) {
				gen.setAmount(gen.amount()-1);
			}
			if (this.drawButton(mc, x+220, slotTop-1, 17, 17, gen.amount()<64, this.isHovering, "+")) {
				gen.setAmount(gen.amount()+1);
			}
			if (this.drawButton(mc, x+343, slotTop-1, 17, 17, true, this.isHovering, "X")) {
				return primer.gens().remove(slotIdx)!=null;
			}
			return false;
		}
		
	}

}
