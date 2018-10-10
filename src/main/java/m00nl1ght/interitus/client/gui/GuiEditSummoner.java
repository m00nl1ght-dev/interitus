package m00nl1ght.interitus.client.gui;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.lwjgl.input.Keyboard;

import m00nl1ght.interitus.block.tileentity.TileEntitySummoner;
import m00nl1ght.interitus.network.ServerPackage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ChatAllowedCharacters;

public class GuiEditSummoner extends GuiScreen {
	
	public static final int[] LEGAL_KEY_CODES = new int[] {203, 205, 14, 211, 199, 207};
    private final TileEntitySummoner tileStructure;
    private GuiTextField nameEdit, pRangeEdit, maxMobEdit, delayBaseEdit, delayRangeEdit, rangeHEdit, rangeVNEdit, rangeVPEdit;
    private GuiButton doneButton,cancelButton;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.0###");

    public GuiEditSummoner(TileEntitySummoner te) {
        this.tileStructure = te;
        this.decimalFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
    }
    
    @Override
    public void updateScreen() {
        this.nameEdit.updateCursorCounter();
        this.pRangeEdit.updateCursorCounter();
        this.maxMobEdit.updateCursorCounter();
        this.delayBaseEdit.updateCursorCounter();
        this.delayRangeEdit.updateCursorCounter();
        this.rangeHEdit.updateCursorCounter();
        this.rangeVNEdit.updateCursorCounter();
        this.rangeVPEdit.updateCursorCounter();
    }
    
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.doneButton = this.addButton(new GuiButton(0, this.width / 2 - 4 - 150, 210, 150, 20, I18n.format("gui.done")));
        this.cancelButton = this.addButton(new GuiButton(1, this.width / 2 + 4, 210, 150, 20, I18n.format("gui.cancel")));
        
        this.nameEdit = new GuiTextField(2, this.fontRenderer, this.width / 2 - 152, 40, 300, 20);
        this.nameEdit.setMaxStringLength(24);
        this.nameEdit.setText(this.tileStructure.getEntityName());
        
        this.pRangeEdit = new GuiTextField(3, this.fontRenderer, this.width / 2 - 152, 80, 80, 20);
        this.pRangeEdit.setMaxStringLength(4);
        this.pRangeEdit.setText(Float.toString(this.tileStructure.getPlayerRange()));

        this.maxMobEdit = new GuiTextField(4, this.fontRenderer, this.width / 2 - 32, 80, 80, 20);
        this.maxMobEdit.setMaxStringLength(4);
        this.maxMobEdit.setText(Integer.toString(this.tileStructure.getMaxMobCountInRange()));
        
        this.delayBaseEdit = new GuiTextField(3, this.fontRenderer, this.width / 2 - 152, 120, 80, 20);
        this.delayBaseEdit.setMaxStringLength(4);
        this.delayBaseEdit.setText(Integer.toString(this.tileStructure.getDelayBase()));

        this.delayRangeEdit = new GuiTextField(4, this.fontRenderer, this.width / 2 - 32, 120, 80, 20);
        this.delayRangeEdit.setMaxStringLength(4);
        this.delayRangeEdit.setText(Integer.toString(this.tileStructure.getDelayRange()));

        this.rangeHEdit = new GuiTextField(6, this.fontRenderer, this.width / 2 - 152, 160, 80, 20);
        this.rangeHEdit.setMaxStringLength(5);
        this.rangeHEdit.setText(Float.toString(this.tileStructure.getRangeH()));

        this.rangeVNEdit = new GuiTextField(7, this.fontRenderer, this.width / 2 - 72, 160, 80, 20);
        this.rangeVNEdit.setMaxStringLength(5);
        this.rangeVNEdit.setText(Float.toString(this.tileStructure.getRangeVN()));

        this.rangeVPEdit = new GuiTextField(8, this.fontRenderer, this.width / 2 + 8, 160, 80, 20);
        this.rangeVPEdit.setMaxStringLength(5);
        this.rangeVPEdit.setText(Float.toString(this.tileStructure.getRangeVP()));

    }
    
    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
    
    @Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.enabled) {
			if (button.id == 1) { //cancel
				//-> revert the changes
				this.mc.displayGuiScreen((GuiScreen) null);
			} else if (button.id == 0) { //ok
				if (ServerPackage.sendSummonerUpdate(this)) {
					this.mc.displayGuiScreen((GuiScreen) null);
				}
			}
		}
	}
	
	public void writeToBuffer(PacketBuffer buffer) {
		this.tileStructure.writeCoordinates(buffer);
		buffer.writeString(this.nameEdit.getText());
		buffer.writeFloat(this.parseFloat(this.pRangeEdit.getText(), 1F, 256F, 16F));
		buffer.writeInt(this.parseInt(this.maxMobEdit.getText(), 1, 1024, 4));
		buffer.writeInt(this.parseInt(this.delayBaseEdit.getText(), 10, 5000, 100));
		buffer.writeInt(this.parseInt(this.delayRangeEdit.getText(), 0, 5000, 100));
		buffer.writeFloat(this.parseFloat(this.rangeHEdit.getText(), 1F, 32F, 8F));
		buffer.writeFloat(this.parseFloat(this.rangeVNEdit.getText(), 1F, 16F, 4F));
		buffer.writeFloat(this.parseFloat(this.rangeVPEdit.getText(), 1F, 16F, 4F));
	}
	
	private int parseInt(String string, int min, int max, int def) {
		try {
			return Math.max(Math.min(Integer.parseInt(string), max), min);
		} catch (NumberFormatException var3) {
			return def;
		}
	}
	
	private float parseFloat(String string, float min, float max, float def) {
		try {
			return Math.max(Math.min(Float.parseFloat(string), max), min);
		} catch (NumberFormatException var3) {
			return def;
		}
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (this.nameEdit.getVisible() && isValidCharacterForName(typedChar, keyCode)) {
			this.nameEdit.textboxKeyTyped(typedChar, keyCode);
		}
		if (this.pRangeEdit.getVisible()) {
			this.pRangeEdit.textboxKeyTyped(typedChar, keyCode);
		}
		if (this.maxMobEdit.getVisible()) {
			this.maxMobEdit.textboxKeyTyped(typedChar, keyCode);
		}
		if (this.delayBaseEdit.getVisible()) {
			this.delayBaseEdit.textboxKeyTyped(typedChar, keyCode);
		}
		if (this.delayRangeEdit.getVisible()) {
			this.delayRangeEdit.textboxKeyTyped(typedChar, keyCode);
		}
		if (this.rangeHEdit.getVisible()) {
			this.rangeHEdit.textboxKeyTyped(typedChar, keyCode);
		}
		if (this.rangeVNEdit.getVisible()) {
			this.rangeVNEdit.textboxKeyTyped(typedChar, keyCode);
		}
		if (this.rangeVPEdit.getVisible()) {
			this.rangeVPEdit.textboxKeyTyped(typedChar, keyCode);
		}
		if (keyCode != 28 && keyCode != 156) {
			if (keyCode == 1) {
				this.actionPerformed(this.cancelButton);
			}
		} else {
			this.actionPerformed(this.doneButton);
		}
	}

	private static boolean isValidCharacterForName(char p_190301_0_, int p_190301_1_) {
		boolean flag = true;
		for (int i : LEGAL_KEY_CODES) {
			if (i == p_190301_1_) {
				return true;
			}
		}
		for (char c0 : ChatAllowedCharacters.ILLEGAL_STRUCTURE_CHARACTERS) {
			if (c0 == p_190301_0_) {
				flag = false;
				break;
			}
		}
		return flag;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (this.nameEdit.getVisible()) {
			this.nameEdit.mouseClicked(mouseX, mouseY, mouseButton);
		}
		if (this.pRangeEdit.getVisible()) {
			this.pRangeEdit.mouseClicked(mouseX, mouseY, mouseButton);
		}
		if (this.maxMobEdit.getVisible()) {
			this.maxMobEdit.mouseClicked(mouseX, mouseY, mouseButton);
		}
		if (this.delayBaseEdit.getVisible()) {
			this.delayBaseEdit.mouseClicked(mouseX, mouseY, mouseButton);
		}
		if (this.delayRangeEdit.getVisible()) {
			this.delayRangeEdit.mouseClicked(mouseX, mouseY, mouseButton);
		}
		if (this.rangeHEdit.getVisible()) {
			this.rangeHEdit.mouseClicked(mouseX, mouseY, mouseButton);
		}
		if (this.rangeVNEdit.getVisible()) {
			this.rangeVNEdit.mouseClicked(mouseX, mouseY, mouseButton);
		}
		if (this.rangeVPEdit.getVisible()) {
			this.rangeVPEdit.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, "Edit Summoner", this.width / 2, 10, 16777215);

		this.drawString(this.fontRenderer, "Entity", this.width / 2 - 153, 30, 10526880);
		this.nameEdit.drawTextBox();
		
		this.drawString(this.fontRenderer, "Conditions: player range and mob limit", this.width / 2 - 153, 70, 10526880);
		this.pRangeEdit.drawTextBox();
		this.maxMobEdit.drawTextBox();
		
		this.drawString(this.fontRenderer, "Spawn Delay: base ticks and random extra ticks", this.width / 2 - 153, 110, 10526880);
		this.delayBaseEdit.drawTextBox();
		this.delayRangeEdit.drawTextBox();
		
		this.drawString(this.fontRenderer, "Range: radius / down / up", this.width / 2 - 153, 150, 10526880);
		this.rangeHEdit.drawTextBox();
		this.rangeVNEdit.drawTextBox();
		this.rangeVPEdit.drawTextBox();
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

}
