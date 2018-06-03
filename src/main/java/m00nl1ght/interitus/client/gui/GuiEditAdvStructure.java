package m00nl1ght.interitus.client.gui;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

import m00nl1ght.interitus.Interitus;
import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure;
import m00nl1ght.interitus.block.tileentity.TileEntityAdvStructure.Mode;
import m00nl1ght.interitus.network.CDefaultPackage;
import m00nl1ght.interitus.structures.StructurePackInfo;

public class GuiEditAdvStructure extends GuiScreen {
	
    public static final int[] LEGAL_KEY_CODES = new int[] {203, 205, 14, 211, 199, 207};
    private final TileEntityAdvStructure tileStructure;
    private GuiTextField nameEdit, packEdit, posXEdit, posYEdit, posZEdit, sizeXEdit, sizeYEdit, sizeZEdit, dataEdit;
    private GuiButton doneButton,saveButton,loadButton,rotateZeroDegreesButton,rotateNinetyDegreesButton,rotate180DegreesButton,rotate270DegressButton,modeButton,detectSizeButton,showEntitiesButton,mirrorButton,showAirButton,showBoundingBoxButton,giveToolButton,emptyAirButton,voidAirButton,editDataButton,browsePacksButton;
    private final List<GuiTextField> tabOrder = Lists.<GuiTextField>newArrayList();
    private final DecimalFormat decimalFormat = new DecimalFormat("0.0###");
    public final StructurePackInfo packInfo;

    public GuiEditAdvStructure(TileEntityAdvStructure te, StructurePackInfo packInfo) {
        this.tileStructure = te;
        this.packInfo = packInfo;
        this.decimalFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
    }
    
    @Override
    public void updateScreen() {
        this.nameEdit.updateCursorCounter();
        this.packEdit.updateCursorCounter();
        this.posXEdit.updateCursorCounter();
        this.posYEdit.updateCursorCounter();
        this.posZEdit.updateCursorCounter();
        this.sizeXEdit.updateCursorCounter();
        this.sizeYEdit.updateCursorCounter();
        this.sizeZEdit.updateCursorCounter();
        this.dataEdit.updateCursorCounter();
    }
    
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.tileStructure.setAcceptUpdates(false); // in case another player modifies this TE while this GUI is opened
        this.buttonList.clear();
        this.doneButton = this.addButton(new GuiButton(0, this.width / 2 - 4 - 150, 210, 150, 20, I18n.format("gui.done")));
        this.saveButton = this.addButton(new GuiButton(9, this.width / 2 + 4 + 100, 185, 50, 20, I18n.format("structure_block.button.save")));
        this.loadButton = this.addButton(new GuiButton(10, this.width / 2 + 4 + 100, 185, 50, 20, I18n.format("structure_block.button.load")));
        this.modeButton = this.addButton(new GuiButton(18, this.width / 2 + 4, 210, 150, 20, "MODE"));
        this.detectSizeButton = this.addButton(new GuiButton(19, this.width / 2 + 4 + 100, 80, 50, 20, I18n.format("structure_block.button.detect_size")));
        this.showEntitiesButton = this.addButton(new GuiButton(20, this.width / 2 - 4 - 150, 106, 124, 20, "ENTITIES"));
        this.mirrorButton = this.addButton(new GuiButton(21, this.width / 2 - 20, 185, 40, 20, "MIRROR"));
        this.showAirButton = this.addButton(new GuiButton(22, this.width / 2 - 24, 106, 124, 20, "SHOWAIR"));
        this.showBoundingBoxButton = this.addButton(new GuiButton(23, this.width / 2 - 4 - 150, 130, 124, 20, "SHOWBB"));
        this.giveToolButton = this.addButton(new GuiButton(24, this.width / 2 - 4 - 150, 130, 80, 20, "Data Tool"));
        this.editDataButton = this.addButton(new GuiButton(27, this.width / 2 - 4 - 65, 130, 39, 20, "Edit"));
        this.browsePacksButton = this.addButton(new GuiButton(28, this.width / 2 - 16, 40, 15, 20, "..."));
        this.emptyAirButton = this.addButton(new GuiButton(25, this.width / 2 + 4, 185, 45, 20, "Air > X"));
        this.voidAirButton = this.addButton(new GuiButton(26, this.width / 2 + 4 + 50, 185, 45, 20, "X > Air"));
        this.rotateZeroDegreesButton = this.addButton(new GuiButton(11, this.width / 2 - 1 - 40 - 1 - 40 - 20, 185, 40, 20, "0"));
        this.rotateNinetyDegreesButton = this.addButton(new GuiButton(12, this.width / 2 - 1 - 40 - 20, 185, 40, 20, "90"));
        this.rotate180DegreesButton = this.addButton(new GuiButton(13, this.width / 2 + 1 + 20, 185, 40, 20, "180"));
        this.rotate270DegressButton = this.addButton(new GuiButton(14, this.width / 2 + 1 + 40 + 1 + 20, 185, 40, 20, "270"));
        this.packEdit = new GuiTextField(20, this.fontRenderer, this.width / 2 - 152, 40, 150, 20);
        this.packEdit.setMaxStringLength(32);
        this.packEdit.setText(this.packInfo.active.name);
        this.tabOrder.add(this.packEdit);
        this.nameEdit = new GuiTextField(2, this.fontRenderer, this.width / 2 + 4, 40, 150, 20);
        this.nameEdit.setMaxStringLength(32);
        this.nameEdit.setText(this.tileStructure.getName());
        this.tabOrder.add(this.nameEdit);
        BlockPos blockpos = this.tileStructure.getPosition();
        this.posXEdit = new GuiTextField(3, this.fontRenderer, this.width / 2 - 152, 80, 40, 20);
        this.posXEdit.setMaxStringLength(10);
        this.posXEdit.setText(Integer.toString(blockpos.getX()));
        this.tabOrder.add(this.posXEdit);
        this.posYEdit = new GuiTextField(4, this.fontRenderer, this.width / 2 - 112, 80, 40, 20);
        this.posYEdit.setMaxStringLength(10);
        this.posYEdit.setText(Integer.toString(blockpos.getY()));
        this.tabOrder.add(this.posYEdit);
        this.posZEdit = new GuiTextField(5, this.fontRenderer, this.width / 2 - 72, 80, 40, 20);
        this.posZEdit.setMaxStringLength(10);
        this.posZEdit.setText(Integer.toString(blockpos.getZ()));
        this.tabOrder.add(this.posZEdit);
        BlockPos blockpos1 = this.tileStructure.getStructureSize();
        this.sizeXEdit = new GuiTextField(6, this.fontRenderer, this.width / 2 - 22, 80, 40, 20);
        this.sizeXEdit.setMaxStringLength(10);
        this.sizeXEdit.setText(Integer.toString(blockpos1.getX()));
        this.tabOrder.add(this.sizeXEdit);
        this.sizeYEdit = new GuiTextField(7, this.fontRenderer, this.width / 2 + 18, 80, 40, 20);
        this.sizeYEdit.setMaxStringLength(10);
        this.sizeYEdit.setText(Integer.toString(blockpos1.getY()));
        this.tabOrder.add(this.sizeYEdit);
        this.sizeZEdit = new GuiTextField(8, this.fontRenderer, this.width / 2 + 58, 80, 40, 20);
        this.sizeZEdit.setMaxStringLength(10);
        this.sizeZEdit.setText(Integer.toString(blockpos1.getZ()));
        this.tabOrder.add(this.sizeZEdit);
        this.dataEdit = new GuiTextField(17, this.fontRenderer, this.width / 2 - 152, 120, 240, 20);
        this.dataEdit.setMaxStringLength(128);
        this.dataEdit.setText(this.tileStructure.getMetadata());
        this.tabOrder.add(this.dataEdit);
        this.updateMirrorButton();
        this.updateDirectionButtons();
        this.updateMode();
        this.updateEntitiesButton();
        this.updateToggleAirButton();
        this.updateToggleBoundingBox();
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
				if (this.sendToServer(1)) {
					this.mc.displayGuiScreen((GuiScreen) null);
				}
			} else if (button.id == 9) {
				if (this.tileStructure.getMode() == Mode.SAVE) {
					this.sendToServer(2);
					this.mc.displayGuiScreen((GuiScreen) null);
				}
			} else if (button.id == 10) {
				if (this.tileStructure.getMode() == Mode.LOAD) {
					this.sendToServer(3);
					this.mc.displayGuiScreen((GuiScreen) null);
				}
			} else if (button.id == 11) {
				this.tileStructure.setRotation(Rotation.NONE);
				this.updateDirectionButtons();
			} else if (button.id == 12) {
				this.tileStructure.setRotation(Rotation.CLOCKWISE_90);
				this.updateDirectionButtons();
			} else if (button.id == 13) {
				this.tileStructure.setRotation(Rotation.CLOCKWISE_180);
				this.updateDirectionButtons();
			} else if (button.id == 14) {
				this.tileStructure.setRotation(Rotation.COUNTERCLOCKWISE_90);
				this.updateDirectionButtons();
			} else if (button.id == 18) {
				this.tileStructure.nextMode();
				this.updateMode();
			} else if (button.id == 19) {
				if (this.tileStructure.getMode() == Mode.SAVE) {
					this.sendToServer(4);
					this.mc.displayGuiScreen((GuiScreen) null);
				}
			} else if (button.id == 20) {
				this.tileStructure.setIgnoresEntities(!this.tileStructure.ignoresEntities());
				this.updateEntitiesButton();
			} else if (button.id == 22) {
				this.tileStructure.setShowAir(!this.tileStructure.showsAir());
				this.updateToggleAirButton();
			} else if (button.id == 23) {
				this.tileStructure.setShowBoundingBox(!this.tileStructure.showsBoundingBox());
				this.updateToggleBoundingBox();
			} else if (button.id == 24) {
				CDefaultPackage.requestAction(this.tileStructure, 0, 0, 0);
				this.giveToolButton.enabled=false;
			} else if (button.id == 25) {
				CDefaultPackage.requestAction(this.tileStructure, 1, 0, 0);
			} else if (button.id == 26) {
				CDefaultPackage.requestAction(this.tileStructure, 2, 0, 0);
			} else if (button.id == 27) {
				this.sendToServer(1); // untested
				Interitus.proxy.displayStructureDataScreen(this.tileStructure, this);
			} else if (button.id == 28) {
				this.mc.displayGuiScreen(null);
			} else if (button.id == 21) {
				switch (this.tileStructure.getMirror()) {
				case NONE:
					this.tileStructure.setMirror(Mirror.LEFT_RIGHT);
					break;
				case LEFT_RIGHT:
					this.tileStructure.setMirror(Mirror.FRONT_BACK);
					break;
				case FRONT_BACK:
					this.tileStructure.setMirror(Mirror.NONE);
				}
				this.updateMirrorButton();
			}
		}
	}
	
	private void updateEntitiesButton() {
		if (!this.tileStructure.ignoresEntities()) {
			this.showEntitiesButton.displayString = "Entities: Include";
		} else {
			this.showEntitiesButton.displayString = "Entities: Ignore";
		}
	}

	private void updateToggleAirButton() {
		if (this.tileStructure.showsAir()) {
			this.showAirButton.displayString = "Invisible Blocks: Show";
		} else {
			this.showAirButton.displayString = "Invisible Blocks: Hide";
		}
	}

	private void updateToggleBoundingBox() {
		if (this.tileStructure.showsBoundingBox()) {
			this.showBoundingBoxButton.displayString = "Bounding Box: Show";
		} else {
			this.showBoundingBoxButton.displayString = "Bounding Box: Hide";
		}
	}

	private void updateMirrorButton() {
		Mirror mirror = this.tileStructure.getMirror();
		switch (mirror) {
		case NONE:
			this.mirrorButton.displayString = "|";
			break;
		case LEFT_RIGHT:
			this.mirrorButton.displayString = "< >";
			break;
		case FRONT_BACK:
			this.mirrorButton.displayString = "^ v";
		}
	}

	private void updateDirectionButtons() {
		this.rotateZeroDegreesButton.enabled = true;
		this.rotateNinetyDegreesButton.enabled = true;
		this.rotate180DegreesButton.enabled = true;
		this.rotate270DegressButton.enabled = true;

		switch (this.tileStructure.getRotation()) {
		case NONE:
			this.rotateZeroDegreesButton.enabled = false;
			break;
		case CLOCKWISE_180:
			this.rotate180DegreesButton.enabled = false;
			break;
		case COUNTERCLOCKWISE_90:
			this.rotate270DegressButton.enabled = false;
			break;
		case CLOCKWISE_90:
			this.rotateNinetyDegreesButton.enabled = false;
		}
	}

	private void updateMode() {
		this.nameEdit.setFocused(false);
		this.packEdit.setFocused(false);
		this.posXEdit.setFocused(false);
		this.posYEdit.setFocused(false);
		this.posZEdit.setFocused(false);
		this.sizeXEdit.setFocused(false);
		this.sizeYEdit.setFocused(false);
		this.sizeZEdit.setFocused(false);
		this.dataEdit.setFocused(false);
		this.nameEdit.setVisible(false);
		this.packEdit.setVisible(false);
		this.posXEdit.setVisible(false);
		this.posYEdit.setVisible(false);
		this.posZEdit.setVisible(false);
		this.sizeXEdit.setVisible(false);
		this.sizeYEdit.setVisible(false);
		this.sizeZEdit.setVisible(false);
		this.dataEdit.setVisible(false);
		this.saveButton.visible = false;
		this.loadButton.visible = false;
		this.detectSizeButton.visible = false;
		this.showEntitiesButton.visible = false;
		this.giveToolButton.visible = false;
		this.editDataButton.visible = false;
		this.emptyAirButton.visible = false;
		this.voidAirButton.visible = false;
		this.mirrorButton.visible = false;
		this.rotateZeroDegreesButton.visible = false;
		this.rotateNinetyDegreesButton.visible = false;
		this.rotate180DegreesButton.visible = false;
		this.rotate270DegressButton.visible = false;
		this.showAirButton.visible = false;
		this.showBoundingBoxButton.visible = false;
		this.browsePacksButton.visible = false;

		switch (this.tileStructure.getMode()) {
		case SAVE:
			this.nameEdit.setVisible(true);
			this.nameEdit.setFocused(true);
			this.nameEdit.setEnabled(true);
			this.packEdit.setVisible(true);
			this.packEdit.setEnabled(false);
			this.posXEdit.setVisible(true);
			this.posYEdit.setVisible(true);
			this.posZEdit.setVisible(true);
			this.sizeXEdit.setVisible(true);
			this.sizeYEdit.setVisible(true);
			this.sizeZEdit.setVisible(true);
			this.saveButton.visible = true;
			this.detectSizeButton.visible = true;
			this.showEntitiesButton.visible = true;
			this.giveToolButton.visible = true;
			this.editDataButton.visible = true;
			this.emptyAirButton.visible = true;
			this.voidAirButton.visible = true;
			this.showAirButton.visible = true;
			this.browsePacksButton.visible = true;
			break;
		case LOAD:
			this.nameEdit.setVisible(true);
			this.nameEdit.setFocused(true);
			this.packEdit.setVisible(true);
			this.packEdit.setEnabled(false);
			this.posXEdit.setVisible(true);
			this.posYEdit.setVisible(true);
			this.posZEdit.setVisible(true);
			this.loadButton.visible = true;
			this.showEntitiesButton.visible = true;
			this.mirrorButton.visible = true;
			this.rotateZeroDegreesButton.visible = true;
			this.rotateNinetyDegreesButton.visible = true;
			this.rotate180DegreesButton.visible = true;
			this.rotate270DegressButton.visible = true;
			this.showBoundingBoxButton.visible = true;
			this.browsePacksButton.visible = true;
			this.updateDirectionButtons();
			break;
		case CORNER:
			this.nameEdit.setVisible(true);
			this.nameEdit.setFocused(true);
			this.packEdit.setVisible(true);
			this.packEdit.setEnabled(false);
			this.browsePacksButton.visible = true;
			break;
		case DATA:
			this.dataEdit.setVisible(true);
			this.dataEdit.setFocused(true);
		}

		this.modeButton.displayString = "Mode: " + this.tileStructure.getMode().title();
	}
	
	private boolean sendToServer(int pendingAction) {
		this.tileStructure.setName(this.nameEdit.getText());
		this.tileStructure.setPosition(new BlockPos(Integer.parseInt(this.posXEdit.getText()), Integer.parseInt(this.posYEdit.getText()), Integer.parseInt(this.posZEdit.getText())));
		this.tileStructure.setSize(new BlockPos(Integer.parseInt(this.sizeXEdit.getText()), Integer.parseInt(this.sizeYEdit.getText()), Integer.parseInt(this.sizeZEdit.getText())));
		this.tileStructure.setMetadata(this.dataEdit.getText());
		return CDefaultPackage.sendStructUpdatePacket(this.tileStructure, pendingAction);
	}

	private int parseCoordinate(String p_189817_1_) {
		try {
			return Integer.parseInt(p_189817_1_);
		} catch (NumberFormatException var3) {
			return 0;
		}
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (this.nameEdit.getVisible() && isValidCharacterForName(typedChar, keyCode)) {
			this.nameEdit.textboxKeyTyped(typedChar, keyCode);
		}
		if (this.posXEdit.getVisible()) {
			this.posXEdit.textboxKeyTyped(typedChar, keyCode);
		}
		if (this.posYEdit.getVisible()) {
			this.posYEdit.textboxKeyTyped(typedChar, keyCode);
		}
		if (this.posZEdit.getVisible()) {
			this.posZEdit.textboxKeyTyped(typedChar, keyCode);
		}
		if (this.sizeXEdit.getVisible()) {
			this.sizeXEdit.textboxKeyTyped(typedChar, keyCode);
		}
		if (this.sizeYEdit.getVisible()) {
			this.sizeYEdit.textboxKeyTyped(typedChar, keyCode);
		}
		if (this.sizeZEdit.getVisible()) {
			this.sizeZEdit.textboxKeyTyped(typedChar, keyCode);
		}
		if (this.dataEdit.getVisible()) {
			this.dataEdit.textboxKeyTyped(typedChar, keyCode);
		}
		if (keyCode == 15) {
			GuiTextField guitextfield = null;
			GuiTextField guitextfield1 = null;

			for (GuiTextField guitextfield2 : this.tabOrder) {
				if (guitextfield != null && guitextfield2.getVisible()) {
					guitextfield1 = guitextfield2;
					break;
				}
				if (guitextfield2.isFocused() && guitextfield2.getVisible()) {
					guitextfield = guitextfield2;
				}
			}

			if (guitextfield != null && guitextfield1 == null) {
				for (GuiTextField guitextfield3 : this.tabOrder) {
					if (guitextfield3.getVisible() && guitextfield3 != guitextfield) {
						guitextfield1 = guitextfield3;
						break;
					}
				}
			}

			if (guitextfield1 != null && guitextfield1 != guitextfield) {
				guitextfield.setFocused(false);
				guitextfield1.setFocused(true);
			}
		}

		if (keyCode == 1) {
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
		if (this.packEdit.getVisible()) {
			this.packEdit.mouseClicked(mouseX, mouseY, mouseButton);
		}
		if (this.posXEdit.getVisible()) {
			this.posXEdit.mouseClicked(mouseX, mouseY, mouseButton);
		}
		if (this.posYEdit.getVisible()) {
			this.posYEdit.mouseClicked(mouseX, mouseY, mouseButton);
		}
		if (this.posZEdit.getVisible()) {
			this.posZEdit.mouseClicked(mouseX, mouseY, mouseButton);
		}
		if (this.sizeXEdit.getVisible()) {
			this.sizeXEdit.mouseClicked(mouseX, mouseY, mouseButton);
		}
		if (this.sizeYEdit.getVisible()) {
			this.sizeYEdit.mouseClicked(mouseX, mouseY, mouseButton);
		}
		if (this.sizeZEdit.getVisible()) {
			this.sizeZEdit.mouseClicked(mouseX, mouseY, mouseButton);
		}
		if (this.dataEdit.getVisible()) {
			this.dataEdit.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		Mode tileentitystructure$mode = this.tileStructure.getMode();
		this.drawCenteredString(this.fontRenderer, "Advanced Structure", this.width / 2, 10, 16777215);

		if (tileentitystructure$mode != Mode.DATA) {
			this.drawString(this.fontRenderer, "Structure Pack", this.width / 2 - 153, 30, 10526880);
			this.drawString(this.fontRenderer, "Structure Name", this.width / 2 + 3, 30, 10526880);
			this.packEdit.drawTextBox();
			this.nameEdit.drawTextBox();
		}

		if (tileentitystructure$mode == Mode.LOAD || tileentitystructure$mode == Mode.SAVE) {
			this.drawString(this.fontRenderer, "Structure Position", this.width / 2 - 153, 70, 10526880);
			this.posXEdit.drawTextBox();
			this.posYEdit.drawTextBox();
			this.posZEdit.drawTextBox();
		}

		if (tileentitystructure$mode == Mode.SAVE) {
			this.drawString(this.fontRenderer, "Structure Size", this.width / 2 - 23, 70, 10526880);
			this.sizeXEdit.drawTextBox();
			this.sizeYEdit.drawTextBox();
			this.sizeZEdit.drawTextBox();
			this.drawString(this.fontRenderer, this.tileStructure.getConditions().size()+" conditions", this.width / 2 - 22, 131, 10526880);
			this.drawString(this.fontRenderer, this.tileStructure.getLoot().size()+" loot entries", this.width / 2 - 22, 141, 10526880);
		}

		if (tileentitystructure$mode == Mode.LOAD) {
			//NOP
		}

		if (tileentitystructure$mode == Mode.DATA) {
			this.drawString(this.fontRenderer, I18n.format("structure_block.custom_data"), this.width / 2 - 153, 110, 10526880);
			this.dataEdit.drawTextBox();
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

}
