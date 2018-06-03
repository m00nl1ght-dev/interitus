package m00nl1ght.interitus.client.gui;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.input.Keyboard;

import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure;
import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure.LootGenPrimer;

public class GuiEditLootGen extends GuiScreen {
	
	private final DecimalFormat decimalFormat = new DecimalFormat("0.0###");
	protected final TileEntityAdvStructure tileStructure;
	private GuiButton closeButton;
	private LootListList list;
	private final GuiEditLootEntry parent;
	private final LootGenPrimer primer;
	private final ArrayList<String> lootlists = new ArrayList<String>();
	
	public GuiEditLootGen(TileEntityAdvStructure te, GuiEditLootEntry parent, LootGenPrimer primer) {
        this.tileStructure = te; this.parent = parent; this.primer = primer;
        this.decimalFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        lootlists.add("test"); // TODO actual lists
        lootlists.add("normal");
        lootlists.add("common");
        lootlists.add("rare");
        lootlists.add("epic");
        for (int i=0; i<lootlists.size(); i++) {
        	if (primer.list().equals(lootlists.get(i))) {
        		this.list.selectedIndex=i; break;
        	}
        }
        if (this.list.selectedIndex<0 && !lootlists.isEmpty()) {this.list.selectedIndex=0;}
    }
	
	@Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.tileStructure.setAcceptUpdates(false);
        this.buttonList.clear();
        this.closeButton = this.addButton(new GuiButton(0, this.width / 2 - 75, 305, 150, 20, parent==null?"Close":"Back"));
        this.list = new LootListList(this.width / 2 - 90, 50, 180, 250, 20);
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
				if (this.list.isSelectionValid()) {
					this.primer.setList(lootlists.get(this.list.selectedIndex));
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
		
		this.drawCenteredString(this.fontRenderer, "Choose Loot List", this.width / 2, 10, 16777215);
		
		this.list.drawScreen(mouseX, mouseY, partialTicks);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	public FontRenderer getFontRenderer() {
		return this.fontRenderer;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	private class LootListList extends GuiList {

		public LootListList(int x, int y, int w, int h, int entryH) {
			super(mc, x, y, w, h, entryH);
		}

		@Override
		protected int getElementCount() {
			return lootlists.size();
		}

		@Override
		protected boolean drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
			int x = entryRight-this.w;
			String name = lootlists.get(slotIdx);
			getFontRenderer().drawString(name, x+15, slotTop+5, 16777215);
			return false;
		}
		
	}

}
